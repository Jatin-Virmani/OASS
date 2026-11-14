<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="plagiarism" title="Plagiarism Comparison">
    <div class="page-header">
        <div>
            <h1 class="page-title">Side-by-Side Comparison</h1>
            <p class="text-muted"><c:out value="${pair.assignmentTitle}"/></p>
        </div>
        <a href="${pageContext.request.contextPath}/teacher/plagiarism" class="btn btn-secondary">Back to Dashboard</a>
    </div>

    <div class="card mb-2">
        <div class="card-header">
            <span class="card-title">Similarity Metric Breakdown</span>
            <span class="badge ${pair.risk == 'HIGH' ? 'badge-red' : (pair.risk == 'MEDIUM' ? 'badge-amber' : 'badge-green')}">
                <c:out value="${pair.risk}"/> RISK
            </span>
        </div>
        <div style="display:flex; gap:2.5rem; flex-wrap:wrap;">
            <div><strong>Overall Match:</strong> <span style="font-size:1.2rem; font-weight:700;"><c:out value="${pair.score}"/>%</span></div>
            <div><strong>3-Word Shingle Jaccard:</strong> <c:out value="${pair.jaccard}"/>%</div>
            <div><strong>Vector Cosine:</strong> <c:out value="${pair.cosine}"/>%</div>
        </div>
        <p class="hint-box" style="margin:.75rem 0 0;">Text matching a 3-word phrase of the other submission is <mark style="background:#FEF08A; padding:0 3px;">highlighted</mark>.</p>
    </div>

    <div class="plagiarism-comparison">
        <div class="plagiarism-pane">
            <div style="display:flex; justify-content:space-between; margin-bottom:.75rem;">
                <strong>Student A: <c:out value="${pair.studentAName}"/></strong>
                <span class="text-muted text-small">Receipt: <c:out value="${pair.receiptA}"/></span>
            </div>
            <hr class="section-sep" style="margin:.5rem 0 1rem 0;">
            <%-- htmlA is built and HTML-escaped by PlagiarismService.highlight(), so it is printed as it is --%>
            <div class="pair-text">${pair.htmlA}</div>
        </div>

        <div class="plagiarism-pane">
            <div style="display:flex; justify-content:space-between; margin-bottom:.75rem;">
                <strong>Student B: <c:out value="${pair.studentBName}"/></strong>
                <span class="text-muted text-small">Receipt: <c:out value="${pair.receiptB}"/></span>
            </div>
            <hr class="section-sep" style="margin:.5rem 0 1rem 0;">
            <div class="pair-text">${pair.htmlB}</div>
        </div>
    </div>
</t:layout>
