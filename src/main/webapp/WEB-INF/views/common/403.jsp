<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 Forbidden – OASS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div style="display:flex;align-items:center;justify-content:center;min-height:100vh;background:var(--bg);">
    <div style="text-align:center;padding:2rem;">
        <div style="font-size:4rem;font-weight:700;color:var(--muted);">403</div>
        <h1 style="font-size:2rem;margin:.5rem 0;">Access Denied</h1>
        <p style="color:var(--muted);">You do not have permission to view this page.</p>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="margin-top:1.5rem;">
            Go to Login
        </a>
    </div>
</div>
</body>
</html>
