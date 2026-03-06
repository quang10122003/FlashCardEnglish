package com.TestFlashCard.FlashCard.repository.critetia;

import jakarta.persistence.criteria.Predicate;

public interface SearchQueryCriteriaConsumer<T> {

    void apply(SearchCriteria criteria);

    Predicate getPredicate();

    void setPredicate(Predicate predicate);
}
