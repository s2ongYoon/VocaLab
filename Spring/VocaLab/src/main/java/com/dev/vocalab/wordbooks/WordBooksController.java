package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FilesService;
import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.users.details.CustomUsersDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // 단어장 목록 조회 - JSP에서 직접 렌더링
    @GetMapping("/List")
    public String wordbookList(Model model) {
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = AuthenticationUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        // 사용자 정보와 단어장 목록 조회
        UsersEntity user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        List<WordBooksEntity> wordbooks = wordBooksRepository.findByUserId(userId);
        model.addAttribute("wordbooks", wordbooks);
        model.addAttribute("userNickname", user.getUserNickname());

        return "wordbooks/wordbooksList";
    }
    // 단어장 내부 진입
    @GetMapping("/Word")
    public String wordListPage(
            @RequestParam(value = "wordBookId") Integer wordBookId,
            Model model) {
        try {
            // 인증 확인
            if (!AuthenticationUtil.isAuthenticated()) {
                return "redirect:/login";
            }
            String currentUserId = AuthenticationUtil.getCurrentUserId();
            if (currentUserId == null) {
                return "redirect:/login";
            }

            // 단어장 정보 조회 및 소유자 확인
            Optional<WordBooksEntity> wordbook = wordBooksRepository.findById(wordBookId);
            if (wordbook.isEmpty() || !wordbook.get().getUserId().equals(currentUserId)) {
                return "redirect:/WordBook/List";
            }

            // 단어 목록 조회
            List<String[]> wordList = wordBooksService.readWordBook(wordBookId);
            model.addAttribute("wordList", wordList);
            wordbook.ifPresent(wb -> model.addAttribute("wordbook", wb));

        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid WordBook ID: " + wordBookId);
            return "wordbooks/wordBooksList";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load the word book: " + e.getMessage());
        }
        return "wordbooks/wordList";
    }
    // 단어장 내부에서 데이터 불러오기 API
    @GetMapping("/WordData")
    @ResponseBody
    public List<String[]> getWordData(@RequestParam("wordBookId") Integer wordBookId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
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
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        try {
            wordBooksService.deleteWord(request.getWordBookId(), request.getWords());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete words from word book.", e);
        }
    }
//    북마크 상태 변경 1→0 0→1
    @PostMapping("/bookmark")
    @ResponseBody  // AJAX 응답을 위해 추가
    public Map<String, Object> changeBookmark(@RequestParam("wordBookId") Integer wordBookId) {
        if (!AuthenticationUtil.isAuthenticated()) {
            throw new AuthenticationException("인증이 필요합니다.") {};
        }
        wordBooksService.changeStatusOfBookmark(wordBookId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "즐겨찾기 상태가 변경되었습니다.");
        return response;
    }
}
