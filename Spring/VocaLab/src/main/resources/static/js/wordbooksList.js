// 상수 정의
const MESSAGES = {
    DELETE_CONFIRM: (title) => `단어장 "${title}"을(를) 삭제하시겠습니까?`,
    DELETE_SUCCESS: (title) => `단어장 "${title}"이(가) 삭제되었습니다.`,
    FAVORITE_TOGGLE: (title, action) => `단어장 "${title}"의 즐겨찾기 상태가 변경되었습니다.`,
    PAGE_MOVE: (page, title) => `${page} 페이지로 이동합니다. 단어장: "${title}"`
};

class WordbookManager {
    constructor() {
        this.selectedWordBook = null;
        this.initializeEventListeners();
    }

    // 모달 UI 업데이트
    updateModalUI() {
        if (this.selectedWordBook && this.selectedWordBook.title) {
            console.log('Updating modal UI with:', this.selectedWordBook);
            const modalTitle = this.selectedWordBook.title;
            $('#modalWordbookTitle').text(modalTitle);
            const bookmarkText = this.selectedWordBook.bookmark ? '즐겨찾기 해제' : '즐겨찾기 등록';
            $('#btnToggleFavorite').text(bookmarkText);
        }
    }

    // 모달 이벤트 바인딩
    bindModalEvents() {
        $('#btnMoveWordbook').on('click', () => this.handleWordbookMove());
        $('#btnDeleteWordbook').on('click', () => this.handleWordbookDelete());
        $('#btnToggleFavorite').on('click', () => this.handleFavoriteToggle());
        $('.modal-body').on('click', '[id^=btnGo]', (e) => this.handlePageNavigation(e));
        $('#btnEditTitle').on('click', () => this.handleTitleEdit());
    }

    // 단어장 이동 처리
    handleWordbookMove() {
        if (this.selectedWordBook) {
            window.location.href = '/WordBook/Word?wordBookId=' + this.selectedWordBook.id;
        }
    }

    // 단어장 삭제 처리
    handleWordbookDelete() {
        if (this.selectedWordBook && confirm(MESSAGES.DELETE_CONFIRM(this.selectedWordBook.title))) {
            $.ajax({
                url: '/api/wordbook/' + this.selectedWordBook.id,
                method: 'DELETE',
                success: () => {
                    alert(MESSAGES.DELETE_SUCCESS(this.selectedWordBook.title));
                    $('#wordbookModal').modal('hide');
                    $(`[data-id="${this.selectedWordBook.id}"]`).remove();
                }
            });
        }
    }

    // 즐겨찾기 토글 처리
    handleFavoriteToggle() {
        if (this.selectedWordBook) {
            const action = this.selectedWordBook.bookmark ? '해제' : '등록';
            $.ajax({
                url: '/WordBook/bookmark',
                method: 'POST',
                data: {
                    wordBookId: this.selectedWordBook.id,
                    bookmark: !this.selectedWordBook.bookmark
                },
                success: (response) => {
                    if (response.success) {
                        this.selectedWordBook.bookmark = !this.selectedWordBook.bookmark;
                        alert(MESSAGES.FAVORITE_TOGGLE(this.selectedWordBook.title, action));
                        this.updateModalUI();
                        window.location.reload();
                    }
                },
                error: (xhr, status, error) => {
                    console.error('Error:', error);
                    alert('즐겨찾기 변경에 실패했습니다.');
                }
            });
        }
    }

    // 페이지 이동 처리
    handlePageNavigation(event) {
        if (!this.selectedWordBook) {
            console.log('No wordbook selected');
            return;
        }

        const buttonId = event.target.id;
        const pageType = this.getPageTypeFromButtonId(buttonId);
        if (!pageType) return;

        this.navigateToPage(pageType);
    }

    // 버튼 ID로부터 페이지 타입 결정
    getPageTypeFromButtonId(buttonId) {
        const pageTypes = {
            btnGoWriting: 'Writing',
            btnGoNews: 'News',
            btnGoTest: 'Test'
        };
        return pageTypes[buttonId];
    }

