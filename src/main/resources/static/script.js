let currentUser = null
let openedGroup = null
let botUsername = ''

const tg = window.Telegram?.WebApp

document.addEventListener('DOMContentLoaded', () => {

    if (tg) {
        tg.ready()
        tg.expand()
        document.body.classList.add('tg-mode')
    }

    // Always fetch bot config so botUsername is available for invite links
    api('/config').then(cfg => { if (cfg?.botUsername) botUsername = cfg.botUsername })

    if (tg?.initData) {
        api('/auth/telegram', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ initData: tg.initData })
        })
            .then(user => {
                currentUser = user
                localStorage.setItem('currentUser', JSON.stringify(user))
                showDashboard()
                loadAll()
                checkPendingInvite()
            })
            .catch(e => alert('Telegram auth failed: ' + e.message))
        return
    }

    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
        currentUser = JSON.parse(savedUser)
        showDashboard()
        loadAll()
        checkPendingInvite()
        return
    }

    // Load bot username and inject Telegram Login Widget
    api('/config')
        .then(cfg => {
            if (!cfg?.botUsername) return
            botUsername = cfg.botUsername
            const script = document.createElement('script')
            script.src = 'https://telegram.org/js/telegram-widget.js?22'
            script.setAttribute('data-telegram-login', cfg.botUsername)
            script.setAttribute('data-size', 'large')
            script.setAttribute('data-radius', '8')
            script.setAttribute('data-onauth', 'onTelegramWidgetAuth(user)')
            script.setAttribute('data-request-access', 'write')
            script.async = true
            document.getElementById('tgWidgetContainer').appendChild(script)
        })
})

/* ---------- HELPERS ---------- */

function api(url, options = {}) {

    return fetch(url, options)
        .then(async r => {

            if (!r.ok) {

                const text = await r.text()
                throw new Error(text || "Request failed")
            }

            if (r.status === 204) {
                return null
            }

            // Some endpoints respond with empty body + 200 (e.g. delete).
            const contentType = r.headers.get("content-type") || ""
            if (!contentType.includes("application/json")) {
                return null
            }

            return r.json()
        })
}

function loadAll() {
    loadHabits()
    loadGroups()
}

/* ---------- TELEGRAM WIDGET ---------- */

function onTelegramWidgetAuth(user) {

    api('/auth/telegram/widget', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    })
        .then(loggedInUser => {
            currentUser = loggedInUser
            localStorage.setItem('currentUser', JSON.stringify(loggedInUser))
            showDashboard()
            loadAll()
            checkPendingInvite()
        })
        .catch(e => alert('Telegram auth failed: ' + e.message))
}

/* ---------- INVITE ---------- */

function checkPendingInvite() {
    // TG Mini App: launched via t.me/bot?startapp=invite_<id>
    const startParam = tg?.initDataUnsafe?.start_param
        ?? new URLSearchParams(location.search).get('join')
    if (!startParam) return
    const match = String(startParam).match(/^invite_(\d+)$/)
    if (!match) return
    joinGroupByInvite(parseInt(match[1]))
}

function joinGroupByInvite(groupId) {
    api(`/groups/${groupId}/members`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login: currentUser.login })
    })
        .then(() => {
            loadGroups()
            openGroup(groupId)
        })
        .catch(e => alert('Could not join group: ' + e.message))
}

function copyInviteLink() {
    if (!openedGroup) return
    const url = botUsername
        ? `https://t.me/${botUsername}?startapp=invite_${openedGroup.id}`
        : `${location.origin}${location.pathname}?join=${openedGroup.id}`
    navigator.clipboard.writeText(url)
        .then(() => showToast('Invite link copied!'))
        .catch(() => {
            // fallback for browsers without clipboard API
            prompt('Copy this link:', url)
        })
}

/* ---------- LOGIN ---------- */

function login() {

    const login =
        document.getElementById("loginLogin").value

    const password =
        document.getElementById("loginPassword").value

    api("/user/login", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, password })
    })
        .then(user => {

            currentUser = user

            localStorage.setItem(
                "currentUser",
                JSON.stringify(user)
            )

            showDashboard()
            loadAll()
            checkPendingInvite()
        })
        .catch(e => alert(e.message))
}

/* ---------- REGISTER ---------- */

function register() {

    const username =
        document.getElementById("regName").value

    const login =
        document.getElementById("regLogin").value

    const password =
        document.getElementById("regPassword").value

    api("/user/register", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username,
            login,
            password
        })
    })
        .then(() => {
            alert("Registered successfully")
            showLogin()
        })
        .catch(e => alert(e.message))
}

