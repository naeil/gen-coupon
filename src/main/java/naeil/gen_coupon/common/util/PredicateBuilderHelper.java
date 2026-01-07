package naeil.gen_coupon.common.util;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

import java.util.Collection;

public class PredicateBuilderHelper {
    public static <T> BooleanExpression eq(PathBuilder<T> path, String fieldName, Object value) {
        return value != null ? path.get(fieldName).eq(value) : null;
    }

    public static <T> BooleanExpression like(PathBuilder<T> path, String fieldName, String value) {
        return value != null && !value.isEmpty()
                ? path.getString(fieldName).likeIgnoreCase("%" + value + "%") : null;
    }

    public static <T> BooleanExpression in(PathBuilder<T> path, String fieldName, Collection<?> values) {
        return values != null && !values.isEmpty()
                ? path.get(fieldName).in(values) : null;
    }

    public static <T> BooleanExpression between(PathBuilder<T> path, String fieldName, Comparable<?> start, Comparable<?> end) {
        if (start != null && end != null) {
            return path.getComparable(fieldName, Comparable.class).between(start, end);
        } else if (start != null) {
            return path.getComparable(fieldName, Comparable.class).goe(start);
        } else if (end != null) {
            return path.getComparable(fieldName, Comparable.class).loe(end);
        }
        return null;
    }
}
