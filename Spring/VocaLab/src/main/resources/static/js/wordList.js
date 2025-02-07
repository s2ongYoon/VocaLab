let currentPage = 0;
const itemsPerPage = 54;
let isLoading = false;
let hasMoreData = true;
let isMouseDown = false;
let lastClickedIndex = null;
let timeoutId = null;
let wordData = []; // 서버에서 로드한 데이터 저장
const selectedWords = new Map(); // 선택된 단어 관리
let isModalMinimized = false; // 최소화 상태 여부
let originalOrder = [];

$(document).ready(function () {
    const wordBookId = $('#wordBookId').val(); // JSP에서 전달받은 ID
    // 초기화
    $('#selectedWordsModal').hide();
    $('#btnRestoreModal').hide();
    // 초기 데이터 로드
    loadWordBookInfo();
    fetchWordData();
    // 단어장 정보 로드 함수
    function loadWordBookInfo() {
        $.ajax({
            url: '/WordBook/WordBookData',
            type: 'GET',
            data: { wordBookId },
            success: function(response) {
                var titleHtml = response.wordBookTitle;
                if (response.bookmark) {
                    titleHtml = response.wordBookTitle + ' <i class="fas fa-star text-warning"></i>';
                }
                $('#title-text').html(titleHtml);
            },
            error: function(xhr) {
                console.error('단어장 정보를 불러오는데 실패했습니다.');
            }
        });
    }

    // 서버 데이터 로드
    function fetchWordData() {
        console.log("wordBookId: " + wordBookId);
        return $.ajax({
            url: '/WordBook/WordData',
            type: 'GET',
            data: { wordBookId },
            success: function (data) {
                console.log('Server Data:', data);
                renderWordTable(data);
            },
            error: function () {
                alert('단어 데이터를 불러오는 데 실패했습니다.');
            }
        });
    }

    // 테이블 렌더링
    function renderWordTable(data) {
        const tableBody = $('#wordTableBody');
        tableBody.empty();
        originalOrder = [...data];

        for (let i = 0; i < data.length; i += 6) {
            let rowHtml = '<tr>';

            for (let j = i; j < i + 6 && j < data.length; j++) {
                const word = data[j][0] || 'Unknown';
                const meaning = data[j][1] || 'No meaning';
                const type = data[j][2] || 'N/A';
                const example = data[j][3] || 'No example';

                rowHtml += `
                    <td data-word="${word}" data-meaning="${meaning}" data-type="${type}" data-example="${example}">
                        <div style="font-size: 16px; font-weight: bold;">${word}</div>
                        <div style="font-size: 12px; color: gray;">${meaning}</div>
                    </td>`;
            }

            rowHtml += '</tr>';
            tableBody.append(rowHtml);
        }
    }

    // 단어 선택 관련 기능
    $('.word-table').on('mousedown', 'td', function (event) {
        isMouseDown = true;
        const currentIndex = $('.word-table td').index(this);
        const word = $(this).data('word');
        const type = $(this).data('type');
        const meaning = $(this).data('meaning');
        const example = $(this).data('example');

        if (event.shiftKey && lastClickedIndex !== null) {
            const start = Math.min(lastClickedIndex, currentIndex);
            const end = Math.max(lastClickedIndex, currentIndex);
            const cells = $('.word-table td').slice(start, end + 1);

            if (cells.toArray().every(td => $(td).hasClass('highlight'))) {
                cells.removeClass('highlight');
                cells.each(function () {
                    const wordToRemove = $(this).data('word');
                    selectedWords.delete(wordToRemove);
                });
            } else {
                cells.addClass('highlight');
                cells.each(function () {
                    const wordToAdd = $(this).data('word');
                    selectedWords.set(wordToAdd, {
                        word: wordToAdd,
                        type: $(this).data('type'),
                        meaning: $(this).data('meaning'),
                        example: $(this).data('example')
                    });
                });
            }
        } else {
            if ($(this).hasClass('highlight')) {
                $(this).removeClass('highlight');
                selectedWords.delete(word);
            } else {
                $(this).addClass('highlight');
                selectedWords.set(word, { word, type, meaning, example });
            }
        }

        lastClickedIndex = currentIndex;
        updateSelectedWordsModal();

        // 단어 상세 정보 모달
        timeoutId = setTimeout(() => {
            showWordDetailModal(word);
        }, 1000);
    });

    function showWordDetailModal(word) {
        $('#modalWord').text("로딩중 입니다.");
        $('#modalType').text("로딩중 입니다.");
        $('#modalMeaning').text("로딩중 입니다.");
        $('#modalExample').text("로딩중 입니다.");
        $('#modalSpeech').text("로딩중 입니다.");
        $('#wordDetailModal').modal('show');

        $.ajax({
            url: 'https://pywm.21v.in/Python/word-mean',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ word: word }),
            success: function (response) {
                $('#modalWord').text(response.word || word);
                $('#modalType').text(response.partofspeech || 'N/A');
                $('#modalMeaning').text(response.mean || 'N/A');
                $('#modalExample').text(response.example || 'N/A');
                $('#modalSpeech').text(response.speech || 'N/A');
            },
            error: function (xhr, status, error) {
                console.error('Failed to fetch data from Python API:', error);
                alert('단어 정보를 불러오는 데 실패했습니다.');
            }
        });
    }

    // 선택된 단어 모달 관리
    function updateSelectedWordsModal() {
        const modalList = $('#selectedWordsList');
        modalList.empty();

        if (selectedWords.size === 0) {
            $('#selectedWordsModal').hide();
            $('#btnRestoreModal').hide();
        } else if (!isModalMinimized) {
            $('#selectedWordsModal').show();
            $('#btnRestoreModal').hide();

            selectedWords.forEach((data, word) => {
                const listItem = $(`
                    <li class="list-group-item d-flex justify-content-between align-items-center word-item">
                        <span data-word="${data.word}" 
                              data-type="${data.type}" 
                              data-meaning="${data.meaning}" 
                              data-example="${data.example}">
                              ${word}</span>
                        <button class="btn btn-sm btn-danger btn-remove-word" data-word="${word}">X</button>
                    </li>
                `);
                modalList.append(listItem);
            });
        } else {
            $('#btnRestoreModal').show();
        }
    }

    // 이벤트 핸들러들
    $('.word-table').on('mouseup mouseleave', 'td', function () {
        clearTimeout(timeoutId);
        isMouseDown = false;
    });

    $('.word-table').on('mousemove', 'td', function () {
        if (isMouseDown) {
            $(this).addClass('highlight');
            const word = $(this).data('word');
            if (!selectedWords.has(word)) {
                selectedWords.set(word, {
                    word,
                    type: $(this).data('type'),
                    meaning: $(this).data('meaning'),
                    example: $(this).data('example')
                });
            }
            updateSelectedWordsModal();
        }
    });

    $('#selectedWordsList').on('click', '.btn-remove-word', function () {
        const wordToRemove = $(this).data('word');

        $('.word-table td').each(function () {
            if ($(this).data('word') === wordToRemove) {
                $(this).removeClass('highlight');
            }
        });

        selectedWords.delete(wordToRemove);
        updateSelectedWordsModal();
    });

    $('#selectedWordsList').on('click', '.word-item span', function () {
        const word = $(this).data('word');
        showWordDetailModal(word);
    });

    // 모달 컨트롤 버튼들
    $('#btnMinimizeModal').on('click', function () {
        $('#selectedWordsModal').hide();
        $('#btnRestoreModal').show();
        isModalMinimized = true;
    });

    $('#btnRestoreModal').on('click', function () {
        $('#selectedWordsModal').show();
        $('#btnRestoreModal').hide();
        isModalMinimized = false;
    });

    $('#btnSelectAll').on('click', function () {
        $('.word-table td').each(function () {
            $(this).addClass('highlight');
            const word = $(this).data('word');
            selectedWords.set(word, {
                word,
                type: $(this).data('type'),
                meaning: $(this).data('meaning'),
                example: $(this).data('example')
            });
        });
        updateSelectedWordsModal();
    });

    $('#btnDeselectAll').on('click', function () {
        $('.word-table td').removeClass('highlight');
        selectedWords.clear();
        updateSelectedWordsModal();
    });

    // 검색 기능
    $('#searchInput').on('input', function () {
        const searchKeyword = $(this).val().toLowerCase();
        $('.word-table td').each(function () {
            const word = $(this).data('word').toLowerCase();
            const meaning = $(this).data('meaning').toLowerCase();
            $(this).toggle(word.includes(searchKeyword) || meaning.includes(searchKeyword));
        });
    });

    // 단어 삭제 기능
    $('#wordDeleteBtn').on('click', function() {
        const selectedWordList = Array.from(selectedWords.keys());
        if (selectedWordList.length === 0) {
            alert('최소 한개 이상 선택해주세요.');
            return;
        }
        if(confirm('정말 삭제 하시겠습니까?')){
            $.ajax({
                url: '/WordBook/WordData/remove',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    wordBookId: wordBookId,
                    words: selectedWordList
                }),
                success: function (response) {
                    alert('단어가 삭제 되었습니다.');
                    window.location.reload();
                },
                error: function (xhr, status, error) {
                    alert('데이터 전송에 실패했습니다.');
                    console.error('AJAX 에러:', error);
                }
            });
        }else{
            return;
        }
    });
    // 버튼 ID와 해당하는 API 엔드포인트를 매핑
    const pageMapping = {
        'btnTest': 'Test',
        'btnWrite': 'Writing',
        'btnNews': 'News'
    };

