package naeil.gen_coupon.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.service.GenericService;
import naeil.gen_coupon.common.util.PredicateBuilderHelper;
import naeil.gen_coupon.dto.querydsl.CouponSearchRequestDTO;
import naeil.gen_coupon.dto.response.CouponIssueDTO;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.entity.QCouponIssueEntity;
import naeil.gen_coupon.repository.CouponIssueRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService extends GenericService<CouponIssueEntity, QCouponIssueEntity, CouponSearchRequestDTO> {
    
    private final CouponIssueRepository couponRepository;

    @Transactional(readOnly = true)
    public List<CouponIssueDTO> searchCouponIssueList(CouponSearchRequestDTO requestDTO) {
        List<CouponIssueEntity> searchedList = searchList(
                requestDTO,
                QCouponIssueEntity.couponIssueEntity, q -> buildPredicate(requestDTO),
                q -> buildOrderSpecifier(requestDTO, q)
        );

        return searchedList.stream().map(coupon -> CouponIssueDTO.toDTO(coupon)).toList();
    }

    @Override
    protected PathBuilder<CouponIssueEntity> getPathBuilder() {
        return new PathBuilder<>(CouponIssueEntity.class, "couponIssueEntity");
    }

    private BooleanBuilder buildPredicate(CouponSearchRequestDTO condition) {
        PathBuilder<CouponIssueEntity> path = getPathBuilder();
        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime start = null;
        if(condition.getFromDate() != null) {
            start = LocalDateTime.parse(condition.getFromDate() + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime stop = null;
        if(condition.getToDate() != null) {
            stop = LocalDateTime.parse(condition.getToDate() + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        builder.and(PredicateBuilderHelper.eq(path, "customerEntity.customerId", condition.getCustomerId()));
        builder.and(PredicateBuilderHelper.like(path, "customerEntity.customerName", condition.getCustomerName()));
        builder.and(PredicateBuilderHelper.between(path, "createDate", start, stop));

        return builder;
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(CouponSearchRequestDTO condition, QCouponIssueEntity qClass) {
        return new OrderSpecifier[] {
                qClass.createDate.desc()
        };
    }
}
