<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout activeNav="requests" title="Requests Inbox" >
    <div class="page-header"><h1 class="page-title">Extension / Regrade Requests</h1></div>
    <div class="table-responsive">
        <table>
            <thead><tr><th>Type</th><th>Student</th><th>Assignment</th><th>Reason</th><th>Action</th></tr></thead>
            <tbody>
                <tr><td><span class="status-badge status-LATE">EXTENSION</span></td><td>Student A</td><td>Java MVC</td><td>Medical</td><td>
                    <form action="${pageContext.request.contextPath}/teacher/requests" method="POST" style="display:inline;"><button type="submit" name="action" value="approve" class="btn btn-primary btn-sm">Approve</button> <button type="submit" name="action" value="reject" class="btn btn-danger btn-sm">Reject</button></form>
                </td></tr>
            </tbody>
        </table>
    </div>
</t:layout>
