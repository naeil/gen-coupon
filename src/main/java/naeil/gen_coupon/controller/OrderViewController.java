package naeil.gen_coupon.controller;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.service.OrderService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    
    private final OrderService orderService;
   
    @GetMapping
    public String orders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Model model
    ) {
        // model.addAttribute("orders", orderService.search(userId, fromDate, toDate));
        model.addAttribute("orders", new ArrayList<>());
        return "orders";
    }
}
