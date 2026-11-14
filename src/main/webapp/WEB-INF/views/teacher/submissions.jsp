<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="assignments" title="Submissions">
    <div class="page-header">
        <div>
            <h1 class="page-title">Submissions: <c:out value="${assignment.title}"/></h1>
            <c:if test="${assignment.blindGrading}"><p class="text-muted">Blind grading is on: student names are hidden.</p></c:if>
        </div>
        <div class="d-flex gap-1">
            <a href="${pageContext.request.contextPath}/teacher/marks/export?id=${assignment.id}" class="btn btn-secondary">Export CSV</a>
            <a href="${pageContext.request.contextPath}/teacher/assignments" class="btn btn-secondary">Back</a>
        </div>
    </div>

    <div class="card">
        <c:choose>
        <c:when test="${empty rows}">
            <div class="empty-state">No student has submitted yet.</div>
        </c:when>
        <c:otherwise>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Student</th><th>Receipt</th><th>Submitted At</th><th>Timing</th><th>Evaluation</th><th>Marks</th><th>Action</th></tr></thead>
                <tbody>
                    <c:forEach var="r" items="${rows}">
                        <tr>
                            <td>
                                <c:choose>
                                    <c:when test="${assignment.blindGrading}">Candidate #<c:out value="${r.id}"/></c:when>
                                    <c:otherwise><c:out value="${r.studentName}"/> <span class="text-muted text-small">(<c:out value="${r.rollNo}"/>)</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><code><c:out value="${r.receiptNo}"/></code></td>
                            <td><c:out value="${r.submittedAt}"/></td>
                            <td><span class="badge ${r.isLate ? 'status-LATE' : 'status-SUBMITTED'}">${r.isLate ? 'LATE' : 'ON TIME'}</span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.evalStatus == 'PUBLISHED'}"><span class="badge status-GRADED">GRADED</span></c:when>
                                    <c:when test="${r.evalStatus == 'DRAFT'}"><span class="badge status-DRAFT">DRAFT</span></c:when>
                                    <c:otherwise><span class="badge status-PENDING">NOT GRADED</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${r.evalStatus == 'PUBLISHED' ? r.finalMarks : '-'}"/> / <c:out value="${assignment.maxMarks}"/></td>
                            <td><a href="${pageContext.request.contextPath}/teacher/submission/evaluate?id=${r.id}" class="btn btn-primary btn-sm">Evaluate</a></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        </c:otherwise>
        </c:choose>
    </div>
</t:layout>
