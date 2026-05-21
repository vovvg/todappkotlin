let currentUser = null
let openedGroup = null

document.addEventListener('DOMContentLoaded', () => {

    const savedUser =
        localStorage.getItem('currentUser')

    if (savedUser) {

        currentUser = JSON.parse(savedUser)

        showDashboard()
        loadAll()
    }
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

        div.innerHTML = `
            <strong>${escapeHtml(group.name)}</strong>
            <span>${group.members.length} members</span>
            <button onclick="openGroup(${group.id})">
                Open
            </button>
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

function openGroup(groupId) {

    // Pass userLogin so the response carries this user's streak per habit.
    api(`/groups/${groupId}?userLogin=${encodeURIComponent(currentUser.login)}`)
        .then(group => {

            openedGroup = group

            document.getElementById("dashboard").classList.add("hidden")
            document.getElementById("groupDetails").classList.remove("hidden")

            renderGroup(group)
        })
        .catch(e => alert(e.message))
}

function refreshOpenedGroup() {
    if (!openedGroup) return
    openGroup(openedGroup.id)
}

function renderGroup(group) {

    document.getElementById("groupTitle").innerText = group.name

    const members = document.getElementById("groupMembers")
    const habits = document.getElementById("groupHabits")

    members.innerHTML = ""
    habits.innerHTML = ""

    group.members.forEach(m => {

        const div = document.createElement("div")
        div.className = "member"

        const removeButton =
            m.login === currentUser.login
                ? "" // can't remove yourself from the dashboard view
                : `<button class="danger small" onclick="removeMemberFromOpenedGroup(${m.id})">Remove</button>`

        div.innerHTML = `
            <span>${escapeHtml(m.username)} (${escapeHtml(m.login)})</span>
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
                        ${escapeHtml(ms.login)}
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

function addMemberToOpenedGroup() {

    if (!openedGroup) return

    const input = document.getElementById("groupUserInput")
    const login = input.value

    if (!login) return

    api(`/groups/${openedGroup.id}/members`, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login })
    })
        .then(() => {
            input.value = ""
            refreshOpenedGroup()
        })
        .catch(e => alert(e.message))
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
        `Welcome, ${currentUser.username}`
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

function escapeHtml(s) {
    if (s == null) return ""
    return String(s)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;")
}