// 컨텐츠 버튼 공통 로직을 처리하는 함수
    function contentButtonClick(pageType,selectedWordList){
        if (!selectedWordList || selectedWordList.length === 0) {
            alert('선택된 단어가 없습니다.');
            return;
        }
        // 테스트의 경우 특별 처리
        if (pageType === 'Test' && selectedWordList.length < 20) {
            alert('테스트를 진행할 시 단어를 20개 이상 선택하셔야 합니다.');
            return;
        }
        const wordBookId = $('#wordBookId').val();

        // 선택된 단어 리스트를 세션 스토리지에 저장
        sessionStorage.setItem('selectedWords', JSON.stringify(selectedWordList));

        // 페이지 이동
        window.location.href = '/WordBook/' + pageType + '?wordBookId=' + wordBookId;
    }
    // 버튼들에 이벤트 리스너 등록
    $('#btnTest, #btnWrite, #btnNews').on('click', function() {
        const buttonId = $(this).attr('id');
        const pageType = pageMapping[buttonId];
        const selectedWordList = Array.from(selectedWords.keys());

        if (buttonId === 'btnTest' && !confirm("테스트를 시작하시겠습니까?")) {
            return;
        }

        contentButtonClick(pageType, selectedWordList);
    });

    $('#sortSelect').on('change', function () {
        const sortType = $(this).val();
        sortWordTable(sortType);
    });

    function sortWordTable(sortType) {
        let wordArray = $('#wordTableBody tr td').map(function () {
            return {
                element: $(this),
                word: $(this).data('word') || '',
                meaning: $(this).data('meaning') || '',
                isSelected: $(this).hasClass('highlight'), // 기존 하이라이트 여부 저장
                originalIndex: originalOrder.findIndex(item => item[0] === $(this).data('word')) // 원본 순서 인덱스
            };
        }).get();

        switch (sortType) {
            case 'original':
                wordArray.sort((a, b) => a.originalIndex - b.originalIndex); // 원래 순서대로 정렬
                break;
            case 'abc':
                wordArray.sort((a, b) => a.word.localeCompare(b.word, 'ko'));
                break;
            case 'zyx':
                wordArray.sort((a, b) => b.word.localeCompare(a.word, 'ko'));
                break;
            case 'meaning_asc':
                wordArray.sort((a, b) => a.meaning.localeCompare(b.meaning, 'ko'));
                break;
            case 'meaning_desc':
                wordArray.sort((a, b) => b.meaning.localeCompare(a.meaning, 'ko'));
                break;
            default:
                return;
        }

        // 정렬된 데이터를 다시 추가
        const tableBody = $('#wordTableBody');
        tableBody.empty();
        let rowHtml = '<tr>';

        wordArray.forEach((item, index) => {
            const tdHtml = item.element.prop('outerHTML');
            rowHtml += tdHtml.replace('class="', `class="${item.isSelected ? 'highlight ' : ''}`); // 선택된 단어 유지
            if ((index + 1) % 6 === 0) {
                rowHtml += '</tr><tr>';
            }
        });

        rowHtml += '</tr>';
        tableBody.append(rowHtml);
    }
    function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function validateTitle(title) {
        // HTML 태그 존재 여부 체크
        if (/<[^>]*>/g.test(title)) {
            alert('HTML 태그는 사용할 수 없습니다.');
            return false;
        }

        // 기존 길이 체크
        if (title.length < 1 || title.length > 50) {
            alert('제목은 1-50자 사이여야 합니다.');
            return false;
        }

        // 허용된 문자만 사용 가능하도록
        const validPattern = /^[가-힣a-zA-Z0-9\s,.()-_]+$/;
        if (!validPattern.test(title)) {
            alert('제목은 한글, 영문, 숫자와 일부 특수문자(,.()-_)만 사용할 수 있습니다.');
            return false;
        }

        return true;
    }

    // 제목 수정 처리
    $('#btnEditTitle').on('click', function() {
        const $titleText = $('#title-text');
        const currentTitle = $titleText.text();

        $titleText.html('<div class="input-group">' +
            '<input type="text" class="form-control" id="newTitle" value="' + escapeHtml(currentTitle) + '" maxlength="50">' +
            '<button class="btn btn-success btn-sm" id="btnSaveTitle">저장</button>' +
            '<button class="btn btn-secondary btn-sm" id="btnCancelEdit">취소</button>' +
            '</div>');

        $('#btnSaveTitle').on('click', function() {
            const newTitle = $('#newTitle').val().trim();

            if (!validateTitle(newTitle)) {
                // alert('제목은 1-50자의 한글, 영문, 숫자와 일부 특수문자(,.()-_)만 사용할 수 있습니다.');
                return;
            }

            if (newTitle && newTitle !== currentTitle) {
                const wordBookId = $('#wordBookId').val();
                const escapedTitle = escapeHtml(newTitle);

                $.ajax({
                    url: '/WordBook/updateTitle',
                    method: 'POST',
                    data: {
                        wordBookId: wordBookId,
                        newTitle: escapedTitle
                    },
                    success: function(response) {
                        if (response.success) {
                            $titleText.text(newTitle);
                            loadWordBookInfo();
                        } else {
                            alert('제목 변경에 실패했습니다.');
                            $titleText.text(currentTitle);
                        }
                    },
                    error: function() {
                        alert('제목 변경 중 오류가 발생했습니다.');
                        $titleText.text(currentTitle);
                    }
                });
            } else {
                $titleText.text(currentTitle);
            }
        });
        // 취소 버튼 클릭 시
        $('#btnCancelEdit').on('click', function() {
            $titleText.text(currentTitle);
        });
    });
});
