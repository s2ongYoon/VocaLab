$(document).ready(function() {

    // history.jsp
    // 생성기록 선택
    // 전체 선택 클릭 이벤트
    let isAllChecked = false; // 전체 선택 상태를 추적

    // 전체 선택 버튼 클릭
    $("#select-all-a").on("click", function () {
        isAllChecked = true; // 전체 선택 상태 활성화
        $(".compile-record").prop("checked", true); // 모든 체크박스 체크
        $("#record-list li").css("background-color", "#919090"); // 모든 li 배경색 변경
    });

    // 선택 해제 버튼 클릭
    $("#deselect-a").on("click", function () {
        isAllChecked = false; // 전체 선택 상태 해제
        $(".compile-record").prop("checked", false); // 모든 체크박스 체크 해제
        $("#record-list li").css("background-color", "#cccccc"); // 모든 li 배경색 원래대로
    });

    // li 클릭 이벤트 (체크박스와 연동)
    $("#record-list").on("click", "li", function (e) {
        // // 만약 클릭한 요소가 form 내부의 버튼이면 li의 submit 동작을 막음
        // if ($(e.target).is("input, button")) {
        //     return;
        // }
        // li 내부의 form을 찾아서 submit
        $(this).find("form").submit();
    });

    // 체크박스 직접 클릭 시 이벤트
    $("#record-list").on("change", ".compile-record", function () {
        updateListStyle(); // 전체 상태 업데이트
    });

    // 리스트 스타일 업데이트 함수
    function updateListStyle() {
        let allChecked = true; // 전체 선택 상태 확인용

        $("#record-list li").each(function () {
            const checkbox = $(this).find("input[name='compileIdCheck']");
            if (checkbox.prop("checked")) {
                $(this).css("background-color", "#919090");
            } else {
                $(this).css("background-color", "#cccccc");
                allChecked = false; // 하나라도 체크 안 되어 있으면 전체 선택 아님
            }
        });

        isAllChecked = allChecked; // 전체 선택 상태 업데이트
    }

    // 무한 스크롤 구현 (단순 예시)
    let pageNum = 1;
    const $recordList = $("#record-list");

    $(window).on("scroll", function() {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 10) {
            loadMoreRecords();
        }
    });

    function loadMoreRecords() {
        const newRecords = $("<div>").html(`<p>기록 ${pageNum++} (몇일 전)</p>`);
        $recordList.append(newRecords);
    }


    //
});
