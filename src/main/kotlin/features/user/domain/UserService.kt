package com.application.features.user.domain

import at.favre.lib.crypto.bcrypt.BCrypt
import com.application.features.user.data.UserRepository
import com.application.features.user.domain.User
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.contentOrNull
import org.slf4j.LoggerFactory
import java.net.URLDecoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class UserService(
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    suspend fun login(login: String, password: String): User? {
        val user = userRepository.getByLogin(login) ?: return null

        val verified = BCrypt.verifyer().verify(
            password.toCharArray(),
            user.passwordHash
        ).verified

        return if (verified) user else null
    }

    suspend fun register(username: String, login: String, password: String): User? {
        if (userRepository.getByLogin(login) != null) return null

        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        return userRepository.create(
            User(
                username = username,
                login = login,
                passwordHash = hash,
                habits = emptyList()
            )
        )
    }

    suspend fun loginOrRegisterWithTelegram(initData: String, botToken: String): User? {
        val tgUser = validateAndParseInitData(initData, botToken) ?: return null

        userRepository.getByTelegramId(tgUser.id)?.let { return it }

        val login = "tg_${tgUser.id}"
        return userRepository.create(
            User(
                username = tgUser.displayName,
                login = login,
                passwordHash = "",
                telegramId = tgUser.id,
                habits = emptyList()
            )
        )
    }

    private fun validateAndParseInitData(initData: String, botToken: String): TgUser? {
        if (botToken.isBlank()) {
            log.error("TG auth failed: TG_BOT_TOKEN is not set")
            return null
        }

        val params = mutableMapOf<String, String>()
        for (part in initData.split("&")) {
            val idx = part.indexOf("=")
            if (idx < 0) continue
            params[part.substring(0, idx)] = URLDecoder.decode(part.substring(idx + 1), "UTF-8")
        }

        val hash = params.remove("hash") ?: run {
            log.error("TG auth failed: no 'hash' field in initData")
            return null
        }

        val dataCheckString = params.entries
            .sortedBy { it.key }
            .joinToString("\n") { "${it.key}=${it.value}" }

        val secretKey = hmacSha256("WebAppData".toByteArray(Charsets.UTF_8), botToken.toByteArray(Charsets.UTF_8))
        val computedHash = hmacSha256(dataCheckString.toByteArray(Charsets.UTF_8), secretKey)
        val computedHashHex = computedHash.joinToString("") { "%02x".format(it) }

        if (!computedHashHex.equals(hash, ignoreCase = true)) {
            log.error("TG auth failed: HMAC mismatch (check TG_BOT_TOKEN)")
            log.debug("dataCheckString:\n{}", dataCheckString)
            return null
        }

        val userJson = params["user"] ?: run {
            log.error("TG auth failed: no 'user' field in initData")
            return null
        }

        return try {
            val obj = Json.parseToJsonElement(userJson).jsonObject
            val id = obj["id"]?.jsonPrimitive?.long ?: return null
            val firstName = obj["first_name"]?.jsonPrimitive?.contentOrNull ?: ""
            val lastName = obj["last_name"]?.jsonPrimitive?.contentOrNull
            val username = obj["username"]?.jsonPrimitive?.contentOrNull
            log.info("TG auth success: id={} username={}", id, username)
            TgUser(id, firstName, lastName, username)
        } catch (e: Exception) {
            log.error("TG auth failed: error parsing user JSON - {}", e.message)
            null
        }
    }

    private fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray =
        Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(key, "HmacSHA256"))
        }.doFinal(data)

    private data class TgUser(
        val id: Long,
        val firstName: String,
        val lastName: String?,
        val username: String?,
    ) {
        val displayName: String get() = buildString {
            append(firstName)
            if (!lastName.isNullOrBlank()) append(" $lastName")
        }.ifBlank { username ?: "TG_$id" }
    }
}