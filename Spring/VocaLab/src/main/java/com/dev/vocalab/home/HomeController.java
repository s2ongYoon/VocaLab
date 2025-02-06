package com.dev.vocalab.home;

import com.dev.vocalab.users.details.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String home(Model model) {

        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }
        AuthenticationUtil.addUserSessionToModel(model);

        return "home";
    }

}
