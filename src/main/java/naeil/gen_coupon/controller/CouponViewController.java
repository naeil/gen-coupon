package naeil.gen_coupon.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.dto.querydsl.CouponSearchRequestDTO;
import naeil.gen_coupon.dto.response.CouponIssueDTO;
import naeil.gen_coupon.service.CouponService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponViewController {

    private final CouponService couponService;
    
    @GetMapping
    public String coupons(
            @RequestParam(required = false) Integer customerId,            
            @RequestParam(required = false) String customerName,            
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false, defaultValue="1") int pageNumber,
            @RequestParam(required = false, defaultValue="20") int pageSize,
            Model model
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
        model.addAttribute("coupons", coupons);
        
        return "coupons";
    }
}
