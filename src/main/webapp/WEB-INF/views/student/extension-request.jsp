<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="dashboard" title="Extension Request" >
    <div class="page-header"><h1 class="page-title">Request Deadline Extension</h1></div>
    <div class="card">
        <c:choose>
            <c:when test="${not empty assignment}">
                <form action="${pageContext.request.contextPath}/student/extension/request" method="POST">
                    <input type="hidden" name="assignmentId" value="${assignment.id}">
                    <div class="form-group">
                        <label class="form-label">Assignment</label>
                        <input type="text" class="form-control" value="${assignment.title}" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Current Deadline</label>
                        <input type="text" class="form-control" value="${assignment.deadlineText}" disabled>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="newDeadline">Requested New Deadline</label>
                        <input type="datetime-local" id="newDeadline" name="newDeadline" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="reason">Reason for extension</label>
                        <textarea id="reason" name="reason" class="form-control" rows="4" required></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Submit Request</button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    No assignment selected. Go to your
                    <a href="${pageContext.request.contextPath}/student/assignments">assignments</a>
                    and click "Request Extension" on a specific assignment.
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</t:layout>
