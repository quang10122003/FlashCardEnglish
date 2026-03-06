package com.TestFlashCard.FlashCard.JpaSpec;

import org.springframework.data.jpa.domain.Specification;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;
import com.TestFlashCard.FlashCard.entity.FlashCardTopic;

public class FlashCardTopicSpecification {
    public static Specification<FlashCardTopic>hasStatus(FlashCardTopicStatus status){
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status.toString());
    }
}
