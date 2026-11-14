<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Plagiarism Dashboard" activeNav="plagiarism">
    <div class="page-header">
        <div>
            <h1 class="page-title">Plagiarism Detection Engine</h1>
            <p class="text-muted text-small">Algorithms: 3-word shingle Jaccard + word-count vector cosine similarity</p>
        </div>
        <form action="${pageContext.request.contextPath}/teacher/plagiarism" method="POST">
            <button type="submit" class="btn btn-primary">Run Detection on My Assignments</button>
        </form>
    </div>

    <div class="card mb-2">
        <strong>Risk levels:</strong>
        <span class="badge badge-red">HIGH</span> score at or above the assignment's threshold (default 70%)
        &nbsp;<span class="badge badge-amber">MEDIUM</span> within 30 points below it
        &nbsp;<span class="badge badge-green">LOW</span> otherwise.
        Only each student's latest version is compared.
    </div>

    <div class="card">
        <div class="card-header"><span class="card-title">Similarity Analysis Results</span></div>
        <c:choose>
        <c:when test="${empty similarityResults}">
            <div class="empty-state">No pairs compared yet. Press "Run Detection" after students have submitted.</div>
        </c:when>
        <c:otherwise>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Assignment</th><th>Student A</th><th>Student B</th>
                        <th>Jaccard</th><th>Cosine</th><th>Overall Match</th><th>Risk Level</th><th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="res" items="${similarityResults}">
                        <tr>
                            <td><strong><c:out value="${res.assignmentTitle}"/></strong></td>
                            <td><c:out value="${res.studentAName}"/></td>
                            <td><c:out value="${res.studentBName}"/></td>
                            <td><c:out value="${res.jaccard}"/>%</td>
                            <td><c:out value="${res.cosine}"/>%</td>
                            <td><strong style="color: ${res.risk == 'HIGH' ? 'var(--red)' : (res.risk == 'MEDIUM' ? 'var(--amber)' : 'var(--green)')};"><c:out value="${res.score}"/>%</strong></td>
                            <td>
                                <span class="badge ${res.risk == 'HIGH' ? 'badge-red' : (res.risk == 'MEDIUM' ? 'badge-amber' : 'badge-green')}">
                                    <c:out value="${res.risk}"/>
                                </span>
                            </td>
                            <td><a href="${pageContext.request.contextPath}/teacher/plagiarism/compare?id=${res.id}" class="btn btn-secondary btn-sm">Compare</a></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        </c:otherwise>
        </c:choose>
    </div>
</t:layout>
