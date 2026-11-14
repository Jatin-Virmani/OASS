<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Dashboard" activeNav="dashboard">

    <%-- Welcome banner --%>
    <div class="welcome-card">
        <div>
            <div class="welcome-name">Welcome back, <c:out value="${sessionScope.user.name}"/></div>
            <div class="welcome-sub">
                Department of Computer Science &amp; Engineering &bull;
                Section <c:out value="${student.sectionName}"/> &bull;
                Roll: <c:out value="${student.rollNo}"/>
            </div>
        </div>
        <div>
            <span class="welcome-term"><c:out value="${term}"/></span>
        </div>
    </div>

    <%-- Next-due banner (shown only when there is a pending assignment) --%>
    <c:if test="${not empty nextDue}">
        <div class="next-due-banner ${nextDue.timeLeftCssClass == 'time-urgent' ? 'urgent' : ''}">
            <div>
                <div class="next-due-label">Next Deadline</div>
                <div class="next-due-title"><c:out value="${nextDue.title}"/></div>
                <div class="next-due-time">
                    <c:out value="${nextDue.subjectCode}"/> &bull;
                    Due: <c:out value="${nextDue.deadlineText}"/>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/student/assignment/submit?id=${nextDue.id}"
               class="btn btn-primary btn-sm">Submit Now</a>
        </div>
    </c:if>

    <%-- Stat cards --%>
    <div class="stat-cards">
        <div class="stat-card">
            <div class="stat-card-label">Pending</div>
            <div class="stat-card-value" style="color:#92400E"><c:out value="${pending}"/></div>
            <div class="stat-card-sub">Not yet submitted</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Submitted</div>
            <div class="stat-card-value" style="color:#059669"><c:out value="${submitted}"/></div>
            <div class="stat-card-sub">On time</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Late</div>
            <div class="stat-card-value" style="color:#9A3412"><c:out value="${late}"/></div>
            <div class="stat-card-sub">After deadline</div>
        </div>
        <div class="stat-card">
            <div class="stat-card-label">Missed</div>
            <div class="stat-card-value" style="color:#991B1B"><c:out value="${missed}"/></div>
            <div class="stat-card-sub">No submission</div>
        </div>
    </div>

    <%-- Visual Analytics Grid --%>
    <div class="chart-grid">
        <%-- Chart 1: Submission Progress Meter --%>
        <div class="chart-card">
            <div class="chart-header">
                <div>
                    <div class="chart-title">Submission Progress</div>
                    <div class="chart-sub">Assignment completion overview</div>
                </div>
                <span class="badge ${completionPct >= 75 ? 'badge-green' : (completionPct >= 50 ? 'badge-blue' : 'badge-amber')}">
                    <c:out value="${completionPct}"/>% Completed
                </span>
            </div>

            <div class="segmented-meter">
                <c:if test="${totalAssignments > 0}">
                    <div class="segmented-part fill-emerald" style="width: ${((submitted) / totalAssignments) * 100}%;" title="Submitted on time"></div>
                    <div class="segmented-part fill-indigo" style="width: ${((late) / totalAssignments) * 100}%;" title="Submitted late"></div>
                    <div class="segmented-part fill-amber" style="width: ${((pending) / totalAssignments) * 100}%;" title="Pending"></div>
                    <div class="segmented-part fill-rose" style="width: ${((missed) / totalAssignments) * 100}%;" title="Missed"></div>
                </c:if>
            </div>

            <div class="segmented-legend">
                <span class="legend-pill"><span class="legend-dot" style="background:#10B981;"></span> Submitted (<c:out value="${submitted}"/>)</span>
                <span class="legend-pill"><span class="legend-dot" style="background:#6366F1;"></span> Late (<c:out value="${late}"/>)</span>
                <span class="legend-pill"><span class="legend-dot" style="background:#F59E0B;"></span> Pending (<c:out value="${pending}"/>)</span>
                <span class="legend-pill"><span class="legend-dot" style="background:#EF4444;"></span> Missed (<c:out value="${missed}"/>)</span>
            </div>
        </div>

        <%-- Chart 2: Grade Performance Bar Chart --%>
        <div class="chart-card">
            <div class="chart-header">
                <div>
                    <div class="chart-title">Score Performance</div>
                    <div class="chart-sub">Evaluated assignments</div>
                </div>
                <c:if test="${avgScore > 0}">
                    <span class="badge badge-indigo">Avg: <c:out value="${avgScore}"/>%</span>
                </c:if>
            </div>

            <c:choose>
                <c:when test="${empty grades}">
                    <div style="font-size:0.85rem; color:var(--muted); text-align:center; padding:1.25rem 0;">
                        No graded assignments yet. Marks will appear here after evaluation.
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="g" items="${grades}">
                        <div class="bar-chart-row">
                            <div class="bar-chart-meta">
                                <span class="bar-chart-name" title="${g.title}">
                                    <span class="badge badge-indigo" style="font-size:0.7rem; padding:1px 5px;"><c:out value="${g.subjectCode}"/></span>
                                    <c:out value="${g.title}"/>
                                </span>
                                <span class="bar-chart-val"><c:out value="${g.finalMarks}"/> / <c:out value="${g.maxMarks}"/> (<c:out value="${g.pct}"/>%)</span>
                            </div>
                            <div class="bar-chart-track">
                                <div class="bar-chart-fill ${g.pct >= 75 ? 'fill-emerald' : (g.pct >= 50 ? 'fill-blue' : 'fill-amber')}"
                                     style="width: ${g.pct}%;"></div>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Upcoming assignments table --%>
    <div class="card mb-2">
        <div class="card-header">
            <span class="card-title">Upcoming &amp; Active Assignments</span>
            <a href="${pageContext.request.contextPath}/student/assignments" class="btn btn-secondary btn-sm">View All</a>
        </div>
        <c:choose>
            <c:when test="${empty upcoming}">
                <div class="empty-state">No pending assignments – you're all caught up!</div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Subject</th>
                                <th>Assignment</th>
                                <th>Deadline &amp; Time Left</th>
                                <th>Max Marks</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="a" items="${upcoming}">
                                <tr>
                                    <td>
                                        <span class="badge badge-indigo"><c:out value="${a.subjectCode}"/></span>
                                        <div class="text-small text-muted mt-1"><c:out value="${a.subjectName}"/></div>
                                    </td>
                                    <td><c:out value="${a.title}"/></td>
                                    <td>
                                        <div><c:out value="${a.deadlineText}"/></div>
                                        <span class="countdown ${a.timeLeftCssClass}" data-deadline="${a.deadlineEpochMillis}"><c:out value="${a.timeLeftText}"/></span>
                                    </td>
                                    <td><c:out value="${a.maxMarks}"/></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/student/assignment/submit?id=${a.id}"
                                           class="btn btn-primary btn-sm">Submit</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- Recent notifications widget --%>
    <c:if test="${not empty notifications}">
        <div class="card">
            <div class="card-header">
                <span class="card-title">Recent Notifications</span>
                <a href="${pageContext.request.contextPath}/student/notifications" class="btn btn-secondary btn-sm">View All</a>
            </div>
            <c:forEach var="n" items="${notifications}">
                <div class="notif-item">
                    <div class="notif-dot ${n.read ? 'read' : ''}"></div>
                    <div>
                        <div class="notif-title"><c:out value="${n.title}"/></div>
                        <div class="notif-msg"><c:out value="${n.message}"/></div>
                        <div class="notif-time"><c:out value="${n.createdAtText}"/></div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <%-- Announcements from teachers of this section --%>
    <div class="card mb-2">
        <div class="card-header"><span class="card-title">Announcements</span></div>
        <c:choose>
            <c:when test="${empty announcements}"><div class="empty-state">No announcements.</div></c:when>
            <c:otherwise>
                <c:forEach var="an" items="${announcements}">
                    <div class="score-line" style="display:block;">
                        <strong><c:out value="${an.title}"/></strong>
                        <span class="text-muted text-small"> &bull; <c:out value="${an.createdAt}"/></span>
                        <p style="white-space:pre-wrap; margin-top:.25rem;"><c:out value="${an.body}"/></p>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</t:layout>
