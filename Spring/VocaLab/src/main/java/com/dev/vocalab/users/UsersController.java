package com.dev.vocalab.users;

import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOIDCUsers;
import com.dev.vocalab.users.details.CustomUsersDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // 로그인 기능 => 스프링 시큐리티에서 처리되므로 필요없음
//    @PostMapping("/login")
//    String loginID(@RequestParam String userId, @RequestParam String userPassword, Model model) {
//        usersService.loginUser(userId, userPassword);
//        return "users/login";
//    }

    // GET 방식으로 회원가입 페이지 접근
    @GetMapping("/register")
    public String registerPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
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
    public String mainPage(Model model) {
        UsersDTO usersDTO = usersService.getUserInfo();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (usersDTO != null) {
            model.addAttribute("loginType", usersDTO.getLoginType());
            model.addAttribute("userId", usersDTO.getUserId());
            model.addAttribute("userName", usersDTO.getUserName());
            model.addAttribute("userNickname", usersDTO.getUserNickname());
            model.addAttribute("users", usersRepository.findAll());

            model.addAttribute("principal", principal);

            return "board/csmain";
        }

        return "board/csmain";
    }
    

    // 로그아웃 테스트용
    @GetMapping("/test")
    public String testPage (Authentication authentication){
        return "users/test";
    }

}




/*
컨트롤러에서 비즈니스 로직 처리
    @GetMapping("/main")
    public String mainPage(Model model) {
//        // 로그인 상태 확인
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return "redirect:/login";
//        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUsersDetails) {
                CustomUsersDetails userDetails = (CustomUsersDetails) principal;
                model.addAttribute("loginType", "normal");
                model.addAttribute("userId", userDetails.getUserId());
                model.addAttribute("userName", userDetails.getUserName());
                model.addAttribute("nickname", userDetails.getUserNickname());
            } else if (principal instanceof CustomOAuth2Users) {
                CustomOAuth2Users oauth2User = (CustomOAuth2Users) principal;
                model.addAttribute("loginType", "oauth2");
                model.addAttribute("nickname", oauth2User.getNickname());
            } else if (principal instanceof CustomOIDCUsers) {
                CustomOIDCUsers oidcUser = (CustomOIDCUsers) principal;
                model.addAttribute("loginType", "oidc");
                model.addAttribute("nickname", oidcUser.getNickname());
            }

            model.addAttribute("principal", principal);
            model.addAttribute("users", usersRepository.findAll());
            return "board/csmain";
        }
        return "board/csmain";
    }
*/

