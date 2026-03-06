package com.TestFlashCard.FlashCard.JpaSpec;

import org.springframework.data.jpa.domain.Specification;

import com.TestFlashCard.FlashCard.entity.Evaluate;

public class EvaluateSpecification {
    public static Specification<Evaluate>hasStar(Integer star){
        return (root, query, cb) -> star == null ? null : cb.equal(root.get("star"),star);
    }
}
