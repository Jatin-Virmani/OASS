<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Teacher Dashboard" activeNav="dashboard">

    <div class="page-header">
        <h1 class="page-title">Teacher Dashboard</h1>
        <a href="${pageContext.request.contextPath}/teacher/assignment/new"
           class="btn btn-primary">+ New Assignment</a>
    </div>

    <%-- Stat cards --%>
    <div class="stat-cards">
        <div class="stat-card">
            <div class="stat-card-label">My Assignments</div>
            <div class="stat-card-value"><c:out value="${totalAssignments}"/></div>
            <div class="stat-card-sub">Total created</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Total Submissions</div>
            <div class="stat-card-value"><c:out value="${totalSubmissions}"/></div>
            <div class="stat-card-sub">Across all assignments</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid #6366F1;">
            <div class="stat-card-label">Average Marks</div>
            <div class="stat-card-value"><c:out value="${avgMarksText}"/></div>
            <div class="stat-card-sub">Published results</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid #EF4444;">
            <div class="stat-card-label">Plagiarism Check</div>
            <div class="stat-card-value" style="color:#EF4444;"><c:out value="${riskCounts.HIGH}"/> Flagged</div>
            <div class="stat-card-sub"><a href="${pageContext.request.contextPath}/teacher/plagiarism" style="color:var(--brand); font-weight:600;">Open Engine &rarr;</a></div>
        </div>
        <div class="stat-card" style="border-left: 4px solid #10B981;">
            <div class="stat-card-label">Requests Inbox</div>
            <div class="stat-card-value" style="color:#10B981;">Review</div>
            <div class="stat-card-sub"><a href="${pageContext.request.contextPath}/teacher/requests" style="color:var(--brand); font-weight:600;">Extensions &amp; Appeals &rarr;</a></div>
        </div>
    </div>

    <%-- Visual Analytics Grid --%>
    <div class="chart-grid">
        <%-- Chart 1: Submission Turnout Rates --%>
        <div class="chart-card">
            <div class="chart-header">
                <div>
                    <div class="chart-title">Class Submission Turnout</div>
                    <div class="chart-sub">Student completion by assignment</div>
                </div>
                <a href="${pageContext.request.contextPath}/teacher/assignments" class="btn btn-secondary btn-sm" style="font-size:0.75rem; padding:3px 8px;">View All</a>
            </div>

            <c:choose>
                <c:when test="${empty submissionStats}">
                    <div style="font-size:0.85rem; color:var(--muted); text-align:center; padding:1.25rem 0;">
                        No active assignments to report.
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="s" items="${submissionStats}">
                        <div class="bar-chart-row">
                            <div class="bar-chart-meta">
                                <span class="bar-chart-name" title="<c:out value='${s.title}'/>">
                                    <span class="badge badge-indigo" style="font-size:0.7rem; padding:1px 5px;"><c:out value="${s.subjectCode}"/></span>
                                    <c:out value="${s.title}"/>
                                </span>
                                <span class="bar-chart-val"><c:out value="${s.subCount}"/> / <c:out value="${s.totalStudents}"/> (<c:out value="${s.pct}"/>%)<c:if test="${not empty s.avgMarks}"> &bull; avg <c:out value="${s.avgMarks}"/></c:if></span>
                            </div>
                            <div class="bar-chart-track">
                                <div class="bar-chart-fill ${s.pct >= 50 ? 'fill-emerald' : (s.pct >= 20 ? 'fill-blue' : 'fill-amber')}"
                                     style="width: ${s.pct}%;"></div>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- Chart 2: Plagiarism Risk Distribution --%>
        <div class="chart-card">
            <div class="chart-header">
                <div>
                    <div class="chart-title">Plagiarism Risk Distribution</div>
                    <div class="chart-sub"><c:out value="${totalPairs}"/> student pairs compared</div>
                </div>
                <a href="${pageContext.request.contextPath}/teacher/plagiarism" class="btn btn-secondary btn-sm" style="font-size:0.75rem; padding:3px 8px;">View Matrix</a>
            </div>

            <c:choose>
                <c:when test="${totalPairs == 0}">
                    <div style="font-size:0.85rem; color:var(--muted); text-align:center; padding:1.25rem 0;">
                        No similarity checks executed yet. Click Plagiarism Check to run analysis.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="segmented-meter" style="height:16px;">
                        <div class="segmented-part fill-emerald" style="width: ${lowPct}%;" title="Low Risk (< 40%)"></div>
                        <c:if test="${medPct > 0}">
                            <div class="segmented-part fill-amber" style="width: ${medPct}%;" title="Medium Risk (40-69%)"></div>
                        </c:if>
                        <div class="segmented-part fill-rose" style="width: ${highPct}%;" title="High Risk (&ge; 70%)"></div>
                    </div>

                    <div class="segmented-legend">
                        <span class="legend-pill"><span class="legend-dot" style="background:#10B981;"></span> Low Risk (<c:out value="${riskCounts.LOW}"/> pairs &bull; <c:out value="${lowPct}"/>%)</span>
                        <c:if test="${riskCounts.MEDIUM > 0}">
                            <span class="legend-pill"><span class="legend-dot" style="background:#F59E0B;"></span> Medium (<c:out value="${riskCounts.MEDIUM}"/> pairs)</span>
                        </c:if>
                        <span class="legend-pill"><span class="legend-dot" style="background:#EF4444;"></span> High Risk (<c:out value="${riskCounts.HIGH}"/> pairs &bull; <c:out value="${highPct}"/>%)</span>
                    </div>

                    <div style="margin-top:1.25rem; padding:0.75rem; background:rgba(239,68,68,0.08); border:1px solid rgba(239,68,68,0.2); border-radius:var(--radius-sm); display:flex; justify-content:space-between; align-items:center;">
                        <span style="font-size:0.8rem; color:#991B1B; font-weight:500;">
                            <c:out value="${riskCounts.HIGH}"/> submission pairs require teacher review
                        </span>
                        <a href="${pageContext.request.contextPath}/teacher/plagiarism" class="btn btn-sm btn-primary" style="font-size:0.75rem; padding:3px 10px;">
                            Review
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Recent assignments table --%>
    <div class="card">
        <div class="card-header">
            <span class="card-title">Recent Assignments</span>
            <a href="${pageContext.request.contextPath}/teacher/assignments"
               class="btn btn-secondary btn-sm">View All</a>
        </div>
        <c:choose>
            <c:when test="${empty recentAssignments}">
                <div class="empty-state">No assignments yet. Create your first one!</div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Assignment</th>
                                <th>Subject</th>
                                <th>Deadline</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="a" items="${recentAssignments}">
                                <tr>
                                    <td><c:out value="${a.title}"/></td>
                                    <td>
                                        <span class="badge badge-indigo"><c:out value="${a.subjectCode}"/></span>
                                    </td>
                                    <td><c:out value="${a.deadlineText}"/></td>
                                    <td>
                                        <span class="badge status-${a.status}">
                                            <c:out value="${a.status}"/>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</t:layout>
