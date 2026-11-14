<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password – OASS | Manav Rachna University</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        /* Campus background – same as login */
        .auth-page {
            background-image: url('${pageContext.request.contextPath}/assets/img/campus.png'),
                              linear-gradient(135deg, #0F172A 0%, #1D4ED8 100%);
        }
        /* Glassmorphism override */
        .auth-box {
            background: rgba(255, 255, 255, 0.15);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.25);
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
        .step-hint {
            font-size: .82rem;
            color: rgba(255,255,255,0.65);
            margin-bottom: 1.2rem;
            text-align: center;
        }
    </style>
</head>
<body>
<div class="auth-page">
    <div class="auth-box-glass">
        <div class="auth-logo">
            <div class="logo-icon" style="font-weight:700; font-size:1.1rem;">MRU</div>
            <h1>Manav Rachna University</h1>
            <p>Password Recovery</p>
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

        <%-- 2-step flow: step 1 = enter email, step 2 = enter OTP + new password --%>
        <c:choose>
            <c:when test="${step == 'otp'}">
                <%-- Step 2: OTP + new password --%>
                <p class="step-hint">Step 2 of 2 – Check your email (or server log) for the OTP.</p>
                <form action="${pageContext.request.contextPath}/forgot-password?action=reset"
                      method="POST"
                      onsubmit="return validateResetForm()">

                    <%-- Pass email along so the servlet can look up the OTP --%>
                    <input type="hidden" name="email" value="<c:out value="${email}"/>">

                    <div class="form-group">
                        <label class="form-label" for="otp">6-Digit OTP</label>
                        <input type="text" id="otp" name="otp" class="form-control"
                               placeholder="e.g. 482910" required maxlength="6"
                               pattern="\d{6}" autocomplete="one-time-code">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="newPassword">New Password</label>
                        <input type="password" id="newPassword" name="newPassword" class="form-control"
                               placeholder="Enter new password" required autocomplete="new-password">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="confirmPassword">Confirm New Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                               placeholder="Re-enter new password" required autocomplete="new-password">
                    </div>

                    <button type="submit" class="btn btn-primary" style="width:100%;margin-top:.5rem;">
                        Reset Password
                    </button>
                </form>
            </c:when>
            <c:otherwise>
                <%-- Step 1: Enter email --%>
                <p class="step-hint">Step 1 of 2 – Enter your registered email to receive an OTP.</p>
                <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
                    <div class="form-group">
                        <label class="form-label" for="email">Registered Email</label>
                        <input type="email" id="email" name="email" class="form-control"
                               placeholder="yourname@mru.edu" required maxlength="120"
                               autocomplete="email">
                    </div>
                    <button type="submit" class="btn btn-primary" style="width:100%;margin-top:.5rem;">
                        Send OTP
                    </button>
                </form>
            </c:otherwise>
        </c:choose>

        <div class="auth-links" style="margin-top:1rem;">
            <p><a href="${pageContext.request.contextPath}/login">← Back to Login</a></p>
        </div>
    </div>
</div>

<script>
    /* Validate OTP form: passwords must match */
    function validateResetForm() {
        var pwd     = document.getElementById('newPassword').value;
        var confirm = document.getElementById('confirmPassword').value;
        if (pwd !== confirm) {
            alert('Passwords do not match.');
            return false;
        }
        return true;
    }
</script>
</body>
</html>
