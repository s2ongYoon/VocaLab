package com.dev.vocalab.myPage;

import com.dev.vocalab.compileRecord.CompileDTO;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.AuthenticationUtil;
import com.dev.vocalab.wordbooks.WordBooksEntity;
import com.dev.vocalab.wordbooks.WordBooksService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/myPage")
@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final UsersRepository usersRepository;
    private final WordBooksService wordBookService;
    private final MyPageService myPageService;

    // [ 단어기록 ]
    @RequestMapping("/compileHistory")
    public String myPageRecord(Model model) {

        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = AuthenticationUtil.getCurrentUserId();
        List<CompileDTO> recordList = myPageService.getCompileRecordList(userId);
        System.out.println("recordList: " + recordList);

        model.addAttribute("recordList", recordList);

        return "mypage/myPageHistory";
    }

    // [ 기록중 다시 단어추가 기능 ]
    @RequestMapping("/recordAddWords")
    public String recordAddWords(Map<String,Object> map, @RequestParam("compileId")int compileId) {
        System.out.println("recordAddWords");
        System.out.println("compileId: " + compileId);
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = AuthenticationUtil.getCurrentUserId();
        // < 단어장 목록 불러오기 >
        List<WordBooksEntity> wordBookList = wordBookService.getWordBooks(userId);
        System.out.println("wordBookList" + wordBookList.toString());
        map.put("books", wordBookList);

        return "compile/compile_result";
    }

    // [ 회원정보 ]
    @GetMapping("/userInformation")
    public String myPageUserInfo(Model model) {
        System.out.println("myPageUserInfo");
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = AuthenticationUtil.getCurrentUserId();
        System.out.println("userId: " + userId);
        Optional<UsersEntity> users = usersRepository.findByUserId(userId);

        model.addAttribute("user", users.get());

        return "mypage/myPageUserInfo";
    }

    // [ 회원정보수정 뷰 ]
    @PostMapping("/userModify")
    public String myPageUserModify(Model model, @RequestParam("password") String password) {
        System.out.println("myPageUserModify");
        System.out.println("password: " + password);
        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }

        String userId = AuthenticationUtil.getCurrentUserId();
        System.out.println("userId: " + userId);
        Optional<UsersEntity> users = usersRepository.findByUserId(userId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String birth = sdf.format(users.get().getCreatedAt());

        model.addAttribute("user", users.get());
        model.addAttribute("birth", birth);

        return "mypage/myPageUserModify";
    }

    // [ 회원정보 수정 버튼 ]
    @PostMapping("/userModifyPro")
    public String myPageUserModifyPro(Model model,
                                      @RequestParam("password") String password) {

        return "";
    }

    // [ 비밀번호 조건 확인 - 대소문자 영문 특수문자]
    @PostMapping("/checkPassword")
    @ResponseBody
    public String checkPassword(@RequestParam("password") String password) {
        return "";
    }

} // class
