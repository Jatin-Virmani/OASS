<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Grades & Results" activeNav="results">
    <div class="page-header">
        <h1 class="page-title">Grades &amp; Published Results</h1>
    </div>
    
    <div class="card">
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Assignment</th>
                        <th>Max Marks</th>
                        <th>Score Obtained</th>
                        <th>Teacher Feedback</th>
                        <th>Published Date</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty results}">
                            <c:forEach var="r" items="${results}">
                                <tr>
                                    <td><span class="badge badge-indigo"><c:out value="${r.subjectCode}"/></span></td>
                                    <td><strong><c:out value="${r.assignmentTitle}"/></strong></td>
                                    <td><c:out value="${r.maxMarks}"/></td>
                                    <td><strong style="color:var(--green);"><c:out value="${r.finalMarks}"/></strong></td>
                                    <td><c:out value="${r.feedback}"/></td>
                                    <td><c:out value="${r.publishedAt}"/></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/student/result/view?evalId=${r.evalId}" class="btn btn-primary btn-sm">View</a>
                                        <a href="${pageContext.request.contextPath}/student/regrade/request?evalId=${r.evalId}" class="btn btn-secondary btn-sm">Appeal Regrade</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="empty-state">No published evaluations yet.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</t:layout>
