package naeil.gen_coupon.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.response.StampDTO;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.StampRepository;

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
}
