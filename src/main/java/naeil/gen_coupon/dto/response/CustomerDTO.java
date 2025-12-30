package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.CustomerEntity;

import java.util.List;

@Data
@Builder
public class CustomerDTO {

    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private String customerHtel;
    private List<OrderHistoryDTO> orderHistoryDTOList;
    private List<CouponIssueDTO> couponIssueDTOList;
    private List<StampDTO> stampDTOList;

    public static CustomerDTO toDTO(CustomerEntity customerEntity) {
        return CustomerDTO.builder()
                .customerId(customerEntity.getCustomerId())
                .customerName(customerEntity.getCustomerName())
                .customerEmail(customerEntity.getCustomerEmail())
                .customerHtel(customerEntity.getCustomerHtel())
                .orderHistoryDTOList(customerEntity.getOrderHistoryEntities()
                        .stream().map(OrderHistoryDTO::toDTO).toList())
                .couponIssueDTOList(customerEntity.getCouponIssueEntities()
                        .stream().map(CouponIssueDTO::toDTO).toList())
                .stampDTOList(customerEntity.getStampEntities()
                        .stream().map(StampDTO::toDTO).toList())
                .build();
    }
}