/* ---------- HABITS ---------- */

function loadHabits() {

    api(`/users/${currentUser.login}/habits`)
        .then(displayHabits)
}

function displayHabits(habits) {

    const list =
        document.getElementById("habitList")

    const empty =
        document.getElementById("emptyState")

    list.innerHTML = ""

    if (!habits || habits.length === 0) {
        empty.style.display = "block"
        return
    }

    empty.style.display = "none"

    habits.forEach(h => {

        const div = document.createElement("div")
        div.className = "habit"

        const checkedIn = !!h.checkedInToday
        const checkBtn = checkedIn
            ? `<button class="check-button done" disabled title="Already checked in today">✓ Today</button>`
            : `<button class="check-button" onclick="checkinUserHabit(${h.id})">Check in</button>`

        div.innerHTML = `
            <span class="habit-name">${escapeHtml(h.habitName)}</span>
            <span class="streak" title="Current streak">🔥 ${h.streak ?? 0}</span>
            ${checkBtn}
            <button class="danger" onclick="deleteHabit(${h.id})">
                Delete
            </button>
        `

        list.appendChild(div)
    })
}

function addHabit() {

    const habitName =
        document.getElementById("habitName").value

    if (!habitName) {
        return
    }

    api(`/users/${currentUser.login}/habits`, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ habitName })
    })
        .then(() => {

            document.getElementById(
                "habitName"
            ).value = ""

            loadHabits()
        })
        .catch(e => alert(e.message))
}

function deleteHabit(habitId) {

    api(`/habits/${habitId}`, {
        method: "DELETE"
    })
        .then(loadHabits)
        .catch(e => alert(e.message))
}

function checkinUserHabit(habitId) {

    api(`/users/${currentUser.login}/habits/${habitId}/checkin`, {
        method: "POST"
    })
        .then(loadHabits)
        .catch(e => alert(e.message))
}

/* ---------- GROUPS ---------- */

function loadGroups() {

    api(`/users/${currentUser.login}/groups`)
        .then(displayGroups)
}

function displayGroups(groups) {

    const list =
        document.getElementById("groupList")

    const empty =
        document.getElementById("emptyGroupState")

    list.innerHTML = ""

    if (!groups || groups.length === 0) {
        if (empty) empty.style.display = "block"
        return
    }

    if (empty) empty.style.display = "none"

    groups.forEach(group => {

        const div = document.createElement("div")
        div.className = "group"

        const count = group.members.length
        const membersLabel = count === 1 ? "1 member" : `${count} members`

        div.innerHTML = `
            <div class="group-info">
                <strong>${escapeHtml(group.name)}</strong>
                <span class="group-meta">${membersLabel}</span>
            </div>
            <div class="group-actions">
                <button class="small" onclick="openGroup(${group.id})">Open</button>
            </div>
        `

        list.appendChild(div)
    })
}

function createGroup() {

    const name =
        document.getElementById("groupName").value

    if (!name) {
        return
    }

    api("/groups", {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name,
            creatorLogin: currentUser.login
        })
    })
        .then(() => {

            document.getElementById(
                "groupName"
            ).value = ""

            loadGroups()
        })
        .catch(e => alert(e.message))
}

function deleteGroup(groupId) {

    if (!confirm("Delete this group? This will remove all its habits too.")) return

    api(`/groups/${groupId}?requesterLogin=${encodeURIComponent(currentUser.login)}`, {
        method: "DELETE"
    })
        .then(loadGroups)
        .catch(e => alert(e.message))
}

function deleteOpenedGroup() {

    if (!openedGroup) return
    if (!confirm("Delete this group? This will remove all its habits too.")) return

    api(`/groups/${openedGroup.id}?requesterLogin=${encodeURIComponent(currentUser.login)}`, {
        method: "DELETE"
    })
        .then(backToDashboard)
        .catch(e => alert(e.message))
}

function leaveOpenedGroup(userId) {

    if (!openedGroup) return
    if (!confirm("Leave this group?")) return

    api(`/groups/${openedGroup.id}/members/${userId}`, {
        method: "DELETE"
    })
        .then(backToDashboard)
        .catch(e => alert(e.message))
}

function openGroup(groupId) {

    api(`/groups/${groupId}?userLogin=${encodeURIComponent(currentUser.login)}`)
        .then(group => {

            openedGroup = group

            document.getElementById("dashboard").classList.add("hidden")
            document.getElementById("groupDetails").classList.remove("hidden")

            renderGroup(group)

            if (tg?.BackButton) {
                tg.BackButton.offClick(backToDashboard)
                tg.BackButton.onClick(backToDashboard)
                tg.BackButton.show()
            }
        })
        .catch(e => alert(e.message))
}

