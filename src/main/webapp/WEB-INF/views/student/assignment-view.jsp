<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Assignment Details" activeNav="assignments">
    <div class="page-header">
        <div>
            <h1 class="page-title"><c:out value="${assignment.title}"/></h1>
            <p class="text-muted"><c:out value="${assignment.subjectCode}"/> &bull; <c:out value="${assignment.subjectName}"/></p>
        </div>
        <c:if test="${assignment.status == 'PUBLISHED'}">
            <a href="${pageContext.request.contextPath}/student/assignment/submit?id=${assignment.id}" class="btn btn-primary">Submit Assignment</a>
        </c:if>
    </div>

    <div class="card mb-2">
        <div class="card-header"><span class="card-title">Instructions &amp; Overview</span></div>
        <p style="line-height:1.7;"><c:out value="${assignment.description}"/></p>
        
        <hr class="section-sep">
        <div style="display:flex; gap:2rem; flex-wrap:wrap;">
            <div><strong>Max Marks:</strong> <c:out value="${assignment.maxMarks}"/></div>
            <div><strong>Deadline:</strong> <c:out value="${assignment.deadlineText}"/></div>
            <div><strong>Allowed Formats:</strong> <c:out value="${assignment.allowedTypes}"/></div>
        </div>
    </div>

    <c:if test="${not empty criteria}">
        <div class="card">
            <div class="card-header"><span class="card-title">Grading Rubric Breakdown</span></div>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Criterion</th><th>Max Marks</th></tr></thead>
                    <tbody>
                        <c:forEach var="c" items="${criteria}">
                            <tr>
                                <td><c:out value="${c.name}"/></td>
                                <td><strong><c:out value="${c.maxMarks}"/></strong></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>
</t:layout>
