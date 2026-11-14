<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="My Profile" activeNav="profile">
    <div class="page-header">
        <h1 class="page-title">Student Profile</h1>
    </div>
    
    <div style="display:grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
        <div class="card">
            <div class="card-header"><span class="card-title">Academic Details</span></div>
            <div class="form-group">
                <label class="form-label">Full Name</label>
                <input type="text" class="form-control" value="<c:out value='${sessionScope.user.name}'/>" readonly>
            </div>
            <div class="form-group">
                <label class="form-label">Email Address</label>
                <input type="text" class="form-control" value="<c:out value='${sessionScope.user.email}'/>" readonly>
            </div>
            <div class="form-group">
                <label class="form-label">Roll Number</label>
                <input type="text" class="form-control" value="<c:out value='${student.rollNo}'/>" readonly>
            </div>
            <div class="form-group">
                <label class="form-label">Class &amp; Section</label>
                <input type="text" class="form-control" value="<c:out value='${student.sectionName}'/>" readonly>
            </div>
        </div>

        <div class="card">
            <div class="card-header"><span class="card-title">Contact Information &amp; Settings</span></div>
            <form action="${pageContext.request.contextPath}/student/profile" method="POST">
                <div class="form-group">
                    <label class="form-label" for="phone">Phone Number</label>
                    <input type="text" id="phone" name="phone" class="form-control" value="<c:out value='${sessionScope.user.phone}'/>" placeholder="10-digit mobile number">
                </div>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </form>
        </div>
    </div>
</t:layout>
