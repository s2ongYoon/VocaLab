<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>VocaLab</title>
        <!-- CSS 파일 경로 -->
        <link rel="stylesheet" type="text/css" href="/css/home.css">
        <link rel="stylesheet" type="text/css" href="/css/home_file_preview.css">
        <link rel="stylesheet" type="text/css" href="/css/top.css">
        <link rel="stylesheet" type="text/css" href="/css/bottom.css">
        <link rel="stylesheet" type="text/css" href="/css/global.css">
        <!-- jQuery CDN -->
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <!-- JavaScript 파일 경로 -->
        <script src="/js/home.js"></script>
        <script src="/js/top.js"></script> <!-- 배너 -->
        <script src="/js/bottm.js"></script> <!-- 배너 -->
    </head>
    <body>
        <div class="page-container">
            <!-- top배너 삽입 -->
            <div id="banner_top" role="banner">
                <%@ include file="banners/top_center.jsp" %>
            </div>
            <!-- 본문 내용 -->
            <div class="container">
                <!--회원일 경우 검색 기록이 표시됨 -->
                <div class="compile-record">
                    <c:if test="${not empty sessionScope.sessionId || not empty compileDtoList}">
                        <c:forEach var="com" items="${compileDtoList}">
                            <c:forEach var="file" items="${com.fileRecordList}" varStatus="status">
                                <input type="hidden" name="file${status.index}" value="${file.fileId}">
                            </c:forEach>
                            <button class="user-record">
                                    ${com.createAt}
                            </button>
                        </c:forEach>
                    </c:if>
                </div>

                <!-- form 태그 -->
                <form class="input-group" name="compileForm" action="/Compile/Result" method="post" enctype="multipart/form-data">
                    <!-- 파일 업로드 아이콘 -->
                    <label for="file-upload" class="file-icon">
                        <img src="/images/file.png" alt="파일 업로드" width="25" height="25">
                    </label>
                    <input id="file-upload" name="files" type="file" multiple style="display: none;">
                    <!-- URL 또는 텍스트 입력 -->
                    <input type="text" id="url-input" name="source" placeholder="URL 또는 텍스트를 입력하세요">
                    <!-- 단어 추출 버튼 -->
                    <button type="submit" class="btn-extract2" >단어 추출</button>
                </form>
                <!-- 파일 미리보기 리스트 -->
                <div id="file-preview-list" class="file-preview"></div>
            </div>

            <div class="banner_bottom">
                <%@ include file="./banners/bottom.jsp" %>
            </div>
        </div>
    </body>
</html>
