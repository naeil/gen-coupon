package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.CustomerEntity;

@Data
@Builder
public class CustomerDTO {

    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private String customerHtel;
    private Integer totalOrderCount;

    public static CustomerDTO toDTO(CustomerEntity customerEntity) {
        return CustomerDTO.builder()
                .customerId(customerEntity.getCustomerId())
                .customerName(customerEntity.getCustomerName())
                .customerEmail(customerEntity.getCustomerEmail())
                .customerHtel(customerEntity.getCustomerHtel())
                .totalOrderCount(customerEntity.getTotalOrderCount())
                .build();
    }
}
