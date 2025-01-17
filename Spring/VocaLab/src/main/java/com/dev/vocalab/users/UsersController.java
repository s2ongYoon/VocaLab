package com.dev.vocalab.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class UsersController {

    private final UsersService usersService;
    private final UsersRepository usersRepository;

    // GET 방식으로 로그인 페이지 접근
    @GetMapping("/login")
    public String loginPage() {
        List<String> result = usersRepository.findAllUserId();
        return "users/login";
    }

    // 로그인 기능
    @PostMapping("/login")
    String loginID(@RequestParam String userId, @RequestParam String userPassword, Model model) {
        usersService.loginUser(userId, userPassword);
        return "users/login";
    }

    // GET 방식으로 회원가입 페이지 접근
    @GetMapping("/register")
    public String registerPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()){
            return "redirect:board/csmain";
        }
        return "users/registration";
    }

    // 회원가입 기능
    @PostMapping("/register")
    String registerID(String userId
            , String userName, String userPassword, String userNickname,
                    String userEmail, String year, String month, String day, Integer gender,
                    Model model) {

        usersService.registerUser(userId, userName, userPassword, userNickname, userEmail, year, month, day, gender);

        return "redirect:/login";
    }

    @GetMapping("/main")
    public String mainPage() {
        return "board/csmain";
    }
}
