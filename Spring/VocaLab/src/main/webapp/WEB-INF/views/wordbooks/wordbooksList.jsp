<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${userNickname}님의 단어장 목록</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <!-- jQuery -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <!-- Bootstrap 5 Bundle JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <style>
        .wordbook-container {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            justify-content: flex-start; /* 오른쪽부터 빈 공간이 생기도록 설정 */
        }

        .wordbook-card {
            flex: 0 0 calc(25% - 15px); /* 4열 구성 */
            max-width: calc(25% - 15px);
            border: 1px solid #dee2e6;
            border-radius: 5px;
            padding: 15px;
            transition: transform 0.2s ease-in-out;
            background-color: #fff;
            box-sizing: border-box;
        }

        .wordbook-card:hover {
            transform: scale(1.05);
        }
    </style>
</head>
<body>
<div class="container py-4">
    <h1>${userNickname}님의 단어장 목록</h1>

    <!-- 정렬 및 필터링 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <select id="sortBy" class="form-select" style="width: 200px;">
                <option value="createdAsc">생성순</option>
                <option value="createdDesc">최신순</option>
                <option value="titleAsc">가나다 순</option>
            </select>
        </div>
        <div>
            <input type="checkbox" id="showFavorites" class="form-check-input">
            <label for="showFavorites" class="form-check-label">즐겨찾기만 보기</label>
        </div>
    </div>
    <div id="wordbooksContainer" class="wordbook-container">
        <!-- 하드코딩된 데이터 -->
        <c:forEach begin="1" end="15" var="i">
            <div class="wordbook-card" data-created="${i * 100000}"
                 data-title="단어장 ${i}"
                 data-bookmark="${i % 2 == 0}">
                <h5>단어장 ${i}</h5>
                <p>생성일: 2025-01-${i}</p>
                <c:if test="${i % 2 == 0}">
                    <span class="badge bg-warning text-dark">즐겨찾기</span>
                </c:if>
            </div>
        </c:forEach>
    </div>
    <!-- 단어장 카드 목록 -->
    <div id="wordbooksContainer-real" class="wordbook-container">
        <!-- 실제 데이터 기반 -->
        <c:forEach items="${wordbooks}" var="wordbook">
            <div class="wordbook-card" data-created="${wordbook.createdAt.time}"
                 data-title="${wordbook.wordBookTitle}"
                 data-bookmark="${wordbook.bookmark}">
                <h5>${wordbook.wordBookTitle}</h5>
                <p>생성일: ${wordbook.createdAt}</p>
                <c:if test="${wordbook.bookmark}">
                    <span class="badge bg-warning text-dark">즐겨찾기</span>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>
</body>
</html>
