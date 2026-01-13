package naeil.gen_coupon.dto.querydsl;

import java.util.Map;

public record CustomerStampSummary(
    Map<Integer, Integer> stamps,
    Long total
) {}