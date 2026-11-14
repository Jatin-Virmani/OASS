<%@ tag description="Main app layout: topbar, sidebar, content area, flash messages" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ attribute name="title"    required="true"  type="java.lang.String" %>
<%@ attribute name="activeNav" required="false" type="java.lang.String" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${title}"/> – OASS | Manav Rachna University</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="role-${sessionScope.user.role}">
<div class="app-shell">

    <%-- ===== TOP BAR ===== --%>
    <header class="topbar">
        <a href="${pageContext.request.contextPath}/${sessionScope.user.role == 'STUDENT' ? 'student' : 'teacher'}/dashboard"
           class="topbar-brand">
            <div class="brand-icon" style="font-weight:700; font-size:0.85rem;">MRU</div>
            <div>
                Manav Rachna University
                <span class="brand-sub">Academic Submission Portal</span>
            </div>
        </a>

        <div class="topbar-right">
            <%-- Notifications with unread badge --%>
            <a href="${pageContext.request.contextPath}/${sessionScope.user.role == 'STUDENT' ? 'student' : 'teacher'}/notifications"
               class="bell-btn" title="Notifications" aria-label="Notifications">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                </svg>
                <c:if test="${sessionScope.unreadCount > 0}">
                    <span class="bell-badge"><c:out value="${sessionScope.unreadCount}"/></span>
                </c:if>
            </a>

            <%-- Avatar / name / logout --%>
            <div class="avatar-btn">
                <div class="avatar-circle">
                    <c:out value="${fn:substring(sessionScope.user.name, 0, 1)}"/>
                </div>
                <div>
                    <div><c:out value="${sessionScope.user.name}"/></div>
                    <div class="avatar-role"><c:out value="${sessionScope.user.role}"/></div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm">Logout</a>
        </div>
    </header>

    <div class="main-wrapper">
        <%-- ===== SIDEBAR ===== --%>
        <aside class="sidebar">
            <div class="sidebar-label">
                <c:out value="${sessionScope.user.role}"/> PORTAL
            </div>
            <nav>
                <c:choose>
                    <c:when test="${sessionScope.user.role == 'STUDENT'}">
                        <a href="${pageContext.request.contextPath}/student/dashboard"
                           class="nav-item ${activeNav == 'dashboard'    ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                            <span>Dashboard</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/student/assignments"
                           class="nav-item ${activeNav == 'assignments'  ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>
                            <span>My Assignments</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/student/submissions"
                           class="nav-item ${activeNav == 'submissions'  ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                            <span>Submissions</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/student/results"
                           class="nav-item ${activeNav == 'results'      ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                            <span>Grades &amp; Feedback</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/student/notifications"
                           class="nav-item ${activeNav == 'notifications'? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                            <span>Notifications</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/student/profile"
                           class="nav-item ${activeNav == 'profile'      ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                            <span>Profile</span>
                        </a>
                    </c:when>
                    <c:when test="${sessionScope.user.role == 'TEACHER'}">
                        <a href="${pageContext.request.contextPath}/teacher/dashboard"
                           class="nav-item ${activeNav == 'dashboard'    ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                            <span>Dashboard</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/assignments"
                           class="nav-item ${activeNav == 'assignments'  ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path></svg>
                            <span>Assignments</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/plagiarism"
                           class="nav-item ${activeNav == 'plagiarism'   ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                            <span>Plagiarism Check</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/questions"
                           class="nav-item ${activeNav == 'questions'    ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"></ellipse><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path></svg>
                            <span>Question Bank</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/requests"
                           class="nav-item ${activeNav == 'requests'     ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"></polyline><path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"></path></svg>
                            <span>Requests</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/announcements"
                           class="nav-item ${activeNav == 'announcements'? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path><line x1="12" y1="2" x2="12" y2="4"></line></svg>
                            <span>Announcements</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/notifications"
                           class="nav-item ${activeNav == 'notifications'? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                            <span>Notifications</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/teacher/profile"
                           class="nav-item ${activeNav == 'profile'      ? 'active' : ''}">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                            <span>Profile</span>
                        </a>
                    </c:when>
                </c:choose>
            </nav>
        </aside>

        <%-- ===== MAIN CONTENT ===== --%>
        <main class="content">
            <%-- Flash messages (shown once, then removed) --%>
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success"><c:out value="${sessionScope.flashSuccess}"/></div>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <div class="alert alert-error"><c:out value="${sessionScope.flashError}"/></div>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <jsp:doBody/>
        </main>
    </div><%-- main-wrapper --%>
</div><%-- app-shell --%>
<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</body>
</html>
