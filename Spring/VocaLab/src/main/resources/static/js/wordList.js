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
    $(document).ready(function () {
        $(document).ready(function () {
            const wordBookId = $('#wordBookId').val(); // JSP에서 전달받은 ID
            let wordData = [];

            // 서버 데이터 로드
            function fetchWordData() {
                return $.ajax({
                    url: '/WordBook/WordData', type: 'GET', data: {wordBookId}, success: function (data) {
                        // console.log('Server Data:', data); // 서버에서 반환된 데이터 확인
                        renderWordTable(data);
                    }, error: function () {
                        alert('단어 데이터를 불러오는 데 실패했습니다.');
                    }
                });
            }


            // 테이블 렌더링
            function renderWordTable(data) {
                const tableBody = $('#wordTableBody');
                tableBody.empty(); // 기존 데이터를 초기화

                // 6개의 단어씩 한 행에 추가
                for (let i = 0; i < data.length; i += 6) {
                    let rowHtml = '<tr>'; // 새로운 행 시작

                    for (let j = i; j < i + 6 && j < data.length; j++) { // 최대 6개, 데이터 길이 초과 방지
                        const word = data[j][0] || 'Unknown'; // 기본값 설정
                        const meaning = data[j][1] || 'No meaning'; // 기본값 설정
                        const type = data[j][2] || 'N/A'; // 기본값 설정
                        const example = data[j][3] || 'No example'; // 기본값 설정

                        rowHtml += `
                <td data-word="${word}" data-meaning="${meaning}" data-type="${type}" data-example="${example}">
                    <div style="font-size: 16px; font-weight: bold;">${word}</div> <!-- 단어 -->
                    <div style="font-size: 12px; color: gray;">${meaning}</div> <!-- 의미 -->
                </td>`;
                    }

                    rowHtml += '</tr>'; // 행 종료
                    tableBody.append(rowHtml); // 테이블에 행 추가
                }
            }


            // 초기 데이터 로드
            fetchWordData();
        });
        loadMoreWords();

        $('.infinite-scroll-container').on('scroll', function () {
            if ($(this).scrollTop() + $(this).height() >= $(this)[0].scrollHeight - 100) {
                if (!isLoading && hasMoreData) {
                    loadMoreWords();
                }
            }
        });

        function loadMoreWords() {
            if (isLoading || !hasMoreData) return;

            isLoading = true;
            const startIndex = currentPage * itemsPerPage;
            const endIndex = Math.min(startIndex + itemsPerPage, wordData.length);

            if (startIndex >= wordData.length) {
                hasMoreData = false;
                isLoading = false;
                return;
            }

            for (let i = startIndex; i < endIndex; i++) {
                const wordEntry = wordData[i];
                if (i % 6 === 0) {
                    $('#wordTableBody').append('<tr></tr>');
                }
                const wordHtml = `
                <td data-word="${wordEntry[0]}" data-type="${wordEntry[1]}"
                    data-meaning="${wordEntry[2]}" data-example="${wordEntry[3]}">
                    ${wordEntry[0]}<br><small>${wordEntry[2]}</small>
                </td>`;
                $('#wordTableBody tr:last-child').append(wordHtml);
            }

            currentPage++;
            isLoading = false;
        }
    });

    $('.word-table').on('mousedown', 'td', function (event) {
        isMouseDown = true;
        const currentIndex = $('.word-table td').index(this);
        const word = $(this).data('word');
        const type = $(this).data('type');
        const meaning = $(this).data('meaning');
        const example = $(this).data('example');

        // Shift 키를 이용한 다중 선택
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
                        word: wordToAdd, type: $(this).data('type'), meaning: $(this).data('meaning'), example: $(this).data('example')
                    });
                });
            }
        } else {
            // 단일 선택/해제
            if ($(this).hasClass('highlight')) {
                $(this).removeClass('highlight');
                selectedWords.delete(word);
            } else {
                $(this).addClass('highlight');
                selectedWords.set(word, {word, meaning});
                console.log(selectedWords)
            }
        }

        lastClickedIndex = currentIndex; // 마지막 클릭 인덱스 업데이트
        updateSelectedWordsModal();


        timeoutId = setTimeout(() => {
            $('#modalWord').text("로딩중 입니다.");
            $('#modalType').text("로딩중 입니다.");
            $('#modalMeaning').text("로딩중 입니다.");
            $('#modalExample').text("로딩중 입니다.");
            $('#modalSpeech').text("로딩중 입니다.");
            $('#wordDetailModal').modal('show');

            $.ajax({
                url: 'http://localhost:5000/Python/word-mean', // Python API 엔드포인트
                type: 'POST', contentType: 'application/json', data: JSON.stringify({word: word}), success: function (response) {
                    console.log('Python API Response:', response);

                    // 모달창 데이터 업데이트
                    $('#modalWord').text(response.word || word);
                    $('#modalType').text(response.partofspeech || 'N/A');
                    $('#modalMeaning').text(response.mean || 'N/A');
                    $('#modalExample').text(response.example || 'N/A');
                    $('#modalSpeech').text(response.speech || 'N/A');

                    // 모달 표시
                    $('#wordDetailModal').modal('show');
                }, error: function (xhr, status, error) {
                    console.error('Failed to fetch data from Python API:', error);
                    alert('단어 정보를 불러오는 데 실패했습니다.');
                }
            });
        }, 1000);

    });

