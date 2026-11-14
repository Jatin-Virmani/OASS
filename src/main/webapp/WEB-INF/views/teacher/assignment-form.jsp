<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- First load: defaults. After a validation error the servlet forwards back here and we refill from the request (param.*). --%>
<c:set var="retry" value="${not empty param.title}"/>
<t:layout activeNav="assignments" title="Create Assignment">
    <div class="page-header"><h1 class="page-title">Create Assignment</h1></div>

    <c:choose>
    <c:when test="${empty classes}">
        <div class="card"><div class="empty-state">No subject/section is assigned to you yet, so you cannot create an assignment.</div></div>
    </c:when>
    <c:otherwise>
    <div class="card">
        <form action="${pageContext.request.contextPath}/teacher/assignment/create" method="POST">

            <div class="form-group">
                <label class="form-label" for="title">Title</label>
                <input type="text" id="title" name="title" class="form-control" required maxlength="150"
                       value="<c:out value='${param.title}'/>">
            </div>

            <div class="form-group">
                <label class="form-label" for="teaching">Subject &amp; Section</label>
                <select id="teaching" name="teaching" class="form-control" required>
                    <c:forEach var="cl" items="${classes}">
                        <option value="${cl.teachingId}" ${param.teaching == cl.teachingId ? 'selected' : ''}>
                            <c:out value="${cl.subjectCode} - ${cl.subjectName} (${cl.sectionName})"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="description">Description</label>
                <textarea id="description" name="description" class="form-control" rows="4"><c:out value="${param.description}"/></textarea>
            </div>

            <div class="form-group">
                <label class="form-label" for="deadline">Deadline</label>
                <input type="datetime-local" id="deadline" name="deadline" class="form-control" required
                       value="<c:out value='${param.deadline}'/>">
            </div>

            <h3 style="margin-top:2rem;">Late Policy</h3>
            <div class="form-group">
                <label><input type="checkbox" name="allowLate" ${retry ? (not empty param.allowLate ? 'checked' : '') : 'checked'}> Allow late submissions</label>
            </div>
            <div class="form-group">
                <label class="form-label" for="penalty">Late penalty (% of marks per day)</label>
                <input type="number" id="penalty" name="penalty" class="form-control" min="0" max="100" step="0.5"
                       value="<c:out value='${empty param.penalty ? 10 : param.penalty}'/>">
            </div>
            <div class="form-group">
                <label class="form-label" for="maxPenalty">Maximum late penalty (%)</label>
                <input type="number" id="maxPenalty" name="maxPenalty" class="form-control" min="0" max="100" step="0.5"
                       value="<c:out value='${empty param.maxPenalty ? 50 : param.maxPenalty}'/>">
            </div>
            <div class="form-group">
                <label class="form-label" for="cutoff">Hard cutoff (no uploads after this; optional)</label>
                <input type="datetime-local" id="cutoff" name="cutoff" class="form-control"
                       value="<c:out value='${param.cutoff}'/>">
            </div>

            <h3 style="margin-top:2rem;">Upload Rules</h3>
            <div class="form-group">
                <label class="form-label" for="fileTypes">Allowed file types (comma separated)</label>
                <input type="text" id="fileTypes" name="fileTypes" class="form-control"
                       value="<c:out value='${empty param.fileTypes ? "pdf, docx, txt, java, zip" : param.fileTypes}'/>">
            </div>
            <div class="form-group">
                <label class="form-label" for="maxSize">Maximum file size (MB, up to 20)</label>
                <input type="number" id="maxSize" name="maxSize" class="form-control" min="1" max="20"
                       value="<c:out value='${empty param.maxSize ? 10 : param.maxSize}'/>">
            </div>

            <h3 style="margin-top:2rem;">Plagiarism &amp; Grading</h3>
            <div class="form-group">
                <label><input type="checkbox" name="plagiarism" ${retry ? (not empty param.plagiarism ? 'checked' : '') : 'checked'}> Enable plagiarism check</label>
            </div>
            <div class="form-group">
                <label class="form-label" for="threshold">Plagiarism threshold (% score that counts as HIGH risk)</label>
                <input type="number" id="threshold" name="threshold" class="form-control" min="1" max="100"
                       value="<c:out value='${empty param.threshold ? 70 : param.threshold}'/>">
            </div>
            <div class="form-group">
                <label><input type="checkbox" name="blindGrading" ${retry ? (not empty param.blindGrading ? 'checked' : '') : ''}>
                    Blind grading (teacher sees "Candidate #id" instead of the student's name)</label>
            </div>

            <h3 style="margin-top:2rem;">Rubric</h3>
            <p class="hint-box">The maximum marks of the assignment are the sum of all criteria.</p>
            <div id="rubric">
                <c:forEach var="n" items="${critNames}" varStatus="st">
                    <div class="rubric-row">
                        <input type="text" name="criterionName" class="form-control" placeholder="Criterion name" maxlength="100"
                               value="<c:out value='${n}'/>">
                        <input type="number" name="criterionMarks" class="form-control marks" placeholder="Marks" min="0.5" step="0.5"
                               value="<c:out value='${critMarks[st.index]}'/>">
                        <button type="button" class="btn btn-secondary btn-sm" onclick="this.parentNode.remove()">Remove</button>
                    </div>
                </c:forEach>
            </div>
            <button type="button" class="btn btn-secondary btn-sm" id="add-criterion">+ Add criterion</button>

            <div style="margin-top:2rem; display:flex; gap:1rem; flex-wrap:wrap;">
                <button type="submit" name="action" value="draft" class="btn btn-secondary">Save as Draft</button>
                <button type="submit" name="action" value="publish" class="btn btn-primary">Publish</button>
            </div>
        </form>
    </div>
    </c:otherwise>
    </c:choose>

    <script>
        // add one more empty rubric row
        var addButton = document.getElementById('add-criterion');
        if (addButton) {
            addButton.addEventListener('click', function () {
                var row = document.createElement('div');
                row.className = 'rubric-row';
                row.innerHTML =
                    '<input type="text" name="criterionName" class="form-control" placeholder="Criterion name" maxlength="100">' +
                    '<input type="number" name="criterionMarks" class="form-control marks" placeholder="Marks" min="0.5" step="0.5">' +
                    '<button type="button" class="btn btn-secondary btn-sm" onclick="this.parentNode.remove()">Remove</button>';
                document.getElementById('rubric').appendChild(row);
            });
        }
    </script>
</t:layout>
