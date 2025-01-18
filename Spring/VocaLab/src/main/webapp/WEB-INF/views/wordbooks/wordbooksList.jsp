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
        .modal-content {
            border-radius: 10px;
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
<!-- 모달 -->
<div class="modal fade" id="wordbookModal" tabindex="-1" aria-labelledby="wordbookModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="wordbookModalLabel">단어장 작업</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p id="modalWordbookTitle"></p>
                <div class="d-flex flex-column gap-3">
                    <button class="btn btn-primary" id="btnMoveWordbook">단어장 이동</button>
                    <button class="btn btn-danger" id="btnDeleteWordbook">단어장 삭제</button>
                    <button class="btn btn-warning text-dark" id="btnToggleFavorite"></button>
                    <button class="btn btn-secondary" id="btnGoWriting">작문 이동</button>
                    <button class="btn btn-secondary" id="btnGoNews">뉴스 이동</button>
                    <button class="btn btn-secondary" id="btnGoTest">테스트 이동</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {
        // 카드 클릭 이벤트
        $('.wordbook-card').on('click', function () {
            const wordBookId = $(this).data('id');
            const wordBookTitle = $(this).data('title');
            const isBookmark = $(this).data('bookmark');

            // 모달 타이틀 업데이트
            $('#modalWordbookTitle').text(`"${wordBookTitle}" 단어장 작업`);

            // 즐겨찾기 버튼 텍스트 업데이트
            if (isBookmark) {
                $('#btnToggleFavorite').text('즐겨찾기 해제');
            } else {
                $('#btnToggleFavorite').text('즐겨찾기 등록');
            }

            // 모달 데이터에 단어장 ID 저장
            $('#btnMoveWordbook').data('id', wordBookId);
            $('#btnDeleteWordbook').data('id', wordBookId);
            $('#btnToggleFavorite').data('id', wordBookId);
            $('#btnGoWriting').data('id', wordBookId);
            $('#btnGoNews').data('id', wordBookId);
            $('#btnGoTest').data('id', wordBookId);

            // 모달 표시
            $('#wordbookModal').modal('show');
        });

        // 단어장 이동
        $('#btnMoveWordbook').on('click', function () {
            const wordBookId = $(this).data('id');
            // window.location.href = `/WordBook/Word`;
            window.location.href = `/WordBook/Word?wordBookId=1<%--${wordBookId}--%>`;
        });

        // 단어장 삭제
        $('#btnDeleteWordbook').on('click', function () {
            const wordBookId = $(this).data('id');
            if (confirm(`단어장 ${wordBookId}을(를) 삭제하시겠습니까?`)) {
                alert(`단어장 ${wordBookId}이(가) 삭제되었습니다.`);
                // 삭제 로직 추가
            }
        });

        // 즐겨찾기 등록/해제
        $('#btnToggleFavorite').on('click', function () {
            const wordBookId = $(this).data('id');
            const action = $(this).text() === '즐겨찾기 등록' ? '등록' : '해제';
            alert(`단어장 ${wordBookId}이(가) 즐겨찾기 ${action}되었습니다.`);
            // 즐겨찾기 토글 로직 추가
        });

        // 작문 이동
        $('#btnGoWriting').on('click', function () {
            const wordBookId = $(this).data('id');
            alert(`작문 페이지로 이동합니다. 단어장 ID: ${wordBookId}`);
            // 작문 페이지 이동 로직 추가
        });

        // 뉴스 이동
        $('#btnGoNews').on('click', function () {
            const wordBookId = $(this).data('id');
            alert(`뉴스 페이지로 이동합니다. 단어장 ID: ${wordBookId}`);
            // 뉴스 페이지 이동 로직 추가
        });

        // 테스트 이동
        $('#btnGoTest').on('click', function () {
            const wordBookId = $(this).data('id');
            alert(`테스트 페이지로 이동합니다. 단어장 ID: ${wordBookId}`);
            // 테스트 페이지 이동 로직 추가
        });
    });
</script>
</body>
</html>
