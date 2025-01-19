package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FilesRepository;
import com.dev.vocalab.files.FilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/WordBook")
@Controller
@RequiredArgsConstructor
public class WordBooksController {
    private final WordBooksService wordBooksService;
    private final FilesService filesService;

    @GetMapping("/List")
    public String wordbookList() {
        return "wordbooks/wordbooksList";
    }
    @GetMapping("/WordData")
    @ResponseBody
    public List<String[]> getWordData(@RequestParam("wordBookId") Integer wordBookId) {
        try {
            // Fetch the word list using the WordBooksService
            return wordBooksService.readWordBook(wordBookId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load word book data.", e);
        }
    }
    @PostMapping("/WordData/remove")
    @ResponseBody
    public void deleteWords(@RequestBody Map<String, List<String>> deleteWordList, @RequestParam("wordBookId") Integer wordBookId) {
        try {
            List<String> words = deleteWordList.get("words");
            System.out.println("Received words: " + words);
            wordBooksService.deleteWord(wordBookId,words);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load word book data.", e);
        }
    }

    @GetMapping("/Word")
    public String wordListPage(@RequestParam(value = "wordBookId", required = true) Integer wordBookId, Model model) {
        try {
            // wordBookId가 null인 경우를 대비한 체크
            if (wordBookId == null) {
                model.addAttribute("error", "WordBook ID is missing.");
                return "wordbooks/wordList";
            }

            // Fetch the word list using the WordBooksService
            List<String[]> wordList = wordBooksService.readWordBook(wordBookId);
            model.addAttribute("wordList", wordList);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load the word book: " + e.getMessage());
        }
        return "wordbooks/wordList";
    }

}
