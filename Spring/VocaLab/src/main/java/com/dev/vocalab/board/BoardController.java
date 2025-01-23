package com.dev.vocalab.board;

import com.dev.vocalab.files.FilesRepository;
import com.dev.vocalab.files.FilesService;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.CustomUsersDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/CS")
@RequiredArgsConstructor
@Slf4j
public class BoardController {
    private final BoardService boardService;
    private final FilesService filesService;
    private final UsersRepository usersRepository;
    private final String realPath = "C:/Dev/Data/VocaLab/Spring/VocaLab/src/main/resources/static/images/upload";
    private final BoardRepository boardRepository;
    private final FilesRepository filesRepository;

    /*
    페이지로 이동되는 메서드들
    */
//    CS/Main, /CS , /CS/ 를 공지사항으로 이동시킵니다.
    @GetMapping({"/Main", "","/"})
    public String boardHome() {
        return "redirect:/CS/Notice";
    }

//    공지사항 탭 입니다.
    @GetMapping("/Notice")
    public String boardNotice(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(required = false) String searchWord,
                              Model model,
                              HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.NOTICE, "Notice", "공지사항",
                pageNum, searchWord, model, request);
    }

//    FAQ 탭 입니다.
    @GetMapping("/FAQ")
    public String boardFaq(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(required = false) String searchWord,
                           Model model,
                           HttpServletRequest request) {
        return handleBoardList(BoardEntity.Category.FAQ, "FAQ", "자주 묻는 질문",
                pageNum, searchWord, model, request);
    }

//    1:1 상담 탭 입니다.
    @GetMapping("/Inquiry")
    public String boardInquiry(@RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(required = false) String searchWord,
                               Model model,
                               HttpServletRequest request) {
        // SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                model.addAttribute("userSession", userDetails); // 사용자 정보를 Model에 추가
            }
        }
        return handleBoardList(BoardEntity.Category.INQUIRY, "Inquiry", "1:1 문의",
                pageNum, searchWord, model, request);
    }
//    게시글 작성 뷰 페이지 이동입니다.
    @GetMapping("/Write")
    public String write(Model model) {
        // SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                model.addAttribute("userSession", userDetails); // 사용자 정보를 Model에 추가
            }
        }
        filesService.deleteFolder(realPath + "/board/temp/");
        return "board/boardWrite";
    }
@GetMapping("/View")
public String view(Model model, @RequestParam Integer boardId) {
    // SecurityContext에서 인증 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUsersDetails userDetails = null;
    if (authentication != null && authentication.isAuthenticated()) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUsersDetails) {
            userDetails = (CustomUsersDetails) principal;
            model.addAttribute("userSession", userDetails); // 사용자 정보를 Model에 추가
        }
    }

    final CustomUsersDetails currentUser = userDetails; // userDetails를 final 변수로 선언

    return boardService.findBoardWithUserNickname(boardId).map(dto -> {
        // 게시글의 카테고리 확인
        if (dto.getCategory() == BoardEntity.Category.INQUIRY) {
            // Inquiry 게시판 접근 제한
            if (currentUser == null ||
                    !(currentUser.getUserRole() == CustomUsersDetails.UserRole.ADMIN ||
                            currentUser.getUserId().equals(dto.getUserId()))) {
                System.out.println("권한 없음: Inquiry 게시판에 접근하려 했습니다.");
                return "redirect:/CS/Main"; // 권한 없음 처리
            }
        } else if (dto.getCategory() == BoardEntity.Category.REPLY) {
            // Reply 카테고리는 항상 접근 금지
            System.out.println("Reply 게시판은 접근이 금지되어 있습니다.");
            return "redirect:/CS/Main";
        }

        // Notice나 FAQ는 로그인 여부와 상관없이 접근 가능
        // 게시글 내용 처리 및 View 반환
        dto.setContent(dto.getContent().replaceAll("\r\n", "<br>"));
        model.addAttribute("row", dto);
        return "board/boardView";
    }).orElseGet(() -> {
        // 게시글이 존재하지 않는 경우 처리
        model.addAttribute("row", null);
        model.addAttribute("userNickname", "알 수 없음");
        return "redirect:/CS/Main";
    });
}




    //    게시글 수정 페이지 이동입니다.
    @GetMapping("/Edit")
    public String boardEdit(Model model, @RequestParam Integer boardId) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        String tempPath = realPath + "/board/temp/";
        filesService.deleteFolder(tempPath);
        String originPath = realPath + "/board/" + board.getBoardId() + "/";
        filesService.copyFiles(originPath, tempPath);
        board.setContent(board.getContent().replaceAll("/" + board.getBoardId() + "/", "/temp/"));
        model.addAttribute("row", board);
        return "board/boardEdit";
    }
