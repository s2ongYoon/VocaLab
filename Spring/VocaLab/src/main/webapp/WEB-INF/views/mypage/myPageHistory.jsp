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
        <div class="word-history-page">
            <h2>단어 생성 기록</h2>
            <div class="select-all">
                <label><input type="checkbox" id="select-all"> 전체선택/선택해제</label>
            </div>
            <div id="record-list">
                <!-- 무한 스크롤로 기록 일자 목록이 여기에 출력될 거야 -->
            </div>
        </div>
    </body>
</html>