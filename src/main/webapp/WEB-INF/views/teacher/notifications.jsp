<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<t:layout title="Notifications" activeNav="notifications">
    <div class="page-header">
        <h1 class="page-title">Teacher Notifications</h1>
    </div>
    <div class="card">
        <div class="notif-item">
            <div class="notif-dot"></div>
            <div>
                <div class="notif-title">New Submissions Uploaded</div>
                <div class="notif-msg">Mini Project Milestone 2 has new submissions awaiting evaluation.</div>
                <div class="notif-time">Just now</div>
            </div>
        </div>
        <div class="notif-item">
            <div class="notif-dot read"></div>
            <div>
                <div class="notif-title">Deadline Approaching</div>
                <div class="notif-msg">Servlet and JSP Mini App submission window closes soon.</div>
                <div class="notif-time">2 hours ago</div>
            </div>
        </div>
    </div>
</t:layout>
