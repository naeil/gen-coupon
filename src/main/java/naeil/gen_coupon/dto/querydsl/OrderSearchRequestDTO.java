package naeil.gen_coupon.dto.querydsl;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.common.dto.BaseSearchDTO;

@Getter
@NoArgsConstructor
@SuperBuilder
public class OrderSearchRequestDTO extends BaseSearchDTO {
    
    private Integer customerId;
    private String customerName;
    private Integer issueId;
    private LocalDate fromDate;
    private LocalDate toDate; 
}
