package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.StampEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class StampDTO {

    private Integer stampId;
    private LocalDateTime createDate;
    private Integer issueId;

    public static StampDTO toDTO(StampEntity stampEntity) {
        return StampDTO.builder()
                .stampId(stampEntity.getStampId())
                .createDate(stampEntity.getCreateDate())
                .build();
    }
}
