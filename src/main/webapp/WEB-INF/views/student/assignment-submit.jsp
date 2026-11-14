<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Submit Assignment" activeNav="assignments">
    <div class="page-header">
        <div>
            <h1 class="page-title">Submit: <c:out value="${assignment.title}"/></h1>
            <p class="text-muted">
                Deadline <c:out value="${assignment.deadlineText}"/> &bull;
                <span class="countdown ${assignment.timeLeftCssClass}" data-deadline="${assignment.deadlineEpochMillis}"><c:out value="${assignment.timeLeftText}"/></span>
            </p>
        </div>
    </div>

    <div class="card">
        <p class="hint-box">
            Allowed types: <strong><c:out value="${allowedTypes}"/></strong> &bull;
            Maximum size: <strong><c:out value="${assignment.maxFileMb}"/> MB</strong>
            <c:if test="${assignment.allowLate}"> &bull; Late uploads are accepted with a penalty of <c:out value="${assignment.latePenaltyPctPerDay}"/>% per day.</c:if>
            <c:if test="${not assignment.allowLate}"> &bull; Late uploads are not accepted.</c:if>
        </p>

        <form id="submit-form" action="${pageContext.request.contextPath}/student/assignment/submit" method="POST" enctype="multipart/form-data">
            <input type="hidden" name="id" value="${assignment.id}">
            <div class="form-group">
                <label class="form-label" for="file">Upload File</label>
                <input type="file" id="file" name="file" class="form-control" required>
                <div id="file-info" class="file-info"></div>
            </div>
            <div class="form-group">
                <label class="form-label" for="comment">Comment (Optional)</label>
                <textarea id="comment" name="comment" class="form-control" rows="4" maxlength="500"></textarea>
            </div>
            <button type="submit" id="submit-btn" class="btn btn-primary">Submit Assignment</button>
        </form>
    </div>

    <script>
        // The server checks everything again. This only gives the student an instant answer.
        var allowed = '<c:out value="${assignment.allowedTypes}"/>'.split(',');
        var maxBytes = ${assignment.maxFileMb} * 1024 * 1024;
        var fileInput = document.getElementById('file');
        var info = document.getElementById('file-info');
        var button = document.getElementById('submit-btn');

        fileInput.addEventListener('change', function () {
            var f = fileInput.files[0];
            info.className = 'file-info';
            button.disabled = false;
            if (!f) { info.textContent = ''; return; }

            var dot = f.name.lastIndexOf('.');
            var ext = dot < 0 ? '' : f.name.substring(dot + 1).toLowerCase();
            var sizeText = (f.size / 1024 / 1024).toFixed(2) + ' MB';

            if (allowed.indexOf(ext) < 0) {
                info.textContent = f.name + ': the type ".' + ext + '" is not allowed.';
                info.className = 'file-info error';
                button.disabled = true;
            } else if (f.size > maxBytes) {
                info.textContent = f.name + ' is ' + sizeText + ', which is more than the limit.';
                info.className = 'file-info error';
                button.disabled = true;
            } else {
                info.textContent = 'Selected: ' + f.name + ' (' + sizeText + ')';
            }
        });

        // show a spinner and stop double clicks while uploading
        document.getElementById('submit-form').addEventListener('submit', function () {
            button.classList.add('is-loading');
            button.textContent = 'Uploading';
        });
    </script>
</t:layout>
