package naeil.gen_coupon.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseSearchDTO {

    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Builder.Default
    private int pageNumber = DEFAULT_PAGE_NUMBER;

    @Builder.Default
    private int pageSize = DEFAULT_PAGE_SIZE;
}
