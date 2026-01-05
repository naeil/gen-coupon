package naeil.gen_coupon.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.dto.querydsl.OrderSearchRequestDTO;
import naeil.gen_coupon.dto.response.OrderHistoryDTO;
import naeil.gen_coupon.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
   
    @GetMapping
    public ResponseEntity<?> getOrderHistories(
            @RequestParam(required = false) Integer customerId,            
            @RequestParam(required = false) String customerName,            
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false, defaultValue="1") int pageNumber,
            @RequestParam(required = false, defaultValue="20") int pageSize,
            Model model
    ) {
        List<OrderHistoryDTO> orders = orderService.searchOrderHistoryList(
            OrderSearchRequestDTO.builder()
                .customerId(customerId)
                .customerName(customerName)
                .fromDate(fromDate)
                .toDate(toDate)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build()
        );
        
        return ResponseEntity.ok().body(orders);
    }    
}
