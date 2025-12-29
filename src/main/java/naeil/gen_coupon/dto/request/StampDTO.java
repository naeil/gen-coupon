package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.StampEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class StampDTO {

    private Integer stampId;
    private LocalDateTime createDate;

    public static StampDTO toDTO(StampEntity stampEntity) {
        return StampDTO.builder()
                .stampId(stampEntity.getStampId())
                .createDate(stampEntity.getCreateDate())
                .build();
    }
}
