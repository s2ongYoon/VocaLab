package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FilesService;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.*;

@RequestMapping("/WordBook")
@Controller
@RequiredArgsConstructor
public class WordBooksController {
    private final WordBooksService wordBooksService;
    private final FilesService filesService;
    private final UsersRepository usersRepository;
    private final WordBooksRepository wordBooksRepository;

    // 단어장 목록 조회 - JSP에서 직접 렌더링하도록 수정
    @GetMapping("/List")
    public String wordbookList(
            @RequestParam(value = "userId", required = false) String userId,
            Model model) {
        String testUserId = (userId != null && !userId.isEmpty()) ? userId : "testAdmin";

        List<WordBooksEntity> wordbooks = wordBooksRepository.findByUserId(testUserId);
        model.addAttribute("wordbooks", wordbooks);

        return "wordbooks/wordbooksList";
    }

    // 단어장 내부에서 데이터 불러오기 API
    @GetMapping("/WordData")
    @ResponseBody
    public List<String[]> getWordData(@RequestParam("wordBookId") Integer wordBookId) {
        try {
            return wordBooksService.readWordBook(wordBookId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load word book data.", e);
        }
    }

    // 단어장 내부 단어 삭제 API
    @PostMapping("/WordData/remove")
    @ResponseBody
    public void deleteWords( @RequestBody DeleteWordsRequestDTO request) {
        try {
            System.out.println("Received words: " + request.getWords());
            System.out.println("wordBookId: " + request.getWordBookId());
            wordBooksService.deleteWord(request.getWordBookId(), request.getWords());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete words from word book.", e);
        }
    }

    // 단어장 내부 진입
    @GetMapping("/Word")
    public String wordListPage(
            @RequestParam(value = "wordBookId") Integer wordBookId,
            Model model) {
        try {
            // 단어 목록 조회
            List<String[]> wordList = wordBooksService.readWordBook(wordBookId);
            model.addAttribute("wordList", wordList);

            // 단어장 정보도 함께 전달
            Optional<WordBooksEntity> wordbook = wordBooksRepository.findById(wordBookId);
            wordbook.ifPresent(wb -> model.addAttribute("wordbook", wb));

        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid WordBook ID: " + wordBookId);
            return "wordbooks/wordBooksList";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load the word book: " + e.getMessage());
        }
        return "wordbooks/wordList";
    }
//    북마크 상태 변경 1→0 0→1
    @PostMapping("/bookmark")
    @ResponseBody  // AJAX 응답을 위해 추가
    public Map<String, Object> changeBookmark(@RequestParam("wordBookId") Integer wordBookId) {
        System.out.println("Received wordBookId: " + wordBookId);
        wordBooksService.changeStatusOfBookmark(wordBookId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "즐겨찾기 상태가 변경되었습니다.");
        return response;
    }
}
