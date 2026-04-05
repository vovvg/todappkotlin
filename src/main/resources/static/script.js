
let currentUser = null

document.addEventListener('DOMContentLoaded', function() {
    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
        currentUser = JSON.parse(savedUser)
        showDashboard()
        loadHabits()
    }
})

/* ---------- LOGIN ---------- */

function login(){

    const login=document.getElementById("loginLogin").value
    const password=document.getElementById("loginPassword").value

    fetch("/user/login",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({login,password})
    })
        .then(r=>r.json())
        .then(user=>{
            currentUser=user
            // Сохраняем пользователя в localStorage
            localStorage.setItem('currentUser', JSON.stringify(user))
            showDashboard()
            loadHabits()
        })
}

/* ---------- REGISTER ---------- */

function register(){

    const username=document.getElementById("regName").value
    const login=document.getElementById("regLogin").value
    const password=document.getElementById("regPassword").value

    fetch("/user/register",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({username,login,password})
    }).then(()=>showLogin())
}

/* ---------- HABITS ---------- */

function loadHabits(){

    fetch(`/habits/${currentUser.login}`)
        .then(r=>r.json())
        .then(displayHabits)
}

function displayHabits(habits){

    const list=document.getElementById("habitList")
    const emptyState = document.getElementById("emptyState")
    
    list.innerHTML=""
    
    if (habits && habits.length > 0) {
        emptyState.style.display = "none"
        
        habits.forEach(h=>{
            const div=document.createElement("div")
            div.className="habit"

            const habitText = document.createElement("span")
            habitText.innerText = `${h.habitName} (streak ${h.streak})`
            div.appendChild(habitText)

            const deleteButton = document.createElement("span")
            deleteButton.className = "delete-button"
            deleteButton.innerText = "❌"
            deleteButton.onclick = function() { deleteHabitById(h.id) }
            div.appendChild(deleteButton)
            
            list.appendChild(div)
        })
    } else {
        emptyState.style.display = "block"
    }
}

function deleteHabitById(habitId){
    fetch("/habits/delete",{
        method:"DELETE",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
            habitId: habitId,
            login: currentUser.login
        })
    })
    .then(loadHabits)
}

function addHabit(){

    const name=document.getElementById("habitName").value

    fetch("/habits/add",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
            habitName:name,
            login:currentUser.login
        })
    })
        .then(loadHabits)
}

function deleteHabit(){
    const habitId = document.getElementById("habitIdToDelete").value

    fetch("/habits/delete",{
        method:"DELETE",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
            habitId: parseInt(habitId),
            login: currentUser.login
        })
    })
    .then(loadHabits)
}

/* ---------- VIEW SWITCH ---------- */

function showLogin(){
    loginBlock.classList.remove("hidden")
    registerBlock.classList.add("hidden")
    dashboard.classList.add("hidden")
}

function showRegister(){
    loginBlock.classList.add("hidden")
    registerBlock.classList.remove("hidden")
}

function showDashboard(){
    loginBlock.classList.add("hidden")
    registerBlock.classList.add("hidden")
    dashboard.classList.remove("hidden")
}

function logout(){
    currentUser = null
    localStorage.removeItem('currentUser')
    
    showLogin()
    
    document.getElementById('habitList').innerHTML = ''
}