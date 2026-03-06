package com.TestFlashCard.FlashCard.JpaSpec;

import org.springframework.data.jpa.domain.Specification;

import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.entity.User;

public class UserSpecification {
    public static Specification<User> hasStatus(boolean status){
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), status);
    }
    public static Specification<User> hasRole(Role role){
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role.toString());
    }
}
