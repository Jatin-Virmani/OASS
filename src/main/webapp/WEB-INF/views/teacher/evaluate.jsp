<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="assignments" title="Evaluate Submission">
    <div class="page-header">
        <div>
            <h1 class="page-title">Evaluate: <c:out value="${sub.assignmentTitle}"/></h1>
            <p class="text-muted">
                <c:choose>
                    <c:when test="${sub.blindGrading}">Candidate #<c:out value="${sub.id}"/></c:when>
                    <c:otherwise><c:out value="${sub.studentName}"/> (<c:out value="${sub.rollNo}"/>)</c:otherwise>
                </c:choose>
                &bull; Receipt <code><c:out value="${sub.receiptNo}"/></code>
                &bull; Submitted <c:out value="${sub.submittedAt}"/>
                <c:if test="${sub.isLate}"> &bull; <span class="badge status-LATE">LATE</span></c:if>
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/teacher/submissions?id=${sub.assignmentId}" class="btn btn-secondary">Back</a>
    </div>

    <div class="chart-grid">
        <%-- ===== the student's work ===== --%>
        <div class="card">
            <div class="card-header">
                <span class="card-title">Submitted work</span>
                <c:if test="${not empty sub.storedName}">
                    <a href="${pageContext.request.contextPath}/teacher/submission/file?id=${sub.id}" class="btn btn-secondary btn-sm">Download file</a>
                </c:if>
            </div>
            <c:if test="${not empty sub.originalName}">
                <p class="text-small text-muted">File: <c:out value="${sub.originalName}"/></p>
            </c:if>
            <c:if test="${not empty sub.sha256}">
                <p class="text-small text-muted">SHA-256: <code><c:out value="${sub.sha256}"/></code></p>
            </c:if>
            <c:choose>
                <c:when test="${not empty preview}"><div class="text-preview"><c:out value="${preview}"/></div></c:when>
                <c:otherwise><div class="empty-state">No text could be read from this file. Use the download button.</div></c:otherwise>
            </c:choose>
        </div>

        <%-- ===== rubric form ===== --%>
        <div class="card">
            <div class="card-header"><span class="card-title">Rubric marks</span></div>
            <form action="${pageContext.request.contextPath}/teacher/submission/evaluate" method="POST">
                <input type="hidden" name="submissionId" value="${sub.id}">

                <c:forEach var="cr" items="${criteria}">
                    <div class="form-group">
                        <label class="form-label" for="score_${cr.id}"><c:out value="${cr.name}"/> (max <c:out value="${cr.maxMarks}"/>)</label>
                        <input type="number" id="score_${cr.id}" name="score_${cr.id}" class="form-control"
                               min="0" max="${cr.maxMarks}" step="0.5" value="<c:out value='${scores[cr.id]}'/>">
                    </div>
                </c:forEach>

                <div class="form-group">
                    <label class="form-label" for="feedback">Feedback for the student</label>
                    <textarea id="feedback" name="feedback" class="form-control" rows="4"><c:out value="${evaluation.feedback}"/></textarea>
                </div>

                <c:if test="${sub.isLate}">
                    <p class="hint-box">Late submission: the late penalty is calculated automatically when you save
                        (<c:out value="${sub.latePenaltyPctPerDay}"/>% per day, max <c:out value="${sub.maxLatePenaltyPct}"/>%).</p>
                </c:if>
                <c:if test="${not empty evaluation}">
                    <p class="hint-box">Saved: total <strong><c:out value="${evaluation.total}"/></strong>,
                        late penalty <strong><c:out value="${evaluation.latePenalty}"/></strong>,
                        final <strong><c:out value="${evaluation.finalMarks}"/></strong> of <c:out value="${sub.assignmentMax}"/>
                        &mdash; status <span class="badge ${evaluation.status == 'PUBLISHED' ? 'status-GRADED' : 'status-DRAFT'}"><c:out value="${evaluation.status}"/></span></p>
                </c:if>

                <div style="display:flex; gap:1rem; flex-wrap:wrap;">
                    <button type="submit" name="action" value="draft" class="btn btn-secondary">Save Draft</button>
                    <button type="submit" name="action" value="finalize" class="btn btn-primary">Finalize &amp; Publish</button>
                </div>
            </form>
        </div>
    </div>
</t:layout>
