<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Login</title>
</head>
<body>
    <div class="nav">
        <a class="logo">VocaLab</a>

        <sec:authorize access="isAuthenticated()">
            <span>
                <%-- Principal 객체를 변수로 저장 --%>
                <sec:authentication property="principal" var="user" />

                <%-- OAuth2 로그인인 경우 --%>
                <c:if test="${user.attributes != null}">
                    <c:choose>
                        <c:when test="${user.attributes.containsKey('name')}">
                            ${user.attributes.name}님 <%-- 구글 --%>
                        </c:when>
                        <c:when test="${user.attributes.containsKey('response')}">
                            ${user.attributes.response.name}님 <%-- 네이버 --%>
                        </c:when>
                    </c:choose>
                </c:if>

                <%-- 일반 로그인인 경우 --%>
                <%-- Spring Security의 기본 UserDetails 구현에서는 username이 userId에 매핑--%>
                <c:if test="${user.attributes == null}">
                    <sec:authentication property="principal.username" />님
                </c:if>

                <a href="/logout">로그아웃</a>
            </span>
            <pre>
                <!-- 디버깅용 출력 -->
                Principal Type: ${user.getClass().name}
                Is OAuth2: ${user.attributes != null}
                Properties: ${user}
            </pre>
        </sec:authorize>

        <sec:authorize access="!isAuthenticated()">
            <span>
                <a href="/login">로그인</a>
            </span>
        </sec:authorize>
    </div>
    <h4>로그인페이지</h4>

    <c:if test="${param.error != null}">
        <h4>아이디나 비밀번호가 일치하지 않습니다.</h4>
    </c:if>

    <form action="/login-proc" method="POST">
        <input name="userId" required><br>
        <input name="userPassword" type="password" required><br>
        <button type="submit">로그인</button>
        <a href="${pageContext.request.contextPath}/register" class="btn">회원가입</a>
    </form>

    <!-- OAuth2 로그인 버튼 -->
    <div class="social-login">
        <p>소셜 계정으로 로그인</p>
        <a href="/oauth2/authorization/google" class="social-btn google-btn">
            <img src="https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg" alt="Google Logo">
            Google 로그인
        </a>
        <a href="/oauth2/authorization/naver" class="social-btn naver-btn">
            <img src="https://play-lh.googleusercontent.com/Kbu0747Cx3rpzHcSbtM1zDriGFG74zVbtkPmVnOKpmLCS59l7IuKD5M3MKbhqKyS-ml8" alt="Naver Logo">
            네이버 로그인
        </a>
    </div>
        <button type="submit">로그인</button>
        <a href="${pageContext.request.contextPath}/register" class="btn">회원가입</a>
        <%-- <button class="btn" onclick="location.href='/signup'">회원가입</button> --%>
    </form>
</body>
</html>