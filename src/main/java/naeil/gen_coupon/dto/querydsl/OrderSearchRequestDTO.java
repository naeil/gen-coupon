package naeil.gen_coupon.dto.querydsl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.common.dto.BaseSearchDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@SuperBuilder
public class OrderSearchRequestDTO extends BaseSearchDTO {

    private String shopCode;
    private Integer customerId;
    private String customerName;
    private Integer issueId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;


    public void normalize() {
        if (!StringUtils.hasText(shopCode)) shopCode = null;
        if (!StringUtils.hasText(customerName)) customerName = null;
    }
}
