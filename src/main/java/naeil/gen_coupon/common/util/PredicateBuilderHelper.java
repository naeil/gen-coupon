package naeil.gen_coupon.common.util;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

import java.util.Collection;

public class PredicateBuilderHelper {
    @SuppressWarnings("unchecked")
    public static <T> BooleanExpression eq(PathBuilder<T> path, String fieldName, Object value) {
        if (value == null)
            return null;
        String[] parts = fieldName.split("\\.");
        PathBuilder<?> parentPath = path;
        for (int i = 0; i < parts.length - 1; i++) {
            parentPath = parentPath.get(parts[i]);
        }
        return ((PathBuilder<Object>) parentPath).get(parts[parts.length - 1]).eq(value);
    }

    public static <T> BooleanExpression like(PathBuilder<T> path, String fieldName, String value) {
        if (value == null || value.isEmpty())
            return null;
        String[] parts = fieldName.split("\\.");
        PathBuilder<?> parentPath = path;
        for (int i = 0; i < parts.length - 1; i++) {
            parentPath = parentPath.get(parts[i]);
        }
        return parentPath.getString(parts[parts.length - 1]).likeIgnoreCase("%" + value + "%");
    }

    @SuppressWarnings("unchecked")
    public static <T> BooleanExpression in(PathBuilder<T> path, String fieldName, Collection<?> values) {
        if (values == null || values.isEmpty())
            return null;
        String[] parts = fieldName.split("\\.");
        PathBuilder<?> parentPath = path;
        for (int i = 0; i < parts.length - 1; i++) {
            parentPath = parentPath.get(parts[i]);
        }
        return ((PathBuilder<Object>) parentPath).get(parts[parts.length - 1]).in(values);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> BooleanExpression between(PathBuilder<T> path, String fieldName, Comparable<?> start,
            Comparable<?> end) {
        if (start == null && end == null)
            return null;

        String[] parts = fieldName.split("\\.");
        PathBuilder<?> parentPath = path;
        for (int i = 0; i < parts.length - 1; i++) {
            parentPath = parentPath.get(parts[i]);
        }
        String leaf = parts[parts.length - 1];
        Class clazz = (start != null ? start.getClass() : end.getClass());

        if (start != null && end != null) {
            return parentPath.getComparable(leaf, clazz).between(start, end);
        } else if (start != null) {
            return parentPath.getComparable(leaf, clazz).goe(start);
        } else {
            return parentPath.getComparable(leaf, clazz).loe(end);
        }
    }

}
