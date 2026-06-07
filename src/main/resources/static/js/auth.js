// ===============================
// API HELPER
// ===============================

function apiRequest(endpoint, method, body = null) {

    const headers = {
        "Content-Type": "application/json"
    };

    const token = localStorage.getItem("token");

    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    return fetch("/api" + endpoint, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });
}


// ===============================
// REGISTER
// ===============================

function register() {

    apiRequest("/auth/register", "POST", {
        name: name.value,
        email: email.value,
        password: password.value,
        role: role.value
    })
        .then(res => res.json())
        .then(data => {

            if (data.error) {
                msg.innerText = data.error;
                return;
            }

            alert("Registration successful!");
            window.location.href = "login.html";
        })
        .catch(err => {
            console.error(err);
            msg.innerText = "Registration failed";
        });
}


// ===============================
// LOGIN
// ===============================

function login() {

    apiRequest("/auth/login", "POST", {
        email: email.value,
        password: password.value
    })
        .then(res => res.json())
        .then(data => {

            if (data.token) {

                localStorage.setItem("token", data.token);

                const payload = decodeToken(data.token);

                const role = payload.authorities[0];

                if (role === "ROLE_ADMIN") {
                    window.location.href = "admin-dashboard.html";
                } else {
                    window.location.href = "dashboard.html";
                }

            } else {
                msg.innerText = data.error || "Login failed";
            }
        })
        .catch(err => {
            console.error(err);
            msg.innerText = "Login failed";
        });
}


// ===============================
// LOGOUT
// ===============================

function logout() {

    localStorage.removeItem("token");

    window.location.href = "login.html";
}


// ===============================
// JWT HELPERS
// ===============================

function getToken() {
    return localStorage.getItem("token");
}

function decodeToken(token) {

    try {
        return JSON.parse(
            atob(token.split('.')[1])
        );
    } catch (e) {
        console.error("Invalid token");
        return null;
    }
}

function getUserRole() {

    const token = getToken();

    if (!token) {
        return null;
    }

    const payload = decodeToken(token);

    return payload?.authorities?.[0] || null;
}

function getUserName() {

    const token = getToken();

    if (!token) {
        return null;
    }

    const payload = decodeToken(token);

    return payload?.name || null;
}

function isLoggedIn() {

    return !!getToken();
}


// ===============================
// PAGE PROTECTION
// ===============================

function requireLogin() {

    if (!isLoggedIn()) {
        window.location.href = "login.html";
    }
}

function requireUser() {

    requireLogin();

    const role = getUserRole();

    if (role !== "ROLE_USER") {
        window.location.href = "admin-dashboard.html";
    }
}

function requireAdmin() {

    requireLogin();

    const role = getUserRole();

    if (role !== "ROLE_ADMIN") {
        window.location.href = "dashboard.html";
    }
}


// ===============================
// OPTIONAL
// AUTO REDIRECT IF ALREADY LOGGED IN
// ===============================

function redirectIfLoggedIn() {

    if (!isLoggedIn()) {
        return;
    }

    const role = getUserRole();

    if (role === "ROLE_ADMIN") {
        window.location.href = "admin-dashboard.html";
    } else {
        window.location.href = "dashboard.html";
    }
}