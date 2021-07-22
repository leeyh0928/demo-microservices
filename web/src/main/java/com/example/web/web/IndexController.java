package com.example.web.web;

import com.example.web.config.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model) {
        var user = (SessionUser) httpSession.getAttribute("user");

        if(user != null){
            model.addAttribute("userName", user.getUserName());
        }

        return "index";
    }

    @GetMapping("/product/save")
    public String productSave() {
        return "product-save";
    }
}
