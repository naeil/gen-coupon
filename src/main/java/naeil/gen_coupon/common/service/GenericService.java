package naeil.gen_coupon.common.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import naeil.gen_coupon.common.dto.BaseSearchDTO;

@Slf4j
@Service
public abstract class GenericService<T, Q extends EntityPath<T>, C extends BaseSearchDTO> {

    @Autowired
    private JPAQueryFactory queryFactory;

    protected abstract PathBuilder<T> getPathBuilder();

    public Pageable getPageable(C condition) {
        return PageRequest.of(condition.getPageNumber() - 1, condition.getPageSize());
    }

    /**
     * 페이징 + 동적 쿼리 검색 메소드
     *
     * @param condition          페이징 및 검색 조건을 담고 있는 DTO.
     * @param qClass            엔티티 클래스의 QueryDSL Q 타입.
     * @param predicateBuilder  쿼리 조건을 동적으로 생성하는 함수.
     * @param orderSpecifierBuilder 정렬 조건을 동적으로 생성하는 함수.
     * @return A paginated result of type Page<T>.
     */
    public Page<T> searchPage(C condition, Q qClass, Function<Q, BooleanBuilder> predicateBuilder, Function<Q, OrderSpecifier<?>[]> orderSpecifierBuilder) {
        Pageable pageable = getPageable(condition);

        Predicate whereClause = predicateBuilder.apply(qClass);
        List<T> results = queryFactory
                .selectFrom(qClass)
                .where(whereClause)
                .orderBy(orderSpecifierBuilder.apply(qClass))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Expressions.numberTemplate(Long.class, "count(*)"))
                .from(qClass)
                .where(whereClause)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;
        return new PageImpl<>(results, pageable, total);
    }

    /**
     * 전체 검색 메소드
     *
     * @param condition          검색 조건을 담고 있는 DTO.
     * @param qClass            엔티티 클래스의 QueryDSL Q 타입.
     * @param predicateBuilder  쿼리 조건을 동적으로 생성하는 함수.
     * @param orderSpecifierBuilder 정렬 조건을 동적으로 생성하는 함수.
     * @return A list of results of type List<T>.
     */
    public List<T> searchList(C condition, Q qClass, Function<Q, BooleanBuilder> predicateBuilder, Function<Q, OrderSpecifier<?>[]> orderSpecifierBuilder) {
        return queryFactory
                .selectFrom(qClass)
                .where(predicateBuilder.apply(qClass))
                .orderBy(orderSpecifierBuilder.apply(qClass))
                .fetch();
    }

    /**
     * 페이징 + 정렬된 전체 검색 메소드(동적 쿼리 조건 없이)
     * @param condition 페이징 조건을 담고 있는 DTO.
     * @param qClass    엔티티 클래스의 QueryDSL Q 타입.
     * @param orderSpecifierBuilder 정렬 조건을 동적으로 생성하는 함수.
     * @return A paginated result of type Page<T>.
     */
    public Page<T> findAllPage(C condition, Q qClass, Function<Q, OrderSpecifier<?>[]> orderSpecifierBuilder) {
        Pageable pageable = getPageable(condition);

        List<T> results = queryFactory
                .selectFrom(qClass)
                .orderBy(orderSpecifierBuilder.apply(qClass))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(Expressions.numberTemplate(Long.class, "count(*)"))
                .from(qClass)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(results, pageable, total);
    }
}