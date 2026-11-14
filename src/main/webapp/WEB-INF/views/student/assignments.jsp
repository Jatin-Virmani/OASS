<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="My Assignments" activeNav="assignments">
    <div class="page-header">
        <h1 class="page-title">My Assignments</h1>
    </div>
    
    <div class="card">
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Assignment</th>
                        <th>Deadline</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty assignments}">
                        <tr><td colspan="5" class="empty-state">No assignments have been posted for your section yet.</td></tr>
                    </c:if>
                    <c:forEach var="a" items="${assignments}">
                        <tr>
                            <td>
                                <span class="badge badge-indigo"><c:out value="${a.subjectCode}"/></span>
                            </td>
                            <td><strong><c:out value="${a.title}"/></strong></td>
                            <td>
                                <div><c:out value="${a.deadlineText}"/></div>
                                <span class="countdown ${a.timeLeftCssClass}" data-deadline="${a.deadlineEpochMillis}"><c:out value="${a.timeLeftText}"/></span>
                            </td>
                            <td>
                                <span class="badge status-${a.status}">
                                    <c:out value="${a.status}"/>
                                </span>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/student/assignment/view?id=${a.id}" class="btn btn-secondary btn-sm">View</a>
                                <c:if test="${a.status == 'PUBLISHED'}">
                                    <a href="${pageContext.request.contextPath}/student/assignment/submit?id=${a.id}" class="btn btn-primary btn-sm">Submit</a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</t:layout>
