// ===============================
// Authentication Guard
// ===============================

function getTokenPayload() {
    const token = localStorage.getItem("token");

    if (!token) {
        return null;
    }

    try {
        return JSON.parse(atob(token.split(".")[1]));
    } catch (e) {
        console.error("Invalid token:", e);
        return null;
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "/pages/login.html";
}

// ===============================
// General Authentication Check
// ===============================

function requireAuth() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/pages/login.html";
        return;
    }

    try {
        const payload = JSON.parse(atob(token.split(".")[1]));

        if (payload.exp && Date.now() > payload.exp * 1000) {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            window.location.href = "/pages/login.html";
        }
    } catch (e) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.href = "/pages/login.html";
    }
}

// ===============================
// User Only Pages
// ===============================

function requireUser() {
    requireAuth();

    const payload = getTokenPayload();

    if (!payload) {
        window.location.href = "/pages/login.html";
        return;
    }

    const roles =
        payload.authorities ||
        payload.roles ||
        (payload.role ? [payload.role] : []);

    if (!roles.includes("ROLE_USER")) {
        alert("User access only");
        window.location.href = "/pages/admin-dashboard.html";
    }
}

// ===============================
// Admin Only Pages
// ===============================

function requireAdmin() {
    requireAuth();

    const payload = getTokenPayload();

    if (!payload) {
        window.location.href = "/pages/login.html";
        return;
    }

    const roles =
        payload.authorities ||
        payload.roles ||
        (payload.role ? [payload.role] : []);

    if (!roles.includes("ROLE_ADMIN")) {
        alert("Admin access only");
        window.location.href = "/pages/dashboard.html";
    }
}

// ===============================
// Current User Helpers
// ===============================

function getCurrentUserName() {
    const payload = getTokenPayload();
    return payload?.name || "User";
}

function getCurrentUserEmail() {
    const payload = getTokenPayload();
    return payload?.sub || "";
}

function getCurrentUserRole() {
    const payload = getTokenPayload();

    const roles =
        payload?.authorities ||
        payload?.roles ||
        (payload?.role ? [payload.role] : []);

    return roles.length > 0 ? roles[0] : null;
}