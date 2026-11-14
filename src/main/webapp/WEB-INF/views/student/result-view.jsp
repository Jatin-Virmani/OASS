<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Result" activeNav="results">
    <div class="page-header">
        <div>
            <h1 class="page-title"><c:out value="${result.assignmentTitle}"/></h1>
            <p class="text-muted">
                <span class="badge badge-indigo"><c:out value="${result.subjectCode}"/></span>
                &bull; Receipt <code><c:out value="${result.receiptNo}"/></code> (version <c:out value="${result.versionNo}"/>)
                &bull; Submitted <c:out value="${result.submittedAt}"/>
                <c:if test="${result.isLate}"> &bull; <span class="badge badge-orange">Late</span></c:if>
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/student/results" class="btn btn-secondary">Back</a>
    </div>

    <div class="stat-cards">
        <div class="stat-card"><div class="stat-card-label">Final Marks</div>
            <div class="stat-card-value"><c:out value="${result.finalMarks}"/> / <c:out value="${result.maxMarks}"/></div></div>
        <div class="stat-card"><div class="stat-card-label">Marks Before Penalty</div>
            <div class="stat-card-value"><c:out value="${result.total}"/></div></div>
        <div class="stat-card"><div class="stat-card-label">Late Penalty</div>
            <div class="stat-card-value">-<c:out value="${result.latePenalty}"/></div></div>
    </div>

    <div class="card mb-2">
        <div class="card-header"><span class="card-title">Rubric breakdown</span></div>
        <c:forEach var="s" items="${scores}">
            <div class="score-line">
                <span><c:out value="${s.name}"/></span>
                <strong><c:out value="${s.marks}"/> / <c:out value="${s.maxMarks}"/></strong>
            </div>
        </c:forEach>
    </div>

    <div class="card mb-2">
        <div class="card-header"><span class="card-title">Teacher feedback</span></div>
        <p style="white-space:pre-wrap;"><c:out value="${empty result.feedback ? 'No written feedback.' : result.feedback}"/></p>
        <p class="text-small text-muted">Published <c:out value="${result.publishedAt}"/></p>
    </div>

    <a href="${pageContext.request.contextPath}/student/regrade/request?evalId=${result.evalId}" class="btn btn-secondary">Appeal Regrade</a>
</t:layout>
