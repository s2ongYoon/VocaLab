<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
        <c:forEach items="${wordbooks}" var="wordbook">
            <div class="wordbook-card"
                 data-created="${wordbook.wordBookId}"
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
<div class="modal fade" id="wordbookModal" tabindex="-1" aria-labelledby="modalWordbookTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalWordbookTitle"></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
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
</body>
</html>
<script>
    $(document).ready(function () {
        // 전역 변수로 현재 선택된 단어장 정보 관리
        let selectedWordBook = null;

        // 상수 정의
        const MESSAGES = {
            DELETE_CONFIRM: (title) => `단어장 "${title}"을(를) 삭제하시겠습니까?`,
            DELETE_SUCCESS: (title) => `단어장 "${title}"이(가) 삭제되었습니다.`,
            FAVORITE_TOGGLE: (title, action) => `단어장 "${title}"의 즐겨찾기 상태가 변경되었습니다.`,
            PAGE_MOVE: (page, title) => `${page} 페이지로 이동합니다. 단어장: "${title}"`
        };

        // 모달 UI 업데이트 함수
        function updateModalUI() {
            if (selectedWordBook) {
                $('#modalWordbookTitle').text(`"${selectedWordBook.title}" 단어장 작업`);
                $('#btnToggleFavorite').text(
                    selectedWordBook.bookmark ? '즐겨찾기 해제' : '즐겨찾기 등록'
                );
            }
        }

        // 모달 이벤트 바인딩
        function bindModalEvents() {
            // 단어장 이동
            $('#btnMoveWordbook').on('click', function() {
                if (selectedWordBook) {
                    const wordBookId = selectedWordBook.id;
                    window.location.href = '/WordBook/Word?wordBookId=' + wordBookId;
                }
            });

            // 단어장 삭제
            $('#btnDeleteWordbook').on('click', function() {
                if (selectedWordBook && confirm(MESSAGES.DELETE_CONFIRM(selectedWordBook.title))) {
                    $.ajax({
                        url: '/api/wordbook/' + selectedWordBook.id,
                        method: 'DELETE',
                        success: function() {
                            alert(MESSAGES.DELETE_SUCCESS(selectedWordBook.title));
                            $('#wordbookModal').modal('hide');
                            // 카드 제거
                            $(`[data-id="${selectedWordBook.id}"]`).remove();
                        }
                    });
                }
            });

            // 즐겨찾기 토글
            $('#btnToggleFavorite').on('click', function() {
                if (selectedWordBook) {
                    const action = selectedWordBook.bookmark ? '해제' : '등록';
                    const wordBookId = selectedWordBook.id;
                    $.ajax({
                        url: '/WordBook/bookmark',  // URL 경로 수정
                        method: 'POST',
                        data: {
                            wordBookId: wordBookId,
                            bookmark: !selectedWordBook.bookmark
                        },
                        success: function(response) {
                            if (response.success) {
                                selectedWordBook.bookmark = !selectedWordBook.bookmark;
                                alert(MESSAGES.FAVORITE_TOGGLE(selectedWordBook.title, action));
                                updateModalUI();

                                window.location.reload();
                            }
                        },
                        error: function(xhr, status, error) {
                            console.error('Error:', error);
                            alert('즐겨찾기 변경에 실패했습니다.');
                        }
                    });
                }
            });

            // 기능별 페이지 이동
            ['Writing', 'News', 'Test'].forEach(function(page) {
                $(`#btnGo${page}`).on('click', function() {
                    if (selectedWordBook) {
                        alert(MESSAGES.PAGE_MOVE(page, selectedWordBook.title));
                        // TODO: 실제 페이지 이동 구현
                        window.location.href = `/WordBook/${page.toLowerCase()}?wordBookId=${selectedWordBook.id}`;
                    }
                });
            });
        }

        // 카드 클릭 이벤트 바인딩
        $('#wordbooksContainer').on('click', '.wordbook-card', function() {
            const $card = $(this);
            selectedWordBook = {
                id: $card.attr('data-created'),
                title: $card.attr('data-title'),
                bookmark: $card.attr('data-bookmark') === 'true'
            };

            console.log('Selected wordbook:', selectedWordBook);
            updateModalUI();
            $('#wordbookModal').modal('show');
        });

        // 정렬 기능 구현
        $('#sortBy').on('change', function() {
            const sortValue = $(this).val();
            const $container = $('#wordbooksContainer');
            const $cards = $container.children('.wordbook-card').get();

            $cards.sort(function(a, b) {
                const $a = $(a);
                const $b = $(b);

                switch(sortValue) {
                    case 'createdAsc':
                        return $a.attr('data-created').localeCompare($b.attr('data-created'));
                    case 'createdDesc':
                        return $b.attr('data-created').localeCompare($a.attr('data-created'));
                    case 'titleAsc':
                        return $a.attr('data-title').localeCompare($b.attr('data-title'));
                    default:
                        return 0;
                }
            });

            $container.append($cards);
        });

        // 즐겨찾기 필터 기능 구현
        $('#showFavorites').on('change', function() {
            const showOnlyFavorites = $(this).is(':checked');
            $('.wordbook-card').each(function() {
                const $card = $(this);
                const isBookmarked = $card.attr('data-bookmark') === 'true';
                $card.toggle(!showOnlyFavorites || isBookmarked);
            });
        });

        // 초기 이벤트 바인딩
        bindModalEvents();
    });
</script>

</body>
</html>
