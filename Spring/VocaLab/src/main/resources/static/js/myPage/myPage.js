$(document).ready(function() {
    const $memberInfoMenu = $("#member-info");
    const $wordHistoryMenu = $("#word-history");
    const $pageContent = $("#page-content");

    // 메뉴 클릭 시 페이지 변경
    $memberInfoMenu.on("click", function() {
        loadPage("member-info");
        setActiveMenu($memberInfoMenu);
    });

    $wordHistoryMenu.on("click", function() {
        loadPage("word-history");
        setActiveMenu($wordHistoryMenu);
    });

    // 기본 페이지는 단어 생성 기록 페이지
    loadPage("word-history");
    setActiveMenu($wordHistoryMenu);

    function setActiveMenu($activeMenu) {
        $(".menu-item").removeClass("active");
        $activeMenu.addClass("active");
    }

    function loadPage(page) {
        if (page === "word-history") {
            $pageContent.html($("#word-history").html());
        } else if (page === "member-info") {
            $pageContent.html($("#member-info").html());
        }
    }

    // 회원정보 수정 페이지로 이동
    window.goToEditPage = function() {
        window.location.href = "/member-edit";
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
});