function refreshOpenedGroup() {
    if (!openedGroup) return
    openGroup(openedGroup.id)
}

function renderGroup(group) {

    document.getElementById("groupTitle").innerText = group.name

    const isOwner = group.ownerLogin === currentUser.login
    const me = group.members.find(m => m.login === currentUser.login)

    const actionEl = document.getElementById("groupAction")
    if (isOwner) {
        actionEl.innerHTML = `<button class="danger" onclick="deleteOpenedGroup()">Delete Group</button>`
    } else if (me) {
        actionEl.innerHTML = `<button class="danger" onclick="leaveOpenedGroup(${me.id})">Leave Group</button>`
    } else {
        actionEl.innerHTML = ""
    }

    const members = document.getElementById("groupMembers")
    const habits = document.getElementById("groupHabits")

    members.innerHTML = ""
    habits.innerHTML = ""

    group.members.forEach(m => {

        const div = document.createElement("div")
        div.className = "member"

        const removeButton =
            m.login === currentUser.login || !isOwner
                ? ""
                : `<button class="danger small" onclick="removeMemberFromOpenedGroup(${m.id})">Remove</button>`

        div.innerHTML = `
            <span>${escapeHtml(tgDisplay(m))}</span>
            ${removeButton}
        `

        members.appendChild(div)
    })

    if (group.habits.length === 0) {
        const empty = document.createElement("div")
        empty.className = "empty-state-inline"
        empty.innerText = "No habits in this group yet"
        habits.appendChild(empty)
    }

    group.habits.forEach(h => {

        const wrap = document.createElement("div")
        wrap.className = "group-habit"

        const checkedIn = !!h.checkedInToday
        const checkBtn = checkedIn
            ? `<button class="check-button done" disabled title="Already checked in today">✓ Today</button>`
            : `<button class="check-button" onclick="checkinGroupHabit(${h.id})">Check in</button>`

        // Top row: habit name, viewer's streak, check-in, delete.
        const header = document.createElement("div")
        header.className = "habit"
        header.innerHTML = `
            <span class="habit-name">${escapeHtml(h.habitName)}</span>
            <span class="streak" title="Your streak">🔥 ${h.streak ?? 0}</span>
            ${checkBtn}
            <button class="danger" onclick="deleteGroupHabit(${h.id})">
                Delete
            </button>
        `
        wrap.appendChild(header)

        // Per-member streak strip.
        const streaks = document.createElement("div")
        streaks.className = "member-streaks"
        const list = h.memberStreaks ?? []
        if (list.length === 0) {
            streaks.innerHTML = `<span class="empty-state-inline">No members yet</span>`
        } else {
            list.forEach(ms => {
                const item = document.createElement("div")
                item.className = "member-streak"
                if (ms.checkedInToday) item.classList.add("checked-in")
                if (ms.login === currentUser.login) item.classList.add("is-me")

                item.innerHTML = `
                    <span class="member-streak-name" title="${escapeHtml(ms.username)}">
                        ${escapeHtml(tgDisplay(ms))}
                    </span>
                    <span class="member-streak-value">🔥 ${ms.streak ?? 0}</span>
                    <span class="member-streak-status">${ms.checkedInToday ? "✓" : "·"}</span>
                `
                streaks.appendChild(item)
            })
        }
        wrap.appendChild(streaks)

        habits.appendChild(wrap)
    })
}

function addHabitToOpenedGroup() {

    if (!openedGroup) return

    const input = document.getElementById("groupHabitInput")
    const habitName = input.value

    if (!habitName) return

    api(`/groups/${openedGroup.id}/habits`, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ habitName })
    })
        .then(() => {
            input.value = ""
            refreshOpenedGroup()
        })
        .catch(e => alert(e.message))
}

let _searchTimer = null

function onGroupUserInput(e) {
    const query = e.target.value.trim()
    clearTimeout(_searchTimer)
    if (query.length < 2) { hideUserSearch(); return }
    _searchTimer = setTimeout(() => {
        api(`/user/search?query=${encodeURIComponent(query)}`)
            .then(showUserSearchResults)
            .catch(() => {})
    }, 250)
}

