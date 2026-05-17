let currentUser = null
let openedGroup = null

document.addEventListener('DOMContentLoaded', () => {

    const savedUser =
        localStorage.getItem('currentUser')

    if(savedUser){

        currentUser = JSON.parse(savedUser)

        showDashboard()
        loadAll()
    }
})

/* ---------- HELPERS ---------- */

function api(url, options = {}) {

    return fetch(url, options)
        .then(async r => {

            if(!r.ok){

                const text = await r.text()
                throw new Error(text || "Request failed")
            }

            if(r.status === 204){
                return null
            }

            return r.json()
        })
}

function loadAll(){
    loadHabits()
    loadGroups()
}

/* ---------- LOGIN ---------- */

function login(){

    const login =
        document.getElementById("loginLogin").value

    const password =
        document.getElementById("loginPassword").value

    api("/user/login",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({login,password})
    })
        .then(user=>{

            currentUser=user

            localStorage.setItem(
                "currentUser",
                JSON.stringify(user)
            )

            showDashboard()
            loadAll()
        })
        .catch(e=>alert(e.message))
}

/* ---------- REGISTER ---------- */

function register(){

    const username =
        document.getElementById("regName").value

    const login =
        document.getElementById("regLogin").value

    const password =
        document.getElementById("regPassword").value

    api("/user/register",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
            username,
            login,
            password
        })
    })
        .then(()=>{
            alert("Registered successfully")
            showLogin()
        })
        .catch(e=>alert(e.message))
}

/* ---------- HABITS ---------- */

function loadHabits(){

    api(`/users/${currentUser.login}/habits`)
        .then(displayHabits)
}

function displayHabits(habits){

    const list =
        document.getElementById("habitList")

    const empty =
        document.getElementById("emptyState")

    list.innerHTML = ""

    if(!habits || habits.length === 0){
        empty.style.display="block"
        return
    }

    empty.style.display="none"

    habits.forEach(h=>{

        const div =
            document.createElement("div")

        div.className="habit"

        div.innerHTML = `
            <span>${h.habitName}</span>
            <button onclick="deleteHabit(${h.id})">
                Delete
            </button>
        `

        list.appendChild(div)
    })
}

function addHabit(){

    const habitName =
        document.getElementById("habitName").value

    if(!habitName){
        return
    }

    api(`/users/${currentUser.login}/habits`,{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({habitName})
    })
        .then(()=>{

            document.getElementById(
                "habitName"
            ).value=""

            loadHabits()
        })
        .catch(e=>alert(e.message))
}

function deleteHabit(habitId){

    api(`/habits/${habitId}`,{
        method:"DELETE"
    })
        .then(loadHabits)
        .catch(e=>alert(e.message))
}

/* ---------- GROUPS ---------- */

function loadGroups(){

    api(`/users/${currentUser.login}/groups`)
        .then(displayGroups)
}

function displayGroups(groups){

    const list =
        document.getElementById(
            "groupList"
        )

    list.innerHTML=""

    groups.forEach(group=>{

        const div =
            document.createElement("div")

        div.className="group"

        div.innerHTML=`

            <strong>${group.name}</strong>

            <span>
                ${group.members.length}
                members
            </span>

            <button
                onclick="openGroup(${group.id})">
                Open
            </button>
        `

        list.appendChild(div)
    })
}

function addGroupHabit(groupId){

    const input =
        document.getElementById(
            `groupHabit-${groupId}`
        )

    const habitName = input.value

    if(!habitName){
        return
    }

    api(`/groups/${groupId}/habits`,{
        method:"POST",
        headers:{
            'Content-Type':'application/json'
        },
        body:JSON.stringify({
            habitName
        })
    })
        .then(()=>{
            input.value=""
            loadGroups()
        })
        .catch(e=>alert(e.message))
}

function createGroup(){

    const name =
        document.getElementById("groupName").value

    if(!name){
        return
    }

    api("/groups",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
            name,
            creatorLogin:currentUser.login
        })
    })
        .then(()=>{

            document.getElementById(
                "groupName"
            ).value=""

            loadGroups()
        })
        .catch(e=>alert(e.message))
}

function addUserToGroup(groupId){

    const input =
        document.getElementById(
            `addUser-${groupId}`
        )

    const login = input.value

    if(!login){
        return
    }

    api(`/groups/${groupId}/members`,{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({login})
    })
        .then(()=>{
            input.value=""
            loadGroups()
        })
        .catch(e=>alert(e.message))
}
function openGroup(groupId){

    fetch(`/groups/${groupId}`)
        .then(r=>r.json())
        .then(group=>{

            openedGroup = group

            dashboard.classList.add("hidden")
            groupDetails.classList.remove("hidden")

            renderGroup(group)
        })
}
function renderGroup(group){

    document.getElementById(
        "groupTitle"
    ).innerText = group.name

    const members =
        document.getElementById(
            "groupMembers"
        )

    const habits =
        document.getElementById(
            "groupHabits"
        )

    members.innerHTML=""
    habits.innerHTML=""

    group.members.forEach(m=>{

        const div =
            document.createElement("div")

        div.className="member"

        div.innerText =
            `${m.username} (${m.login})`

        members.appendChild(div)
    })

    group.habits.forEach(h=>{

        const div =
            document.createElement("div")

        div.className="habit"

        div.innerText =
            h.habitName

        habits.appendChild(div)
    })
}

function backToDashboard(){

    groupDetails.classList.add("hidden")
    dashboard.classList.remove("hidden")

    loadGroups()
}

/* ---------- VIEW ---------- */

function showLogin(){

    loginBlock.classList.remove("hidden")
    registerBlock.classList.add("hidden")
    dashboard.classList.add("hidden")
}

function showRegister(){

    loginBlock.classList.add("hidden")
    registerBlock.classList.remove("hidden")
    dashboard.classList.add("hidden")
}

function showDashboard(){

    loginBlock.classList.add("hidden")
    registerBlock.classList.add("hidden")
    dashboard.classList.remove("hidden")

    document.getElementById(
        "welcomeTitle"
    ).innerText =
        `Welcome, ${currentUser.username}`
}

function logout(){

    currentUser = null

    localStorage.removeItem(
        "currentUser"
    )

    showLogin()

    document.getElementById(
        "habitList"
    ).innerHTML=""

    document.getElementById(
        "groupList"
    ).innerHTML=""
}