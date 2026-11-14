<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="results" title="Regrade Request" >
    <div class="page-header"><h1 class="page-title">Appeal for Regrade</h1></div>
    <div class="card">
        <form action="${pageContext.request.contextPath}/student/regrade/request" method="POST">
            <input type="hidden" name="evaluationId" value="<c:out value='${param.evalId}'/>">
            <div class="form-group"><label class="form-label" for="reason">Reason for appeal</label><textarea id="reason" name="reason" class="form-control" rows="4" required></textarea></div>
            <button type="submit" class="btn btn-primary">Submit Appeal</button>
        </form>
    </div>
</t:layout>
