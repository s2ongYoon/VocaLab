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
        <link rel="stylesheet" type="text/css" href="/css/global.css">ㅊ
        <!-- JavaScript 파일 경로 -->
        <script src="/js/top.js"></script> <!-- 배너 -->
        <script src="/js/bottom.js"></script> <!-- 배너 -->
    </head>
    <body>
    <div class="member-edit-page">
        <h2>회원 정보 수정</h2>
        <form>
            <label for="password">비밀번호:</label><input type="password" id="password"><br>
            <label for="confirm-password">비밀번호 확인:</label><input type="password" id="confirm-password"><br>
            <label for="nickname">닉네임:</label><input type="text" id="nickname"><br>
            <button type="button" onclick="submitEdit()">수정완료</button>
        </form>
        <a href="#" class="withdraw">회원탈퇴</a>
    </div>

    </body>
</html>