package naeil.gen_coupon.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.StampEntity;

import java.time.LocalDate;

@Data
@Builder
public class StampDTO {

    private Integer stampId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createDate;
    private Integer issueId;

    public static StampDTO toDTO(StampEntity stampEntity) {
        return StampDTO.builder()
                .stampId(stampEntity.getStampId())
                .issueId(stampEntity.getIssueId())
                .createDate(stampEntity.getCreateDate())
                .build();
    }
}
