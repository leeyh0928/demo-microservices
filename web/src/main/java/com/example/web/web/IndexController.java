package com.example.web.web;

import com.example.web.config.dto.SessionUser;
import com.example.web.dto.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private static final String PRODUCT_URL = "http://gateway:8080/product-composite";
    private final HttpSession httpSession;
    private final WebClient webClient;

    @GetMapping("/")
    public String index(Model model) {
        var user = (SessionUser) httpSession.getAttribute("user");

        if(user != null){
            model.addAttribute("userName", user.getUserName());
        }

        return "index";
    }

    @GetMapping("/product/create")
    public String productCreate() {
        return "product-create";
    }

    @GetMapping("/product/update/{productId}")
    public String productUpdate(@PathVariable int productId, Model model) {
        Product product = webClient.get()
                .uri(PRODUCT_URL + "/" + productId)
                .retrieve()
                .bodyToMono(Product.class)
                .block();

        model.addAttribute("product", product);

        return "product-update";
    }
}
