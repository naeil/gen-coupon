package naeil.gen_coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.response.StampDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.OrderHistoryEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StampService {
    
    private final StampRepository stampRepository;
    private final ConfigRepository configRepository;
    private final MessageService messageService;

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

        List<StampEntity> stampEntities = orderHistories.stream()
                .map(StampEntity::new)
                .toList();

        stampRepository.saveAll(stampEntities);

        List<StampEntity> pendingStamps = stampRepository.findAllPendingNotifications();

        if(pendingStamps.isEmpty()) {
            return;
        }

        List<Integer> targetCustomerIds = pendingStamps.stream()
                .map(s -> s.getCustomerEntity().getCustomerId())
                .distinct()
                .toList();

        List<Object[]> countResults = stampRepository.countStampsByCustomerIds(targetCustomerIds);

        // 4. 사용하기 편하게 Map<고객ID, 개수> 형태로 변환
        Map<Integer, Integer> totalCountMap = countResults.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],             // Key: 고객 ID
                        row -> ((Number) row[1]).intValue()              // Value: 스탬프 개수 (Count는 보통 Long 반환)
                ));


        ConfigEntity config = configRepository.findByConfigKey("minimum_count").orElse(null);
        int standardCount = config != null ? Integer.parseInt(config.getConfigValue()) : 10;

        Map<Integer, List<StampEntity>> alarmCandidates = new HashMap<>();

        for(StampEntity stamp : pendingStamps) {
            CustomerEntity customer = stamp.getCustomerEntity();
            int currentTotal = (int) totalCountMap.getOrDefault(customer.getCustomerId(), 0);

            if(currentTotal < standardCount) {
                alarmCandidates.computeIfAbsent(customer.getCustomerId(), k -> new ArrayList<>())
                        .add(stamp);
            }
        }

        if(!alarmCandidates.isEmpty()) {
            messageService.sendStampAlimTok(alarmCandidates, totalCountMap);
        }
    }
}
