<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login – OASS | Manav Rachna University</title>
    <meta name="description" content="Login to the Manav Rachna University Academic Submission Portal">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        /* Campus photo background if the image exists; falls back to the CSS gradient */
        .auth-page {
            background-image: url('${pageContext.request.contextPath}/assets/img/campus.png'),
                              linear-gradient(135deg, #0F172A 0%, #1D4ED8 100%);
        }
    </style>
</head>
<body>
<div class="auth-page">
    <div class="auth-box-glass">
        <div class="auth-logo">
            <div class="logo-icon" style="font-weight:700; font-size:1.1rem;">MRU</div>
            <h1>Manav Rachna University</h1>
            <p>Academic Submission Portal</p>
        </div>

        <%-- Flash messages --%>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert alert-error"><c:out value="${sessionScope.flashError}"/></div>
            <c:remove var="flashError" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert alert-success"><c:out value="${sessionScope.flashSuccess}"/></div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>

        <form id="login-form" action="${pageContext.request.contextPath}/login" method="POST">
            <div class="form-group">
                <label class="form-label" for="email">Email / Username</label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="your@mru.edu" required autocomplete="email">
            </div>
            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="Enter password" required autocomplete="current-password">
            </div>
            <button type="submit" id="login-btn" class="btn btn-primary" style="width:100%">
                Login
            </button>
        </form>

        <div class="auth-links">
            <p><a href="${pageContext.request.contextPath}/forgot-password">Forgot Password?</a></p>
            <p style="margin-top:.5rem;">
                New user?
                <a href="${pageContext.request.contextPath}/register/student">Register as Student</a>
                &nbsp;|&nbsp;
                <a href="${pageContext.request.contextPath}/register/teacher">Register as Teacher</a>
            </p>
        </div>
    </div>
</div>
</body>
</html>
