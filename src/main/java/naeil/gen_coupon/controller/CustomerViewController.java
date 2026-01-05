package naeil.gen_coupon.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.service.CustomerService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerViewController {
    
    private final CustomerService customerService;

    @GetMapping
    public String users(Model model) {
        // model.addAttribute("customers", orderService.findAll());

        model.addAttribute("customers", new ArrayList<>());
        return "customers";
    }
}