// 마우스 업/마우스 떠날 때 타임아웃 초기화
    $('.word-table').on('mouseup mouseleave', 'td', function () {
        clearTimeout(timeoutId);
        isMouseDown = false;
    });
    $('.word-table').on('mouseup mouseleave', 'td', function () {
        clearTimeout(timeoutId);
    });

    $('.word-table').on('mousemove', 'td', function () {
        if (isMouseDown) {
            $(this).addClass('highlight');
            const word = $(this).data('word');
            if (!selectedWords.has(word)) {
                selectedWords.set(word, {
                    word, type: $(this).data('type'), meaning: $(this).data('meaning'), example: $(this).data('example')
                });
            }
        }
        updateSelectedWordsModal();
    });

    $(document).on('mouseup', function () {
        isMouseDown = false;
    });

    $('#btnSelectAll').on('click', function () {
        $('.word-table td').each(function () {
            $(this).addClass('highlight');
            const word = $(this).data('word');
            selectedWords.set(word, {
                word, type: $(this).data('type'), meaning: $(this).data('meaning'), example: $(this).data('example')
            });
        });
        updateSelectedWordsModal();
    });

    $('#btnDeselectAll').on('click', function () {
        $('.word-table td').removeClass('highlight');
        selectedWords.clear();
        updateSelectedWordsModal();
    });

    function updateSelectedWordsModal() {
        const modalList = $('#selectedWordsList');
        modalList.empty();

        if (selectedWords.size === 0) {
            $('#selectedWordsModal').hide();
            $('#btnRestoreModal').hide();
        } else if (!isModalMinimized) {
            $('#selectedWordsModal').show();
            $('#btnRestoreModal').hide();
        } else {
            $('#btnRestoreModal').show();
        }

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
    }

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

// 최소화 버튼 동작
    $('#btnMinimizeModal').on('click', function () {
        $('#selectedWordsModal').hide();
        $('#btnRestoreModal').show();
        isModalMinimized = true;
    });

// 복구 버튼 동작
    $('#btnRestoreModal').on('click', function () {
        $('#selectedWordsModal').show();
        $('#btnRestoreModal').hide();
        isModalMinimized = false;
    });

