package naeil.gen_coupon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagingInfoDTO {

    private long totalElements; // 전체 데이터 수
    private int pageNumber;    // 현재 페이지 번호
    private int pageSize;      // 현재 페이지의 데이터 수
}
