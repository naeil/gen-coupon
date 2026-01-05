package naeil.gen_coupon.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponViewController {
    
    @GetMapping
    public String coupons(Model model) {
        // model.addAttribute("users", orderService.findAll());
        model.addAttribute("coupons", new ArrayList<>());
        
        return "coupons";
    }
}
