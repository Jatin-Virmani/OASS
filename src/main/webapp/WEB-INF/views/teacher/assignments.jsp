<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="assignments" title="Manage Assignments">
    <div class="page-header">
        <h1 class="page-title">Manage Assignments</h1>
        <a href="${pageContext.request.contextPath}/teacher/assignment/create" class="btn btn-primary">Create Assignment</a>
    </div>

    <div class="card">
        <c:choose>
        <c:when test="${empty assignments}">
            <div class="empty-state">You have not created any assignment yet.</div>
        </c:when>
        <c:otherwise>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Title</th><th>Subject</th><th>Section</th><th>Deadline</th><th>Status</th><th>Submissions</th><th>Action</th></tr></thead>
                <tbody>
                    <c:forEach var="a" items="${assignments}">
                        <tr>
                            <td><strong><c:out value="${a.title}"/></strong></td>
                            <td><c:out value="${a.subjectCode}"/></td>
                            <td><c:out value="${a.sectionName}"/></td>
                            <td><c:out value="${a.deadline}"/></td>
                            <td><span class="badge status-${a.status}"><c:out value="${a.status}"/></span></td>
                            <td><c:out value="${a.subCount}"/> / <c:out value="${a.totalStudents}"/></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/teacher/submissions?id=${a.id}" class="btn btn-secondary btn-sm">Submissions</a>
                                <a href="${pageContext.request.contextPath}/teacher/marks/export?id=${a.id}" class="btn btn-secondary btn-sm">Export CSV</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        </c:otherwise>
        </c:choose>
    </div>
</t:layout>
