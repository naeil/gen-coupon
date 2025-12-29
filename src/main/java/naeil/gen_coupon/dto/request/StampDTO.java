package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.StampEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class StampDTO {

    private Integer stampId;
    private Integer count;
    private LocalDateTime updateAt;

    public static StampDTO toDTO(StampEntity stampEntity) {
        return StampDTO.builder()
                .stampId(stampEntity.getStampId())
                .count(stampEntity.getCount())
                .updateAt(stampEntity.getUpdateAt())
                .build();
    }
}
