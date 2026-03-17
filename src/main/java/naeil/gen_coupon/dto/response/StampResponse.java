package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.StampDTO;
import naeil.gen_coupon.entity.StampEntity;

@SuperBuilder
public class StampResponse extends StampDTO {
    public static StampResponse toDTO(StampEntity stampEntity) {
        if (stampEntity == null) {
            return null;
        }
        return StampResponse.builder()
                .stampId(stampEntity.getStampId())
                .issueId(stampEntity.getIssueId())
                .createDate(stampEntity.getCreateDate())
                .build();
    }
}
