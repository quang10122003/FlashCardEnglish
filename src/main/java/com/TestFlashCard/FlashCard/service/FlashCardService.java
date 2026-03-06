package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import com.TestFlashCard.FlashCard.exception.DuplicateResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;
import com.TestFlashCard.FlashCard.Enum.LearningStatus;
import com.TestFlashCard.FlashCard.entity.Card;
import com.TestFlashCard.FlashCard.entity.FlashCard;
import com.TestFlashCard.FlashCard.entity.FlashCardTopic;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.ICard_Repository;
import com.TestFlashCard.FlashCard.repository.IFlashCardTopic_Repository;
import com.TestFlashCard.FlashCard.repository.IFlashCard_Repository;
import com.TestFlashCard.FlashCard.repository.IUser_Repository;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicUpdateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardUpdateRequest;
import com.TestFlashCard.FlashCard.response.CardsResponse;
import com.TestFlashCard.FlashCard.response.FlashCardTopicPublicResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardTopicResponse;
import com.TestFlashCard.FlashCard.response.ListFlashCardsResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlashCardService {

    @Autowired
    private MinIO_MediaService minIO_MediaService;

    @Autowired
    public final IFlashCard_Repository flashCard_Repository;
    @Autowired
    public final IFlashCardTopic_Repository flashCardTopic_Repository;
    @Autowired
    public final ICard_Repository card_Repository;
    @Autowired
    public final IUser_Repository user_Repository;
    @Autowired
    private final CardService cardService;
    @Autowired
    private final DigitalOceanStorageService storageService;

    public List<ListFlashCardTopicResponse> getFlashCardTopicsByUser(int userID) {
        if (!user_Repository.existsById(userID))
            throw new ResourceNotFoundException("User not found with id: " + userID);
        return flashCardTopic_Repository.findByUserId(userID).stream().map(this::convertTopicsResponse).toList();
    }

    public ListFlashCardTopicResponse getFlashCardTopicById(int id) throws IOException{
        FlashCardTopic topic = flashCardTopic_Repository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find the FlashCard Topic with id: " + id)
        );
        return convertTopicsResponse(topic);
    }

    public List<FlashCardTopicPublicResponse> getFlashCardTopicByVisitCount() throws IOException {
        List<FlashCardTopic> topics = flashCardTopic_Repository
                .findAllByStatusOrderByVisitCountDesc(FlashCardTopicStatus.PUBLIC);
        return topics.stream().map(this::convertToPublicTopicResponse).toList();
    }

    public List<ListFlashCardsResponse> getFlashCardsByTopic(int topicID){
        if (!flashCardTopic_Repository.existsById(topicID)) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicID);
        }

        return flashCard_Repository.findByTopicId(topicID).stream().map(this::convertToResponse).toList();
    }

    private ListFlashCardsResponse convertToResponse(FlashCard flashCard){
        return new ListFlashCardsResponse(
                flashCard.getId(),
                flashCard.getTitle(),
                flashCard.getReviewDate(),
                flashCard.getCycle(),
                flashCard.getLearningStatus().name(),
                getNumOfWordsOfFlashCard(flashCard.getId())
                );
    }
    public int getNumOfWordsOfFlashCard(int flashcardId){
        return card_Repository.countByFlashCardId(flashcardId);
    }

    private ListFlashCardTopicResponse convertTopicsResponse(FlashCardTopic topic) {
        return new ListFlashCardTopicResponse(topic.getId(), topic.getTitle(), topic.getStatus().name(),
                topic.getLeaningStatus().toString());
    }

    public ListFlashCardsResponse getFlashCardById(int id){
        FlashCard flashCard = flashCard_Repository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Cannot find flashcard with id: " + id));
        return convertToResponse(flashCard);
    }

    @Transactional
    public void createFlashCard(FlashCardCreateRequest flashCardDetail) throws IOException {
        FlashCardTopic topic = flashCardTopic_Repository.findById(flashCardDetail.getTopicID()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Cannot find FlashCard's topic with id: " + flashCardDetail.getTopicID()));

        FlashCard flashCard = new FlashCard();
        flashCard.setTitle(flashCardDetail.getTitle());
        flashCard.setCycle(flashCardDetail.getCycle());
        flashCard.setTopic(topic);
        flashCard.setLearningStatus(LearningStatus.NEW);
        flashCard_Repository.save(flashCard);
    }

    @Transactional
    public void createFlashCardTopic(FlashCardTopicCreateRequest flashCardTopicDetail, String accountName)
            throws IOException {
        User user = user_Repository.findByAccountName(accountName);
        if (flashCardTopic_Repository.existsByUserIdAndTitle(user.getId(), flashCardTopicDetail.getTitle()))
            throw new ResourceExistedException("Cannot create new FlashCard Topic. This topic has been existed!");
        FlashCardTopic topic = new FlashCardTopic();
        topic.setTitle(flashCardTopicDetail.getTitle());
        topic.setStatus(flashCardTopicDetail.getStatus());
        topic.setStatusGain(false);
        topic.setLeaningStatus(LearningStatus.NEW);
        topic.setVisitCount(0);
        topic.setUser(user);
        flashCardTopic_Repository.save(topic);
    }

    @Transactional
    public void updateTopic(FlashCardTopicUpdateRequest topicDetail, String accountName) {
        FlashCardTopic topic = flashCardTopic_Repository.findById(topicDetail.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find topic with id: " + topicDetail.getId()));
        User user = user_Repository.findByAccountName(accountName);
        if (!topicDetail.getTitle().equals(topic.getTitle())) {
            if (flashCardTopic_Repository.existsByUserIdAndTitle(user.getId(), topicDetail.getTitle()))
                throw new ResourceExistedException("Cannot update this FlashCard Topic. The topic: "
                        + topicDetail.getTitle() + " has been existed!");
        }
        if (topicDetail.getTitle() != null)
            topic.setTitle(topicDetail.getTitle());
        if (topicDetail.getStatus() != null)
            topic.setStatus(topicDetail.getStatus());
        if (topicDetail.getLearningStatus() != null)
            topic.setLeaningStatus(topicDetail.getLearningStatus());
        if(topicDetail.getVisitCount()!=null)
            topic.setVisitCount(topicDetail.getVisitCount());
        flashCardTopic_Repository.save(topic);
    }

    @Transactional
    public void updateVisitCountTopic(int topicId){
        FlashCardTopic topic = flashCardTopic_Repository.findById(topicId).orElseThrow(()-> new ResourceNotFoundException("Cannot find the topic with id: " + topicId));
        topic.setVisitCount(topic.getVisitCount()+1);
        flashCardTopic_Repository.save(topic);
    }

    @Transactional
    public void updateFlashCard(FlashCardUpdateRequest flashCardDetail) {
        FlashCard flashCard = flashCard_Repository.findById(flashCardDetail.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find FlashCard with id: " + flashCardDetail.getId()));
        if (flashCardDetail.getTitle() != null)
            flashCard.setTitle(flashCardDetail.getTitle());
        if (flashCardDetail.getCycle() != null)
            flashCard.setCycle(flashCardDetail.getCycle());
        if (flashCardDetail.getLearningStatus() != null)
            flashCard.setLearningStatus(flashCardDetail.getLearningStatus());
        if (flashCardDetail.getReviewDate() != null)
            flashCard.setReviewDate(flashCardDetail.getReviewDate());

        flashCard_Repository.save(flashCard);
    }

    @Transactional
    public void deleteTopic(int id) {
        FlashCardTopic topic = flashCardTopic_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find Topic with id: " + id));

        List<FlashCard> flashCards = flashCard_Repository.findByTopicId(id);
        for (FlashCard flashCard : flashCards) {
            List<CardsResponse> cards = cardService.getFlashCardDetail(flashCard.getId()).getListCardResponse();
            for (CardsResponse card : cards) {
                if (card.image() != null)
                    minIO_MediaService.deleteFile(card.image());
            }
        }
        flashCardTopic_Repository.delete(topic);
    }

    @Transactional
    public void deleteFlashCard(int id) {
        FlashCard flashCard = flashCard_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find FlashCard with id: " + id));
        flashCard_Repository.delete(flashCard);
    }
    public boolean checkTopicOfUser(int topicID, String accountName){
        FlashCardTopic topic = flashCardTopic_Repository.findById(topicID).orElseThrow(
                ()-> new ResourceNotFoundException("Cannot find Flashcard topic with id: "+ topicID)
        );
        User user = user_Repository.findByAccountName(accountName);
        if(topic.getUser().getId() == user.getId()){
            return true;
        }else{
            return false;
        }
    }

    private FlashCardTopicPublicResponse convertToPublicTopicResponse(FlashCardTopic topic) {
        return new FlashCardTopicPublicResponse(
                topic.getId(),
                topic.getUser().getAccountName(),
                topic.getTitle(),
                topic.getStatus().toString(),
                topic.getVisitCount());
    }
    public long countAllTopic(){
        return flashCardTopic_Repository.count();
    }

    @Transactional
    public String savePublishTopic(int topicID, String accountName) throws IOException{
        FlashCardTopic topic = flashCardTopic_Repository.findById(topicID).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find Flashcard topic with id: "+ topicID)
        );
        User user = user_Repository.findByAccountName(accountName);
        if(topic.getUser().getId() == user.getId()){
            throw new DuplicateResourceException("You are person who created this topic");
        }
        FlashCardTopic newTopic = new FlashCardTopic();
        newTopic.setTitle(topic.getTitle());
        newTopic.setLeaningStatus(LearningStatus.NEW);
        newTopic.setStatus(FlashCardTopicStatus.PRIVATE);
        newTopic.setUser(user);
        newTopic.setStatusGain(true);
        newTopic.setVisitCount(0);

        flashCardTopic_Repository.save(newTopic);

        for(FlashCard flashCard: topic.getFlashCards()){
            FlashCard newFlashCard = new FlashCard();
            newFlashCard.setCycle(flashCard.getCycle());
            newFlashCard.setLearningStatus(LearningStatus.NEW);
            newFlashCard.setTitle(flashCard.getTitle());
            newFlashCard.setTopic(newTopic);

            flashCard_Repository.save(newFlashCard);

            for(Card card: flashCard.getCards()){
                Card newCard = new Card();
                newCard.setAudio(card.getAudio());
                newCard.setDefinition(card.getDefinition());
                newCard.setExample(card.getExample());
                newCard.setFlashCard(newFlashCard);
                newCard.setIsRemember(0);
                newCard.setLevel(card.getLevel());
                newCard.setPartOfSpeech(card.getPartOfSpeech());
                newCard.setPronounce(card.getPronounce());
                newCard.setTerminology(card.getTerminology());
                newCard.setHint(card.getHint());
                //Copy file MiniIO
                if (card.getImage() != null) {
                    String newImageKey = minIO_MediaService.copyFile(card.getImage());
                    newCard.setImage(newImageKey); // lưu key mới vào bản copy
                }
//                System.out.println("COPY IMAGE: " + card.getImage());
//                minIO_MediaService.copyFile(card.getImage());
//                newCard.setImage(card.getImage());

                card_Repository.save(newCard);
            }
        }
        return "true";
    }
    public boolean shareTopic (int topicID){
        FlashCardTopic topic = flashCardTopic_Repository.findById(topicID).orElseThrow(()-> new ResourceNotFoundException("Cannot find Topic with id: "+ topicID));
        if(topic.getStatusGain() == false){
            topic.setStatus(FlashCardTopicStatus.PUBLIC);
            flashCardTopic_Repository.save(topic);
            return true;
        }
        return false;
    }
}