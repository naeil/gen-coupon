package naeil.gen_coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.response.*;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.CouponIssueRepository;
import naeil.gen_coupon.repository.CustomerRepository;
import naeil.gen_coupon.repository.OrderHistoryRepository;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final ConfigService configService;
    private final CouponIssueRepository couponIssueRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final StampRepository stampRepository;

    public List<CustomerDTO> findAll() {
        List<CustomerDTO> customers = customerRepository.findAll().stream()
            .map(CustomerDTO::toDTO)
            .toList();
        return customers;
    }

    public CustomerDetailResponseDTO getCustomerDetail(Integer customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(404, "존재하지 않는 회원입니다."));

        String maxStampStr = configService.getValue("minimum_count");
        int maxStamp = (maxStampStr != null) ? Integer.parseInt(maxStampStr) : 10;

        List<CouponIssueDTO> coupons = couponIssueRepository.findAllByCustomerEntity_CustomerIdOrderByCreateDateDesc(customerId)
                .stream()
                .map(CouponIssueDTO::toDTO)
                .toList();

        List<StampEntity> stamps = stampRepository.findVerifiedStamps(customerId);

        List<OrderHistoryDTO> orders = stamps
                .stream()
                .map(StampEntity::getOrderHistoryEntity)
                .map(OrderHistoryDTO::toDTO)
                .toList();

        // 5. 스탬프 현황 계산
        int currentStamp = stamps.size();
        int remainStamp = maxStamp - currentStamp;

        // 6. DTO 조립 및 반환
        return CustomerDetailResponseDTO.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .htel(customer.getCustomerHtel())
                .currentStamp(currentStamp)
                .maxStamp(maxStamp)
                .remainStamp(remainStamp)
                .coupons(coupons)
                .orderHistories(orders)
                .build();
    }
}

