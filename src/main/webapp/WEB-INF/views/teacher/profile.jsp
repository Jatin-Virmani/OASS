<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="profile" title="Profile" >
    <div class="page-header"><h1 class="page-title">Profile</h1></div>
    <div class="card" style="max-width: 600px;">
        <p><strong>Name:</strong> <c:out value="${sessionScope.user.name}"/></p>
        <p><strong>Email:</strong> <c:out value="${sessionScope.user.email}"/></p>
        <p><strong>Role:</strong> <c:out value="${sessionScope.user.role}"/></p>
        <h3 style="margin-top: 2rem;">Change Password</h3>
        <form action="${pageContext.request.contextPath}/teacher/profile" method="POST">
            <div class="form-group"><label class="form-label">Current Password</label><input type="password" name="oldpass" class="form-control" required></div>
            <div class="form-group"><label class="form-label">New Password</label><input type="password" name="newpass" class="form-control" required></div>
            <button type="submit" class="btn btn-primary">Update Password</button>
        </form>
    </div>
</t:layout>
