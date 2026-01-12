package naeil.gen_coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.response.StampDTO;
import naeil.gen_coupon.entity.OrderHistoryEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StampService {
    
    private final StampRepository stampRepository;

    public List<StampDTO> getStampsByIssueId(Integer issueId) {
        
        List<StampEntity> stamps;
        if(issueId != null) {
            stamps = stampRepository.findByIssueId(issueId);
        } else {
            stamps = stampRepository.findAll();
        }

        return stamps.stream().map(stamp -> StampDTO.toDTO(stamp)).toList();
    }

    public void createStamp(List<OrderHistoryEntity> orderHistories) {

        List<StampEntity> stampEntities = new ArrayList<>();
        for(OrderHistoryEntity history : orderHistories) {
            StampEntity stamp = new StampEntity(history);
            stampEntities.add(stamp);
        }
        List<StampEntity> savedStampEntities = stampRepository.saveAll(stampEntities);

    }
}
