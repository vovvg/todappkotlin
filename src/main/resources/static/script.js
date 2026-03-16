
let currentUser=null

/* ---------- LOGIN ---------- */

function login(){

    const login=document.getElementById("loginLogin").value
    const password=document.getElementById("loginPassword").value

    fetch("/login",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({login,password})
    })
        .then(r=>r.json())
        .then(user=>{
            currentUser=user
            showDashboard()
            loadHabits()
        })
}

/* ---------- REGISTER ---------- */

function register(){

    const username=document.getElementById("regName").value
    const login=document.getElementById("regLogin").value
    const password=document.getElementById("regPassword").value

    fetch("/register",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({username,login,password})
    }).then(()=>showLogin())
}

/* ---------- HABITS ---------- */

function loadHabits(){

    fetch(`/user/${currentUser.login}/habits`)
        .then(r=>r.json())
        .then(displayHabits)
}

function displayHabits(habits){

    const list=document.getElementById("habitList")
    list.innerHTML=""

    habits.forEach(h=>{
        const div=document.createElement("div")
        div.className="habit"
        div.innerText=`${h.habitName} (streak ${h.streak})`
        list.appendChild(div)
    })
}

function addHabit(){

    const name=document.getElementById("habitName").value

    fetch("/user/habits/add",{
        method:"POST",
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
            habitName:name,
            login:currentUser.login
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