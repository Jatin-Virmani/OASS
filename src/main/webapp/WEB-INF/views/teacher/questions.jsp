<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout activeNav="questions" title="Question Bank">
    <div class="page-header"><h1 class="page-title">Question Bank</h1></div>

    <c:choose>
    <c:when test="${empty subjects}">
        <div class="card"><div class="empty-state">No subject is assigned to you yet, so you cannot add questions.</div></div>
    </c:when>
    <c:otherwise>
    <div class="card mb-2">
        <h3>Add Question</h3>
        <form action="${pageContext.request.contextPath}/teacher/questions" method="POST" style="margin-top:1rem;">
            <div class="form-group">
                <label class="form-label" for="subject">Subject</label>
                <select id="subject" name="subject" class="form-control" required>
                    <c:forEach var="s" items="${subjects}">
                        <option value="${s.id}"><c:out value="${s.code} - ${s.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label" for="questionText">Question</label>
                <textarea id="questionText" name="questionText" class="form-control" rows="3" required></textarea>
            </div>
            <div class="form-group">
                <label class="form-label" for="type">Type</label>
                <select id="type" name="type" class="form-control">
                    <option value="SHORT">Short</option><option value="DESCRIPTIVE" selected>Descriptive</option><option value="ESSAY">Essay</option>
                </select>
            </div>
            <div class="form-group"><label class="form-label" for="topic">Topic</label>
                <input type="text" id="topic" name="topic" class="form-control" maxlength="100"></div>
            <div class="form-group">
                <label class="form-label" for="difficulty">Difficulty</label>
                <select id="difficulty" name="difficulty" class="form-control">
                    <option value="EASY">Easy</option><option value="MEDIUM" selected>Medium</option><option value="HARD">Hard</option>
                </select>
            </div>
            <div class="form-group"><label class="form-label" for="marks">Marks</label>
                <input type="number" id="marks" name="marks" class="form-control" min="0.5" step="0.5" required></div>
            <button type="submit" class="btn btn-primary">Add Question</button>
        </form>
    </div>
    </c:otherwise>
    </c:choose>

    <div class="card">
        <div class="card-header"><span class="card-title">My Questions</span></div>
        <c:choose>
        <c:when test="${empty questions}"><div class="empty-state">Your question bank is empty.</div></c:when>
        <c:otherwise>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Subject</th><th>Question</th><th>Topic</th><th>Type</th><th>Difficulty</th><th>Marks</th></tr></thead>
                <tbody>
                    <c:forEach var="q" items="${questions}">
                        <tr>
                            <td><span class="badge badge-indigo"><c:out value="${q.subjectCode}"/></span></td>
                            <td><c:out value="${q.text}"/></td>
                            <td><c:out value="${q.topic}"/></td>
                            <td><c:out value="${q.type}"/></td>
                            <td><c:out value="${q.difficulty}"/></td>
                            <td><c:out value="${q.marks}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        </c:otherwise>
        </c:choose>
    </div>
</t:layout>
