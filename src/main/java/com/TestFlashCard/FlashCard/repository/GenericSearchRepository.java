package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.repository.critetia.GenericSearchQueryCriteriaConsumer;
import com.TestFlashCard.FlashCard.repository.critetia.SearchCriteria;
import com.TestFlashCard.FlashCard.repository.critetia.SearchQueryCriteriaConsumer;
import com.TestFlashCard.FlashCard.response.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
@Slf4j
public class GenericSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> PageResponse<?> searchByCriteria(
            Class<T> entityClass,
            int pageNo,
            int pageSize,
            List<SearchCriteria> criteriaList,
            String sortBy,
            SearchQueryCriteriaConsumer<T> consumer
    ) {

        List<T> items = getItems(entityClass, pageNo, pageSize, criteriaList, sortBy, consumer);
        Long total = getTotalElements(entityClass, criteriaList, consumer);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .items(items)
                .build();
    }


    private <T> List<T> getItems(
            Class<T> entityClass,
            int pageNo,
            int pageSize,
            List<SearchCriteria> criteriaList,
            String sortBy,
            SearchQueryCriteriaConsumer<T> consumer
    ) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        Predicate predicate = cb.conjunction();
//        consumer.setPredicate(predicate);
        consumer = new GenericSearchQueryCriteriaConsumer<>(predicate, cb, root);

        // apply all criteria
        criteriaList.forEach(consumer::apply);

        predicate = consumer.getPredicate();
        query.where(predicate);

        // --- Sorting ---
        Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
        if (StringUtils.hasLength(sortBy)) {
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String field = matcher.group(1);
                String dir = matcher.group(3);

                query.orderBy(
                        dir.equalsIgnoreCase("asc")
                                ? cb.asc(root.get(field))
                                : cb.desc(root.get(field))
                );
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }


    private <T> Long getTotalElements(
            Class<T> entityClass,
            List<SearchCriteria> criteriaList,
            SearchQueryCriteriaConsumer<T> consumer
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        Predicate predicate = cb.conjunction();
//        consumer.setPredicate(predicate);
        consumer = new GenericSearchQueryCriteriaConsumer<>(predicate, cb, root);
        criteriaList.forEach(consumer::apply);

        predicate = consumer.getPredicate();
        query.select(cb.count(root)).where(predicate);

        return entityManager.createQuery(query).getSingleResult();
    }
}
