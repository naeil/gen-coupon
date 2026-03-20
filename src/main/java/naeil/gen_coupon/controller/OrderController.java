package naeil.gen_coupon.controller;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.dto.querydsl.OrderSearchRequestDTO;
import naeil.gen_coupon.dto.request.OrderHistoryDTO;
import naeil.gen_coupon.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getOrderHistories(
            @RequestParam(required = false, name = "shopCode") String shopCode,
            @RequestParam(required = false, name = "customerId") Integer customerId,
            @RequestParam(required = false, name = "customerName") String customerName,
            @RequestParam(required = false, name = "issueId") Integer issueId,
            @RequestParam(required = false, name = "fromDate") LocalDate fromDate,
            @RequestParam(required = false, name = "toDate") LocalDate toDate,
            @RequestParam(required = false, defaultValue = "1", name = "pageNumber") int pageNumber,
            @RequestParam(required = false, defaultValue = "20", name = "pageSize") int pageSize) {
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
                        .build());

        return ResponseEntity.ok().body(orders);
    }
}
