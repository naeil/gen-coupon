package naeil.gen_coupon.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponDataDTO;
import naeil.gen_coupon.dto.querydsl.CouponSearchRequestDTO;
import naeil.gen_coupon.dto.response.CouponIssueDTO;
import naeil.gen_coupon.service.CouponService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {
    
    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<?> getCoupons(
            @RequestParam(required = false) Integer customerId,            
            @RequestParam(required = false) String customerName,            
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false, defaultValue="1") int pageNumber,
            @RequestParam(required = false, defaultValue="20") int pageSize
    ) {       
        List<CouponIssueDTO> coupons = couponService.searchCouponIssueList(
            CouponSearchRequestDTO.builder()
                .customerId(customerId)
                .customerName(customerName)
                .fromDate(fromDate)
                .toDate(toDate)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build()
        );
        
        return ResponseEntity.ok().body(coupons);
    }

    @GetMapping("/external")
    public ResponseEntity<?> getCouponsFromImWeb(@RequestParam(required = false, defaultValue="10") int limit,
                                                 @RequestParam(required = false, defaultValue="0") int offset) {
        ImWebCouponDataDTO coupons = couponService.fetchCouponsFromImWeb(limit, offset);
        
        return ResponseEntity.ok().body(coupons);
    }
}
