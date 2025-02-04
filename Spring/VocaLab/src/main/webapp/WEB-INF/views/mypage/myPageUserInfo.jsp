<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>VocaLab-mypage</title>
        <!-- CSS 파일 경로 -->
        <link rel="stylesheet" type="text/css" href="/css/top.css">
        <link rel="stylesheet" type="text/css" href="/css/bottom.css">
        <link rel="stylesheet" type="text/css" href="/css/global.css">
        <!-- JavaScript 파일 경로 -->
        <script src="/js/top.js"></script> <!-- 배너 -->
        <script src="/js/bottom.js"></script> <!-- 배너 -->
    </head>
    <body>
    <div class="member-info-page">
        <h2>회원 상세 정보</h2>
        <form>
            <label for="username">아이디:</label><input type="text" id="username" disabled><br>
            <label for="password">비밀번호:</label><input type="password" id="password" disabled><br>
            <label for="name">이름:</label><input type="text" id="name" disabled><br>
            <label for="nickname">닉네임:</label><input type="text" id="nickname" disabled><br>
            <label for="email">이메일:</label><input type="email" id="email" disabled><br>
            <label for="dob">생년월일:</label><input type="date" id="dob" disabled><br>
            <label for="gender">성별:</label><input type="text" id="gender" disabled><br>
            <button type="button" onclick="goToEditPage()">회원정보 수정</button>
        </form>
    </div>

    </body>
</html>