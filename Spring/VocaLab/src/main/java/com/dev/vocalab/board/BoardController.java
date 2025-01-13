package com.dev.vocalab.board;

import com.dev.vocalab.files.FileEntity;
import com.dev.vocalab.files.FileRepository;
import com.dev.vocalab.files.FileService;
import com.dev.vocalab.user.UserEntity;
import com.dev.vocalab.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.json.simple.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/CS")
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final FileService fileService;
    private final UserRepository userRepository;
    private final String realPath = "C:/Dev/Data/VocaLab/Spring/VocaLab/src/main/resources/static/images/upload";
    private final BoardRepository boardRepository;
    private final FileRepository fileRepository;

    /*
    페이지로 이동되는 메서드들
    */
    @GetMapping({"/Main", "","/"})
    public String boardHome() {
        return "redirect:/CS/Notice";
    }

    @GetMapping("/Notice")
    public String boardNotice(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(required = false) String searchWord,
                              Model model,
                              HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.NOTICE, "Notice", "공지사항",
                pageNum, searchWord, model, request);
    }

    @GetMapping("/FAQ")
    public String boardFaq(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(required = false) String searchWord,
                           Model model,
                           HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.FAQ, "FAQ", "자주 묻는 질문",
                pageNum, searchWord, model, request);
    }

    @GetMapping("/Inquiry")
    public String boardInquiry(@RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(required = false) String searchWord,
                               Model model,
                               HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.INQUIRY, "Inquiry", "1:1 문의",
                pageNum, searchWord, model, request);
    }

    @GetMapping("/Write")
    public String write() {
        fileService.deleteFolder(realPath + "/board/temp/");
        return "board/boardWrite";
    }

    @GetMapping("/View")
    public String view(Model model, @RequestParam Integer boardId) {
        boardService.findBoardWithUserNickname(boardId).ifPresentOrElse(
                dto -> {
                    dto.setContent(dto.getContent().replaceAll("\r\n", "<br>"));
                    model.addAttribute("row", dto);
                },
                () -> {
                    model.addAttribute("row", null);
                    model.addAttribute("userNickname", "알 수 없음");
                }
        );
        return "board/boardView";
    }

    @GetMapping("/Edit")
    public String boardEdit(Model model, @RequestParam Integer boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        String tempPath = realPath + "/board/temp/";
        fileService.deleteFolder(tempPath);
        String originPath = realPath + "/board/" + board.getBoardId() + "/";
        fileService.copyFiles(originPath, tempPath);
        board.setContent(board.getContent().replaceAll("/" + board.getBoardId() + "/", "/temp/"));
        model.addAttribute("row", board);
        return "board/boardEdit";
    }
    @GetMapping("/Reply")
    @ResponseBody
    public ResponseEntity<BoardDTO> getReplyBoard(@RequestParam Integer parentId) {
        try {
            BoardDTO replyBoard = boardService.findReplyByParentId(parentId);
            if (replyBoard != null) {
                // 줄바꿈 처리
                replyBoard.setContent(replyBoard.getContent().replaceAll("\r\n", "<br>"));
                return ResponseEntity.ok(replyBoard);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("답변 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
    CRUD 메서드들
    */
    @PostMapping("/Post")
    public String boardPosting(@RequestParam String title,
                               @RequestParam String content,
                               @RequestParam BoardEntity.Category category,
                               @RequestParam String replyStatus,
                               @RequestParam(required = false) MultipartFile thumbnail,
                               Integer parentId) {
        String testUserId = "testAdmin";
        UserEntity user = userRepository.findById(testUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (parentId != null) {
            BoardEntity parentBoard = boardRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 게시글을 찾을 수 없습니다."));
            parentBoard.setReplyStatus(BoardEntity.ReplyStatus.DONE);
            boardRepository.save(parentBoard);
        }

        BoardEntity board = BoardEntity.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .replyStatus(BoardEntity.ReplyStatus.valueOf(replyStatus))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .parentId(parentId)
                .build();

        BoardEntity savedBoard = boardService.savePost(board, null, null);
        fileService.handleBoardFileUpload(savedBoard.getBoardId(), thumbnail, testUserId);
        return "redirect:/CS/Notice";
    }

    @PostMapping("/Update")
    @Transactional
    public String boardUpdate(@RequestParam Integer boardId,
                              @RequestParam String title,
                              @RequestParam String content,
                              @RequestParam String category,
                              @RequestParam String replyStatus,
                              @RequestParam(required = false) MultipartFile thumbnail) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        fileService.handleBoardFileUpdate(boardId, content, thumbnail, "testAdmin");
        board.setTitle(title);
        board.setContent(content.replaceAll("/temp/", "/" + boardId + "/"));
        board.setCategory(BoardEntity.Category.valueOf(category));
        board.setReplyStatus(BoardEntity.ReplyStatus.valueOf(replyStatus));
        board.setUpdatedAt(LocalDateTime.now());
        boardService.savePost(board, null, null);
        return "redirect:/CS/View?boardId=" + boardId;
    }

    @PostMapping("/Delete")
    public String boardDelete(@RequestParam Integer boardId) {
        // 게시글의 카테고리 조회
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        String category = board.getCategory().toString();

        // 답변글이 있는지 확인
        Optional<BoardEntity> replyBoard = boardRepository.findByParentId(boardId);

        // 답변글이 있다면 먼저 삭제
        if (replyBoard.isPresent()) {
            Integer replyBoardId = replyBoard.get().getBoardId();
            fileService.deleteAllBoardFiles(replyBoardId);
            fileService.deleteFolder(realPath + "/board/" + replyBoardId);
            boardRepository.delete(replyBoard.get());
        }

        // 원본 게시글 삭제
        boardService.deleteBoard(boardId);
        fileService.deleteFolder(realPath + "/board/" + boardId);

        // 카테고리 값 변환
        String redirectCategory = category.equals("FAQ") ?
                category :
                String.valueOf(category.charAt(0)) + category.substring(1).toLowerCase();

        return "redirect:/CS/" + redirectCategory;
    }

    /*
    내부 Helper 메서드들
    */
    private String handleBoardList(BoardEntity.Category category, String boardCat,
                                   String pageTitle, int pageNum, String searchWord,
                                   Model model, HttpServletRequest request) {
        request.setAttribute("pageNum", pageNum);

        Page<BoardDTO> boardList;
        Pageable pageable = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Order.desc("boardId")));

        if (searchWord == null || searchWord.isEmpty()) {
            boardList = boardService.selectListPage(pageable, category);
        } else {
            searchWord = searchWord.trim();
            boardList = boardService.selectListPageSearch("%" + searchWord + "%", pageable);
        }

        model.addAttribute("boardList", boardList);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("boardCat", boardCat);
        model.addAttribute("searchWord", searchWord);

        model.addAttribute("pagingImg",
                PagingUtil.pagingImg(
                        (int) boardList.getTotalElements(),
                        boardList.getSize(),
                        5,
                        pageNum,
                        request.getContextPath() + "/CS/" + boardCat + "?",
                        searchWord
                )
        );

        return "board/boardList";
    }
    @PostMapping(value = "/uploadSummernoteImageFile", produces = "application/json; charset=utf8")
    @ResponseBody
    public String uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
        log.info("이미지 업로드 요청: {}", multipartFile.getOriginalFilename());
        String tempPath = realPath + "/board/temp/";
        return fileService.uploadFile(multipartFile, tempPath).toString();
    }

}
