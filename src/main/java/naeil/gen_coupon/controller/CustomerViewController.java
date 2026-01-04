package naeil.gen_coupon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerViewController {
    
    @GetMapping("/")
    public String users(Model model) {
        // model.addAttribute("users", orderService.findAll());
        return "users";
    }
}
