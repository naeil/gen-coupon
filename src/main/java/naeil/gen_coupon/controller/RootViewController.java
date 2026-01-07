package naeil.gen_coupon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootViewController {
    
    @GetMapping("/")
    public String redirectToCustomers() {
        return "redirect:/customers";
    }
}
