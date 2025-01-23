<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
</head>
    <body>
<%--        <sec:authorize access="isAuthenticated()">--%>
<%--            <sec:authentication property="principal" var="user" />--%>
<%--            <pre>--%>
<%--                <!-- 디버깅용 출력 -->--%>
<%--                Principal Type: ${user.getClass().name}<br>--%>
<%--                Is OAuth2: ${user.attributes != null}<br>--%>
<%--                Properties: ${user}--%>
<%--            </pre>--%>
<%--            <a href="/logout">로그아웃</a>--%>
<%--        </sec:authorize>--%>



            <a href="/logout">로그아웃</a>


    </body>
</html>