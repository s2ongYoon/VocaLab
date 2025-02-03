<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="banner-center">
    <div class="nav-logout-links">
        <c:choose>
            <%--        로그아웃       --%>
            <c:when test="${empty sessionScope.sessionId}">
                <span class="nav-item">
                    <a class="menu" href="/login">Login</a>
                </span>
                <span class="nav-item">
                    <a class="menu" href="/register">SignUp</a>
                </span>
            </c:when>
            <%--        관리자 로그인      --%>
            <c:when test="${sessionScope.sessionRole eq 'ADMIN'}">
                <span class="nav-item">
                    <a class="menu" href="/">Vocabulary</a>
                </span>

                <div  class="dropdown">
                    <span class="nav-item dropdown">Contents</span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/">AI Essay</a>
                        <a class="menu" href="/">Test</a>
                    </div>
                </div>

                <div  class="dropdown">
                    <span class="nav-item dropdown"><b>${sessionScope.sessionNickName}</b>&nbsp;님</span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/myPage">MyPage</a>
                        <a class="menu" href="/">Admin</a>
                        <a class="menu" href="/">Logout</a>
                    </div>
                </div>
            </c:when>
            <%--      일반회원 로그인     --%>
            <c:when test="${sessionScope.sessionRole eq 'USER'}">
                <span class="nav-item" id="vocabulary">Vocabulary</span>

                <div  class="dropdown">
                    <span class="nav-item dropdown" id="contents">Contents</span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/">AI Essay</a>
                        <a class="menu" href="/">Test</a>
                    </div>
                </div>

                <div  class="dropdown">
                    <span class="nav-item dropdown" id="username"><b>${sessionScope.sessionNickName}</b>&nbsp;님</span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/myPage">MyPage</a>
                        <a class="menu" href="/">Logout</a>
                    </div>
                </div>
            </c:when>
        </c:choose>
    </div>
    <div class="nav-center-logo">
        <a href="/"><img src="/images/logoEx1.png" alt="Logo" class="logo-center" id="logo"></a>
    </div>
</div>
