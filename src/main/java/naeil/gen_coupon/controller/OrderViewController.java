package naeil.gen_coupon.controller;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.dto.querydsl.OrderSearchRequestDTO;
import naeil.gen_coupon.dto.response.OrderHistoryDTO;
import naeil.gen_coupon.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    
    private final OrderService orderService;
   
    @GetMapping
    public String orders(
            @RequestParam(required = false, name = "shopCode") String shopCode,
            @RequestParam(required = false, name = "customerId") Integer customerId,            
            @RequestParam(required = false, name = "customerName") String customerName,
            @RequestParam(required = false, name = "issudId") Integer issueId,            
            @RequestParam(required = false, name = "fromDate") LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") LocalDate toDate,
            @RequestParam(required = false, defaultValue="1", name = "pageNumber") int pageNumber,
            @RequestParam(required = false, defaultValue="20", name = "pageSize") int pageSize,
            Model model
    ) {
        List<OrderHistoryDTO> orders = orderService.searchOrderHistoryList(
            OrderSearchRequestDTO.builder()
                .shopCode(shopCode)
                .customerId(customerId)
                .customerName(customerName)
                .issueId(issueId)
                .fromDate(fromDate)
                .toDate(toDate)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build()
        );
        model.addAttribute("orders", orders);

        return "orders";
    }
}
