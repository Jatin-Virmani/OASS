<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Submission History" activeNav="submissions">
    <div class="page-header">
        <h1 class="page-title">Submission History</h1>
    </div>
    
    <div class="card">
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Assignment</th>
                        <th>Receipt No</th>
                        <th>Version</th>
                        <th>Submitted At</th>
                        <th>Status</th>
                        <th>Grade / Score</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty submissions}">
                            <c:forEach var="s" items="${submissions}">
                                <tr>
                                    <td><span class="badge badge-indigo"><c:out value="${s.subjectCode}"/></span></td>
                                    <td><strong><c:out value="${s.assignmentTitle}"/></strong></td>
                                    <td><code><c:out value="${s.receiptNo}"/></code></td>
                                    <td>Version <c:out value="${s.versionNo}"/></td>
                                    <td><c:out value="${s.submittedAt}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${s.isLate}">
                                                <span class="badge badge-orange">Late Submitted</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-green">On Time</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty s.finalMarks}">
                                                <strong><c:out value="${s.finalMarks}"/> Marks</strong>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Under Review</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="empty-state">No submissions yet. Check your upcoming assignments!</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</t:layout>