    // 페이지 이동 실행
    navigateToPage(pageType) {
        $.ajax({
            url: '/WordBook/getWords',
            type: 'GET',
            data: { wordBookId: this.selectedWordBook.id },
            success: (words) => {
                if (this.validateNavigation(pageType, words)) {
                    sessionStorage.setItem('selectedWords', JSON.stringify(words));
                    window.location.href = `/WordBook/${pageType}?wordBookId=${this.selectedWordBook.id}`;
                }
            },
            error: (xhr, status, error) => {
                console.error('Error:', error);
                alert('단어 데이터를 가져오는 중 오류가 발생했습니다.');
            }
        });
    }

    // 페이지 이동 유효성 검사
    validateNavigation(pageType, words) {
        if (pageType === 'Test') {
            if (words.length < 20) {
                alert('테스트를 진행하려면 단어장에 20개 이상의 단어가 필요합니다.');
                return false;
            }
            return confirm('테스트를 시작하시겠습니까?');
        }
        return true;
    }

    // 제목 수정 처리
    handleTitleEdit() {
        const currentTitle = this.selectedWordBook.title;
        const $title = $('#modalWordbookTitle');
        this.showTitleEditForm($title, currentTitle);
    }

    // 제목 수정 폼 표시
    showTitleEditForm($title, currentTitle) {
        $title.html(`
            <div class="input-group">
                <input type="text" class="form-control" id="newTitle" value="${currentTitle}">
                <button class="btn btn-success btn-sm" id="btnSaveTitle">저장</button>
                <button class="btn btn-secondary btn-sm" id="btnCancelEdit">취소</button>
            </div>
        `);

        $('#btnSaveTitle').on('click', () => this.handleTitleSave(currentTitle));
        $('#btnCancelEdit').on('click', () => this.updateModalUI());
    }

    // 제목 저장 처리
    handleTitleSave(currentTitle) {
        const newTitle = $('#newTitle').val().trim();
        if (newTitle && newTitle !== currentTitle) {
            this.saveTitleToServer(newTitle);
        } else {
            this.updateModalUI();
        }
    }

    // 제목 서버 저장
    saveTitleToServer(newTitle) {
        $.ajax({
            url: '/WordBook/updateTitle',
            method: 'POST',
            data: {
                wordBookId: this.selectedWordBook.id,
                newTitle: newTitle
            },
            success: (response) => {
                if (response.success) {
                    this.selectedWordBook.title = newTitle;
                    $(`.wordbook-card[data-wordbook-id="${this.selectedWordBook.id}"] h5`).text(newTitle);
                    this.updateModalUI();
                } else {
                    alert('제목 변경에 실패했습니다.');
                }
            },
            error: () => {
                alert('제목 변경 중 오류가 발생했습니다.');
            }
        });
    }

    // 정렬 처리
    handleSort(sortValue) {
        const $container = $('#wordbooksContainer');
        const $cards = $container.children('.wordbook-card').get();

        $cards.sort((a, b) => {
            const $a = $(a);
            const $b = $(b);

            switch(sortValue) {
                case 'createdAsc':
                    return $a.data('wordbook-id') - $b.data('wordbook-id');
                case 'createdDesc':
                    return $b.data('wordbook-id') - $a.data('wordbook-id');
                case 'titleAsc':
                    return $a.data('wordbook-title').localeCompare($b.data('wordbook-title'));
                default:
                    return 0;
            }
        });

        $container.append($cards);
    }

    // 즐겨찾기 필터 처리
    handleFavoriteFilter(showOnlyFavorites) {
        $('.wordbook-card').each(function() {
            const $card = $(this);
            const isBookmarked = $card.data('wordbook-bookmark');
            $card.toggle(!showOnlyFavorites || isBookmarked);
        });
    }

    // 이벤트 리스너 초기화
    initializeEventListeners() {
        $('#wordbooksContainer').on('click', '.wordbook-card', (e) => {
            const $card = $(e.currentTarget);
            this.selectedWordBook = {
                id: $card.data('wordbook-id'),
                title: $card.data('wordbook-title'),
                bookmark: $card.data('wordbook-bookmark')
            };
            console.log('Selected wordbook:', this.selectedWordBook);
            this.updateModalUI();
            $('#wordbookModal').modal('show');
        });

        $('#sortBy').on('change', (e) => this.handleSort($(e.target).val()));
        $('#showFavorites').on('change', (e) => this.handleFavoriteFilter($(e.target).is(':checked')));
        this.bindModalEvents();
    }
}

// 문서 로드 완료 시 WordbookManager 인스턴스 생성
$(document).ready(() => {
    new WordbookManager();
});