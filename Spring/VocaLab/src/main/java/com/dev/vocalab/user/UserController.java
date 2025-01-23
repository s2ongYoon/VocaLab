package com.dev.vocalab.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class UserController {

    public String loginPage() {

        return "login";
    }
}
