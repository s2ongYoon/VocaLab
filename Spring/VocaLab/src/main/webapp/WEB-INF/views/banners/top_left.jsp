<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="banner-left">
    <div class="nav-logout-links">
        <c:choose>
            <%--        로그아웃       --%>
            <c:when test="${empty userSession.userId}">
                <span class="nav-item">
                    <a class="menu" href="/login">Login</a>
                </span>
                <span class="nav-item">
                    <a class="menu" href="/register">SignUp</a>
                </span>
            </c:when>
            <%--        관리자 로그인      --%>
            <c:when test="${userSession.userId eq 'ADMIN'}">
                <span class="nav-item">
                    <a class="menu" href="/WordBook/List">Vocabulary</a>
                </span>

                <div  class="dropdown">
                    <span class="nav-item dropdown">
                        <a class="menu" href="/">Contents</a>
                    </span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/">AI Essay</a>
                        <a class="menu" href="/">Test</a>
                    </div>
                </div>

                <div  class="dropdown">
                    <span class="nav-item dropdown">
                        <b>${userSession.userNickname}</b>&nbsp;님
                    </span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/myPage">MyPage</a>
                        <a class="menu" href="/CS/Inquiry">Admin</a>
                        <a class="menu" href="/logout">Logout</a>
                    </div>
                </div>
            </c:when>
            <%--      일반회원 로그인     --%>
            <c:when test="${userSession.userRole eq 'USER'}">
                <span class="nav-item" id="vocabulary">Vocabulary</span>

                <div  class="dropdown">
                    <span class="nav-item dropdown" id="contents">Contents</span>
                    <div class="dropdown-menu">
                        <a class="menu"  href="/">AI Essay</a>
                        <a class="menu" href="/">Test</a>
                    </div>
                </div>

                <div  class="dropdown">
                    <span class="nav-item dropdown" id="username">
                        <b>${userSession.userNickname}</b>&nbsp;님
                    </span>
                    <div class="dropdown-menu">
                        <a class="menu" href="/myPage">MyPage</a>
                        <a class="menu" href="/logout">Logout</a>
                    </div>
                </div>
            </c:when>
        </c:choose>
    </div>
    <div class="nav-left-logo">
        <a href="/"><img src="/images/logoEx1.png"  alt="Logo" class="logo-left"></a>
    </div>
</div>