//    1:1 문의 답변 입니다.
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomUsersDetails userDetails = (CustomUsersDetails) principal;
        String userId = ((CustomUsersDetails) principal).getUserId();

//        String testUserId = "testAdmin";
        UsersEntity user = usersRepository.findByUserId(userId)
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
        filesService.handleBoardFileUpload(savedBoard.getBoardId(), thumbnail, userId);
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
        filesService.handleBoardFileUpdate(boardId, content, thumbnail, "testAdmin");
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
            filesService.deleteAllBoardFiles(replyBoardId);
            filesService.deleteFolder(realPath + "/board/" + replyBoardId);
            boardRepository.delete(replyBoard.get());
        }

        // 원본 게시글 삭제
        boardService.deleteBoard(boardId);
        filesService.deleteFolder(realPath + "/board/" + boardId);

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                model.addAttribute("userSession", userDetails); // 사용자 세션 정보를 모델에 추가
            }
        }

        Page<BoardDTO> boardList;
        Pageable pageable = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Order.desc("boardId")));

        if (category == BoardEntity.Category.INQUIRY) {
            // INQUIRY 카테고리 처리 로직 분리
            boardList = handleInquiryCategory(searchWord, pageable);
        } else {
            // INQUIRY가 아닌 경우 기존 로직 유지
            if (searchWord == null || searchWord.isEmpty()) {
                boardList = boardService.selectListPage(pageable, category);
            } else {
                searchWord = searchWord.trim();
                boardList = boardService.selectListPageSearch("%" + searchWord + "%", pageable, category);
            }
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
//    1:1문의 탭 조건에 따라 분류 나눔
    private Page<BoardDTO> handleInquiryCategory(String searchWord, Pageable pageable) {
        Page<BoardDTO> boardList;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                if (userDetails.getUserRole() == CustomUsersDetails.UserRole.ADMIN) {
                    // ADMIN 역할: 검색어에 따른 전체 목록 조회
                    if (searchWord == null || searchWord.isEmpty()) {
                        boardList = boardService.selectListPage(pageable, BoardEntity.Category.INQUIRY);
                    } else {
                        searchWord = searchWord.trim();
                        boardList = boardService.selectListPageSearch("%" + searchWord + "%", pageable, BoardEntity.Category.INQUIRY);
                    }
                } else if (userDetails.getUserRole() == CustomUsersDetails.UserRole.USER) {
                    // USER 역할: 본인의 게시글만 검색어에 따라 조회
                    if (searchWord == null || searchWord.isEmpty()) {
                        boardList = boardService.selectListPageByUserId(pageable, BoardEntity.Category.INQUIRY, userDetails.getUserId());
                    } else {
                        searchWord = searchWord.trim();
                        boardList = boardService.selectListPageByUserIdAndSearch("%" + searchWord + "%", pageable, BoardEntity.Category.INQUIRY, userDetails.getUserId());
                    }
                } else {
                    // 기타 역할: 빈 목록 반환
                    boardList = Page.empty(pageable);
                }
            } else {
                // 인증 정보가 없으면 빈 목록 반환
                boardList = Page.empty(pageable);
            }
        } else {
            // 인증되지 않은 사용자 -> 빈 목록 반환
            boardList = Page.empty(pageable);
        }
        return boardList;
    }
    @PostMapping(value = "/uploadSummernoteImageFile", produces = "application/json; charset=utf8")
    @ResponseBody
    public String uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
        log.info("이미지 업로드 요청: {}", multipartFile.getOriginalFilename());
        String tempPath = realPath + "/board/temp/";
        return filesService.uploadFile(multipartFile, tempPath).toString();
    }
}