// 초기 상태
    $('#selectedWordsModal').hide();
    $('#btnRestoreModal').hide();
    $('#selectedWordsList').on('click', '.word-item span', function () {
        const word = $(this).data('word');
        $('#modalWord').text("로딩중 입니다.");
        $('#modalType').text("로딩중 입니다.");
        $('#modalMeaning').text("로딩중 입니다.");
        $('#modalExample').text("로딩중 입니다.");
        $('#modalSpeech').text("로딩중 입니다.");
        $('#wordDetailModal').modal('show');
        $.ajax({
            url: 'http://localhost:5000/Python/word-mean', // Python API 엔드포인트
            type: 'POST', contentType: 'application/json', data: JSON.stringify({word: word}), success: function (response) {
                console.log('Python API Response:', response);

                // 모달창 데이터 업데이트
                $('#modalWord').text(response.word || word);
                $('#modalType').text(response.partofspeech || 'N/A');
                $('#modalMeaning').text(response.mean || 'N/A');
                $('#modalExample').text(response.example || 'N/A');
                $('#modalSpeech').text(response.speech || 'N/A');

                // 모달 표시
                $('#wordDetailModal').modal('show');
            }, error: function (xhr, status, error) {
                console.error('Failed to fetch data from Python API:', error);
                alert('단어 정보를 불러오는 데 실패했습니다.');
            }
        });
    });
    $('#searchInput').on('input', function () {
        const searchKeyword = $(this).val().toLowerCase();

        $('.word-table td').each(function () {
            const word = $(this).data('word').toLowerCase();
            const meaning = $(this).data('meaning').toLowerCase();

            if (word.includes(searchKeyword) || meaning.includes(searchKeyword)) {
                $(this).show(); // 검색 키워드와 일치하면 표시
            } else {
                $(this).hide(); // 일치하지 않으면 숨김
            }
        });
    });
    $(document).ready(function () {
        let currentModalPage = 0;
        const itemsPerModalPage = 20;
        let isModalLoading = false;
        let hasMoreModalData = true;

        // 모달 초기화
        $('#selectedWordsModal').hide();
        $('#btnRestoreModal').hide();

        function loadMoreModalWords() {
            if (isModalLoading || !hasMoreModalData) return;

            isModalLoading = true;

            // 무한 스크롤에서 로드할 항목 계산
            const startIndex = currentModalPage * itemsPerModalPage;
            const modalList = $('#selectedWordsList');

            selectedWords.forEach((data, word, index) => {
                if (index >= startIndex && index < startIndex + itemsPerModalPage) {
                    const listItem = $(`
                    <li class="list-group-item d-flex justify-content-between align-items-center word-item">
                        <span>${word}</span>
                        <button class="btn btn-sm btn-danger btn-remove-word" data-word="${word}">X</button>
                    </li>
                `);
                    modalList.append(listItem);
                }
            });

            currentModalPage++;
            isModalLoading = false;

            if (selectedWords.size <= startIndex + itemsPerModalPage) {
                hasMoreModalData = false;
            }
        }

        // 무한 스크롤 이벤트 추가
        $('#selectedWordsModal').on('scroll', function () {
            if ($(this).scrollTop() + $(this).height() >= $(this)[0].scrollHeight - 50) {
                loadMoreModalWords();
            }
        });

        // 모달 위치 업데이트 및 스크롤 적용
        function updateSelectedWordsModal() {
            const modalList = $('#selectedWordsList');
            modalList.empty();

            if (selectedWords.size === 0) {
                $('#selectedWordsModal').hide();
            } else {
                $('#selectedWordsModal').show();
                currentModalPage = 0;
                hasMoreModalData = true;
                loadMoreModalWords();
            }
        }

        $('#selectedWordsList').on('click', '.btn-remove-word', function () {
            const wordToRemove = $(this).data('word');
            selectedWords.delete(wordToRemove);
            updateSelectedWordsModal();
        });
    });
    // 컨텐츠 생성 버튼
    // 테스트 버튼
    $('#btnTest').on('click', function () {
        // selectedWords가 Map인지 확인하고 크기 확인
        const selectedCount = selectedWords instanceof Map ? selectedWords.size : 0;
        if (selectedCount < 20) {
            alert('20개 이상 선택해주세요.');
        } else {
            // Map의 키만 배열로 변환하여 리스트 생성
            const selectedWordList = Array.from(selectedWords.keys());
            console.log('선택된 단어 리스트:', selectedWordList); // 디버깅용 로그
            // AJAX로 서버에 데이터 전송
            $.ajax({
                url: '/test/example', // 서버 엔드포인트 URL
                method: 'POST',      // HTTP 메서드
                contentType: 'application/json', // JSON 형식으로 데이터 전송
                data: JSON.stringify({ words: selectedWordList }), // 데이터 전송
                success: function (response) {
                    // 성공 시 처리
                    alert('테스트를 시작합니다!');
                    console.log('서버 응답:', response);
                },
                error: function (xhr, status, error) {
                    // 에러 시 처리
                    alert('데이터 전송에 실패했습니다.');
                    console.error('AJAX 에러:', error);
                }
            });
        }
    });
    // 작문 버튼
    $('#btnWrite').on('click', function () {
        const selectedCount = selectedWords instanceof Map ? selectedWords.size : 0;
        if (selectedCount < 1) {
            alert('최소 한개 이상 선택 해주세요.');
        } else {
            // Map의 키만 배열로 변환하여 리스트 생성
            const selectedWordList = Array.from(selectedWords.keys());
            console.log('선택된 단어 리스트:', selectedWordList); // 디버깅용 로그
            // AJAX로 서버에 데이터 전송
            $.ajax({
                url: '/novel/example', // 서버 엔드포인트 URL
                method: 'POST',      // HTTP 메서드
                contentType: 'application/json', // JSON 형식으로 데이터 전송
                data: JSON.stringify({ words: selectedWordList }), // 데이터 전송
                success: function (response) {
                    // 성공 시 처리
                    alert('테스트를 시작합니다!');
                    console.log('서버 응답:', response);
                },
                error: function (xhr, status, error) {
                    // 에러 시 처리
                    alert('데이터 전송에 실패했습니다.');
                    console.error('AJAX 에러:', error);
                }
            });
        }
    });
    // 뉴스 버튼
    $('#btnNews').on('click', function () {
        const selectedCount = selectedWords instanceof Map ? selectedWords.size : 0;
        if (selectedCount < 1) {
            alert('최소 한개 이상 선택 해주세요.');
        } else {
            // Map의 키만 배열로 변환하여 리스트 생성
            const selectedWordList = Array.from(selectedWords.keys());
            console.log('선택된 단어 리스트:', selectedWordList); // 디버깅용 로그
            // AJAX로 서버에 데이터 전송
            $.ajax({
                url: '/novel/example', // 서버 엔드포인트 URL
                method: 'POST',      // HTTP 메서드
                contentType: 'application/json', // JSON 형식으로 데이터 전송
                data: JSON.stringify({ words: selectedWordList }), // 데이터 전송
                success: function (response) {
                    // 성공 시 처리
                    alert('테스트를 시작합니다!');
                    console.log('서버 응답:', response);
                },
                error: function (xhr, status, error) {
                    // 에러 시 처리
                    alert('데이터 전송에 실패했습니다.');
                    console.error('AJAX 에러:', error);
                }
            });
        }
    });
    $('#wordDeleteBtn').on('click', function(){
        const selectedWordList = Array.from(selectedWords.keys());
        console.log('선택된 단어 리스트:', selectedWordList); // 디버깅용 로그
        const selectedCount = selectedWords instanceof Map ? selectedWords.size : 0;
        if (selectedCount < 1) {
            alert('최소 한개 이상 선택 해주세요.');
        } else {
            $.ajax({
                url: '/WordBook/WordData/remove', // 서버 엔드포인트 URL
                method: 'POST',      // HTTP 메서드
                contentType: 'application/json', // JSON 형식으로 데이터 전송
                data: JSON.stringify({words: selectedWordList}), // 데이터 전송
                success: function (response) {
                    // 성공 시 처리
                    alert('단어가 삭제 되었습니다.');
                    console.log('서버 응답:', response);
                    window.location.reload(); // 페이지 새로고침
                },
                error: function (xhr, status, error) {
                    // 에러 시 처리
                    alert('데이터 전송에 실패했습니다.');
                    console.error('AJAX 에러:', error);
                }
            });
        }
    });
});
