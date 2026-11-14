<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Notifications" activeNav="notifications">
    <div class="page-header">
        <h1 class="page-title">Notifications</h1>
        <form action="${pageContext.request.contextPath}/student/notifications" method="GET">
            <button type="submit" class="btn btn-secondary btn-sm">Refresh</button>
        </form>
    </div>
    
    <div class="card">
        <c:choose>
            <c:when test="${not empty notifications}">
                <c:forEach var="n" items="${notifications}">
                    <div class="notif-item">
                        <div class="notif-dot ${n.read ? 'read' : ''}"></div>
                        <div style="flex:1;">
                            <div class="notif-title"><c:out value="${n.title}"/></div>
                            <div class="notif-msg"><c:out value="${n.message}"/></div>
                            <div class="notif-time"><c:out value="${n.createdAtText}"/></div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-state">No notifications. You are all caught up!</div>
            </c:otherwise>
        </c:choose>
    </div>
</t:layout>