function showUserSearchResults(users) {
    const dropdown = document.getElementById("userSearchDropdown")
    dropdown.innerHTML = ""
    if (!users || users.length === 0) {
        dropdown.classList.add("hidden")
        return
    }
    users.forEach(u => {
        const item = document.createElement("div")
        item.className = "search-result-item"
        const initials = u.username.trim().split(" ")
            .map(w => w[0]).slice(0, 2).join("").toUpperCase()
        const primaryName = u.telegramUsername
            ? `@${escapeHtml(u.telegramUsername)}`
            : escapeHtml(u.username)
        const subtitle = u.telegramUsername
            ? `<span class="result-handle">${escapeHtml(u.username)}</span>`
            : ""
        item.innerHTML = `
            <div class="result-avatar">${escapeHtml(initials)}</div>
            <div class="result-info">
                <span class="result-name">${primaryName}</span>
                ${subtitle}
            </div>
        `
        item.onclick = () => {
            hideUserSearch()
            document.getElementById("groupUserInput").value = ""
            addUserToGroupByLogin(u.login)
        }
        dropdown.appendChild(item)
    })
    dropdown.classList.remove("hidden")
}

function hideUserSearch() {
    const d = document.getElementById("userSearchDropdown")
    if (d) d.classList.add("hidden")
}

function addUserToGroupByLogin(login) {
    if (!openedGroup) return
    api(`/groups/${openedGroup.id}/members`, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login })
    })
        .then(refreshOpenedGroup)
        .catch(e => alert(e.message))
}

function addMemberToOpenedGroup() {

    if (!openedGroup) return

    const input = document.getElementById("groupUserInput")
    const login = input.value.trim()

    if (!login) return

    hideUserSearch()
    addUserToGroupByLogin(login)
    input.value = ""
}

function removeMemberFromOpenedGroup(userId) {

    if (!openedGroup) return
    if (!confirm("Remove this member from the group?")) return

    api(`/groups/${openedGroup.id}/members/${userId}`, {
        method: "DELETE"
    })
        .then(refreshOpenedGroup)
        .catch(e => alert(e.message))
}

function deleteGroupHabit(habitId) {

    if (!confirm("Delete this group habit?")) return

    api(`/habits/${habitId}`, {
        method: "DELETE"
    })
        .then(refreshOpenedGroup)
        .catch(e => alert(e.message))
}

function checkinGroupHabit(habitId) {

    api(`/users/${currentUser.login}/habits/${habitId}/checkin`, {
        method: "POST"
    })
        .then(refreshOpenedGroup)
        .catch(e => alert(e.message))
}

function backToDashboard() {

    document.getElementById("groupDetails").classList.add("hidden")
    document.getElementById("dashboard").classList.remove("hidden")

    if (tg?.BackButton) {
        tg.BackButton.hide()
        tg.BackButton.offClick(backToDashboard)
    }

    openedGroup = null
    loadGroups()
}

/* ---------- VIEW ---------- */

function showLogin() {

    document.getElementById("loginBlock").classList.remove("hidden")
    document.getElementById("registerBlock").classList.add("hidden")
    document.getElementById("dashboard").classList.add("hidden")
    document.getElementById("groupDetails").classList.add("hidden")
}

function showRegister() {

    document.getElementById("loginBlock").classList.add("hidden")
    document.getElementById("registerBlock").classList.remove("hidden")
    document.getElementById("dashboard").classList.add("hidden")
    document.getElementById("groupDetails").classList.add("hidden")
}

function showDashboard() {

    document.getElementById("loginBlock").classList.add("hidden")
    document.getElementById("registerBlock").classList.add("hidden")
    document.getElementById("dashboard").classList.remove("hidden")
    document.getElementById("groupDetails").classList.add("hidden")

    document.getElementById("welcomeTitle").innerText =
        `Welcome, ${tgDisplay(currentUser)}`

    if (tg?.initData) {
        document.getElementById("logoutButton").classList.add("hidden")
    }
}

function logout() {

    currentUser = null
    openedGroup = null

    localStorage.removeItem("currentUser")

    showLogin()

    document.getElementById("habitList").innerHTML = ""
    document.getElementById("groupList").innerHTML = ""
}

/* ---------- UTIL ---------- */

/**
 * Returns "@handle" if the object has a telegramUsername, otherwise falls back
 * to the regular username field.  Works with currentUser, GroupMemberResponse,
 * MemberStreakResponse, and UserSearchResponse shapes.
 */
function tgDisplay(obj) {
    return obj?.telegramUsername ? '@' + obj.telegramUsername : (obj?.username ?? '')
}

function showToast(msg) {
    const t = document.createElement('div')
    t.className = 'toast'
    t.textContent = msg
    document.body.appendChild(t)
    setTimeout(() => t.remove(), 2500)
}

function escapeHtml(s) {
    if (s == null) return ""
    return String(s)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;")
}
