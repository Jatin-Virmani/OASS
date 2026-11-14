<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="announcements" title="Announcements">
    <div class="page-header"><h1 class="page-title">Announcements</h1></div>

    <c:choose>
    <c:when test="${empty sections}">
        <div class="card"><div class="empty-state">No section is assigned to you yet, so you cannot post announcements.</div></div>
    </c:when>
    <c:otherwise>
    <div class="card mb-2">
        <form action="${pageContext.request.contextPath}/teacher/announcements" method="POST">
            <div class="form-group">
                <label class="form-label" for="section">Section</label>
                <select id="section" name="section" class="form-control" required>
                    <c:forEach var="s" items="${sections}"><option value="${s.id}"><c:out value="${s.name}"/></option></c:forEach>
                </select>
            </div>
            <div class="form-group"><label class="form-label" for="title">Title</label>
                <input type="text" id="title" name="title" class="form-control" maxlength="150" required></div>
            <div class="form-group"><label class="form-label" for="body">Message</label>
                <textarea id="body" name="body" class="form-control" rows="4" required></textarea></div>
            <button type="submit" class="btn btn-primary">Post Announcement</button>
        </form>
    </div>
    </c:otherwise>
    </c:choose>

    <div class="card">
        <div class="card-header"><span class="card-title">Posted by me</span></div>
        <c:choose>
        <c:when test="${empty announcements}"><div class="empty-state">You have not posted any announcement yet.</div></c:when>
        <c:otherwise>
            <c:forEach var="an" items="${announcements}">
                <div class="score-line" style="display:block;">
                    <strong><c:out value="${an.title}"/></strong>
                    <span class="text-muted text-small"> &bull; <c:out value="${an.sectionName}"/> &bull; <c:out value="${an.createdAt}"/></span>
                    <p style="white-space:pre-wrap; margin-top:.25rem;"><c:out value="${an.body}"/></p>
                </div>
            </c:forEach>
        </c:otherwise>
        </c:choose>
    </div>
</t:layout>
