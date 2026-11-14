<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 Server Error – OASS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<div style="display:flex;align-items:center;justify-content:center;min-height:100vh;background:var(--bg);">
    <div style="text-align:center;padding:2rem;">
        <div style="font-size:4rem;font-weight:700;color:var(--muted);">500</div>
        <h1 style="font-size:2rem;margin:.5rem 0;">Something Went Wrong</h1>
        <p style="color:var(--muted);">An internal error occurred. Please try again later.</p>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="margin-top:1.5rem;">
            Go to Login
        </a>
    </div>
</div>
</body>
</html>
