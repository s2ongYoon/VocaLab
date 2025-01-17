<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%--
    고객센터 메인 페이지 입니다.
--%>
<html>
<head>
    <title>고객센터 메인 페이지</title>
</head>
<body>
<div class="nav">
    <a class="logo">VocaLab</a>
    <sec:authorize access="isAuthenticated()">
            <span>
                <sec:authentication property="principal.username" />님
                <a href="/logout">로그아웃</a>
            </span>
    </sec:authorize>
    <sec:authorize access="!isAuthenticated()">
            <span>
                <a href="/login">로그인</a>
            </span>
    </sec:authorize>
</div>
    <p>메인페이지임</p>
</body>
</html>
