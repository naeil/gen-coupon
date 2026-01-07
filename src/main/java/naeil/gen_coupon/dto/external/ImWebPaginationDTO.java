package naeil.gen_coupon.dto.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImWebPaginationDTO {
    
    @JsonAlias("data_count")
    private String dataCount;   // 숫자지만 문자열로 옴

    @JsonAlias("current_page")
    private int currentPage;

    @JsonAlias("total_page")
    private int totalPage;

    @JsonProperty("pagesize")
    private int pageSize;
}
