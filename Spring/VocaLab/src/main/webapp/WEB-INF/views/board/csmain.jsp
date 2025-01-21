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
            <sec:authentication property="principal" var="user" />


            <%-- 사용자 정보 표시 --%>
            <c:set var="principal" value="${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal}" />

            <%-- 사용자 이름 표시 --%>
            <c:choose>
                <%-- 일반 로그인 --%>
                <c:when test="${principal['class'].simpleName eq 'CustomUsersDetails'}">
                    님
                </c:when>
                <%-- OAuth2 로그인 (구글) --%>
                <c:when test="${principal['class'].simpleName eq 'CustomOIDCUsers'}">
                    ${principal.nickname}님
                </c:when>
                <%-- OAuth2 로그인 (네이버) --%>
                <c:when test="${principal['class'].simpleName eq 'CustomOAuth2Users'}">
                    ${principal.nickname}님
                </c:when>
            </c:choose>


            <a href="/logout">로그아웃</a>

        </span>
        <pre>
            <!-- 디버깅용 출력 -->
            Principal Type: ${user.getClass().name}<br>
            Is OAuth2: ${user.attributes != null}<br>
            Properties: ${user}
        </pre>
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
