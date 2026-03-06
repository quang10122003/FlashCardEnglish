package com.TestFlashCard.FlashCard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.Enum.EUserStatus;
import com.TestFlashCard.FlashCard.entity.User;

@Repository
public interface IUser_Repository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

     Optional<User> findById (Long id);
     User findByAccountName(String accountName);

    public User findByEmail(String email);

    public List<User> findAll();


 

    //Optional<User> findByIdAndIsDeleted(Integer id, boolean isDeleted);

    //Optional<User> findByAccountNameAndIsDeleted(String accountName, boolean isDeleted);

}
