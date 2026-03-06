package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Card;

@Repository
public interface ICard_Repository extends JpaRepository<Card,Integer> {
    public Card findByTerminologyIgnoreCaseAndFlashCardIdAndPartOfSpeech(String terminology, Integer flashCardId, String partOfSpeech);
    List<Card> findByFlashCardId(int flashCardID);
    int countByFlashCardId(Integer flashCardID);
}
