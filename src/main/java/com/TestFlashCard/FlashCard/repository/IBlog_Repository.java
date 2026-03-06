package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Blog;
import com.TestFlashCard.FlashCard.entity.BlogCategory;

@Repository
public interface IBlog_Repository extends JpaRepository<Blog,Integer> {
    List<Blog> findAllByCategory(BlogCategory category);
}