<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register Student – OASS | Manav Rachna University</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        /* Campus background – same as login */
        .auth-page {
            background-image: url('${pageContext.request.contextPath}/assets/img/campus.png'),
                              linear-gradient(135deg, #0F172A 0%, #1D4ED8 100%);
        }
        /* Glassmorphism override for the auth box */
        .auth-box {
            background: rgba(255, 255, 255, 0.15);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.25);
            max-width: 480px;
        }
        .auth-box, .auth-box label,
        .auth-box h1, .auth-box p,
        .auth-box .auth-links {
            color: #fff;
        }
        .auth-logo h1  { color: #fff; }
        .auth-logo p   { color: rgba(255,255,255,0.75); }
        .form-label    { color: rgba(255,255,255,0.90) !important; }
        .auth-links    { color: rgba(255,255,255,0.75); }
        .auth-links a  { color: #93C5FD; }
        .form-control {
            background: rgba(255,255,255,0.12);
            border: 1px solid rgba(255,255,255,0.30);
            color: #fff;
        }
        .form-control::placeholder { color: rgba(255,255,255,0.45); }
        .form-control:focus {
            background: rgba(255,255,255,0.20);
            border-color: rgba(255,255,255,0.60);
            outline: none;
            box-shadow: 0 0 0 3px rgba(255,255,255,0.15);
        }
    </style>
</head>
<body>
<div class="auth-page">
    <div class="auth-box-glass">
        <div class="auth-logo">
            <div class="logo-icon" style="font-weight:700; font-size:1.1rem;">MRU</div>
            <h1>Manav Rachna University</h1>
            <p>Student Registration</p>
        </div>

        <%-- Flash messages from session --%>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert alert-error"><c:out value="${sessionScope.flashError}"/></div>
            <c:remove var="flashError" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert alert-success"><c:out value="${sessionScope.flashSuccess}"/></div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>

        <form id="reg-student-form"
              action="${pageContext.request.contextPath}/register/student"
              method="POST"
              onsubmit="return validateStudentForm()">

            <div class="form-group">
                <label class="form-label" for="name">Full Name</label>
                <input type="text" id="name" name="name" class="form-control"
                       placeholder="e.g. Riya Sharma" required maxlength="100"
                       value="<c:out value="${param.name}"/>">
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email</label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="yourname@mru.edu" required maxlength="120"
                       value="<c:out value="${param.email}"/>">
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone (10 digits)</label>
                <input type="tel" id="phone" name="phone" class="form-control"
                       placeholder="9876543210" required maxlength="10" pattern="\d{10}"
                       value="<c:out value="${param.phone}"/>">
            </div>

            <div class="form-group">
                <label class="form-label" for="rollNo">Roll No (e.g. 2K24CSUN01099)</label>
                <input type="text" id="rollNo" name="rollNo" class="form-control"
                       placeholder="2K24CSUN01099" required maxlength="30"
                       value="<c:out value="${param.rollNo}"/>">
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="Create a password" required autocomplete="new-password">
            </div>

            <div class="form-group">
                <label class="form-label" for="confirmPassword">Confirm Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                       placeholder="Re-enter password" required autocomplete="new-password">
            </div>

            <button type="submit" class="btn btn-primary" style="width:100%;margin-top:.5rem;">
                Register as Student
            </button>
        </form>

        <div class="auth-links" style="margin-top:1rem;">
            <p>Already have an account? <a href="${pageContext.request.contextPath}/login">Login</a></p>
            <p style="margin-top:.4rem;">
                <a href="${pageContext.request.contextPath}/register/teacher">Register as Teacher</a>
            </p>
        </div>
    </div>
</div>

<script>
    /* Client-side validation: email domain, phone length, password match */
    function validateStudentForm() {
        var email   = document.getElementById('email').value.trim();
        var phone   = document.getElementById('phone').value.trim();
        var pwd     = document.getElementById('password').value;
        var confirm = document.getElementById('confirmPassword').value;
        var rollNo  = document.getElementById('rollNo').value.trim();

        if (!email.includes('@')) {
            alert('Please enter a valid email address.');
            return false;
        }
        if (!/^\d{10}$/.test(phone)) {
            alert('Phone must be exactly 10 digits.');
            return false;
        }
        if (!/^2K\d{2}[A-Z]{2,6}\d{5}$/.test(rollNo)) {
            alert('Roll No must match format like 2K24CSUN01099.');
            return false;
        }
        if (pwd !== confirm) {
            alert('Passwords do not match.');
            return false;
        }
        return true;
    }
</script>
</body>
</html>
