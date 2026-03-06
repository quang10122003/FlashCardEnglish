package com.TestFlashCard.FlashCard.repository.critetia;

import jakarta.persistence.criteria.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class GenericSearchQueryCriteriaConsumer<T> implements SearchQueryCriteriaConsumer<T> {

    private Predicate predicate;
    private final CriteriaBuilder builder;
    private final Root<T> root;

    public GenericSearchQueryCriteriaConsumer(Predicate predicate, CriteriaBuilder builder, Root<T> root) {
        this.predicate = predicate;
        this.builder = builder;
        this.root = root;
    }
    @Override
    public void apply(SearchCriteria param) {
        Path<?> path = getPath(root, param.getKey());
        String op = param.getOperation();

        if (op.equals(">")) {
            predicate = builder.and(predicate, builder.greaterThan(path.as(String.class), param.getValue().toString()));
        } else if (op.equals(">=")) {
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(path.as(String.class), param.getValue().toString()));
        } else if (op.equals("<")) {
            predicate = builder.and(predicate, builder.lessThan(path.as(String.class), param.getValue().toString()));
        } else if (op.equals("<=")) {
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(path.as(String.class), param.getValue().toString()));
        } else if (op.equals(":")) {
            if (path.getJavaType() == String.class) {

                Expression<String> dbField = builder.lower(path.as(String.class));
                String value = param.getValue().toString().toLowerCase();

                predicate = builder.and(
                        predicate,
                        builder.like(dbField, "%" + value + "%")
                );

            } else {
                predicate = builder.and(predicate, builder.equal(path, param.getValue()));
            }
        }
    }
    @Override
    public void setPredicate(Predicate newPredicate) {
        this.predicate = newPredicate;
    }
    private String removeAccents(String s) {
        if (s == null) return null;
        // Chuyển sang dạng chuẩn hóa NFD (tách ký tự base + dấu)
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        // Xóa các ký tự dấu
        return normalized.replaceAll("\\p{M}", "");
    }

    private Path<?> getPath(Root<T> root, String key) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            Path<?> path = root;
            for (int i = 0; i < parts.length; i++) {
                if (i == parts.length - 1) {
                    path = path.get(parts[i]); // last part
                } else {
                    path = ((From<?, ?>) path).join(parts[i], JoinType.LEFT);
                }
            }
            return path;
        } else {
            return root.get(key);
        }
    }
}
