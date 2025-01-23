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

$(document).ready(function () {
    const wordBookId = $('#wordBookId').val(); // JSP에서 전달받은 ID

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
            url: 'http://localhost:5000/Python/word-mean',
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
    });

    // 초기화
    $('#selectedWordsModal').hide();
    $('#btnRestoreModal').hide();
    fetchWordData();
});