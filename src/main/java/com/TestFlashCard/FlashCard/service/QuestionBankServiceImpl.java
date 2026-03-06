package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.Utils.SearchCriteriaUtils;
import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.exception.DuplicateGroupInBankException;
import com.TestFlashCard.FlashCard.exception.DuplicateQuestionInBankException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.mapper.BankMapper;
import com.TestFlashCard.FlashCard.repository.*;
import com.TestFlashCard.FlashCard.repository.critetia.GenericSearchQueryCriteriaConsumer;
import com.TestFlashCard.FlashCard.repository.critetia.SearchCriteria;
import com.TestFlashCard.FlashCard.repository.critetia.SearchQueryCriteriaConsumer;
import com.TestFlashCard.FlashCard.response.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {
        private static final Logger log = LoggerFactory.getLogger(QuestionBankServiceImpl.class);
        private final ToeicQuestionRepository toeicRepo;
        private final BankToeicQuestionRepoitory bankToeicRepo;
        private final ExamRepository examRepo;
        private final BankMapper bankMapper;
        private final IUser_Repository IUser_Repository;
        private final BankGroupQuestionRepository bankGroupQuestionRepository;
        private final GroupQuestionRepository groupQuestionRepository;
        private final ToeicQuestionRepository toeicQuestionRepository;
        private final BankToeicOptionRepoitory bankToeicOptionRepoitory;
        private final BankGroupChildQuestionRepository bankGroupChildQuestionRepository;
        private final GenericSearchRepository genericSearchRepository;
        private final ExamRepository examRepository;
        private final ToeicQuestionOptionRepository toeicQuestionOptionRepository;
        private final ToeicQuestionImageRepository toeicQuestionImageRepository;
        private final GroupQuestionImageRepository groupQuestionImageRepository;
        private final GroupQuestionAudioRepository groupQuestionAudioRepository;
        private final MinIO_MediaService minIOMediaService;

        // ===== TOEIC INDEX STANDARDS =====
        private static final Map<String, Integer> PART_START_INDEX = Map.of(
                        "1", 1, // Part 1: 1-6
                        "2", 7, // Part 2: 7-31
                        "3", 32, // Part 3: 32-70
                        "4", 71, // Part 4: 71-100
                        "5", 101, // Part 5: 101-130
                        "6", 131, // Part 6: 131-146
                        "7", 147 // Part 7: 147-200
        );

        /**
         * Get start index for a part according to TOEIC standard
         */
        private int getStartIndexByPart(String part) {
                return PART_START_INDEX.getOrDefault(part, 1);
        }

        /**
         * Calculate next index for a question in exam
         * - isRandom = true: sequential from max index + 1
         * - isRandom = false: based on TOEIC standard part ranges
         */
        private int calculateNextIndex(Exam exam, String part, Map<String, Integer> partMaxIndexMap) {
                if (exam.isRandom()) {
                        // Random exam: get max index in entire exam + 1
                        Integer maxIndex = toeicQuestionRepository.findMaxIndexByExam(exam.getId());
                        return (maxIndex != null ? maxIndex : 0) + 1;
                } else {
                        // Standard TOEIC: calculate based on part
                        if (!partMaxIndexMap.containsKey(part)) {
                                Integer maxIndexInPart = toeicQuestionRepository.findMaxIndexByExamAndPart(exam.getId(),
                                                part);
                                int baseIndex = getStartIndexByPart(part);
                                // If no questions in part yet, start from baseIndex - 1 so first question gets
                                // baseIndex
                                partMaxIndexMap.put(part, maxIndexInPart != null ? maxIndexInPart : baseIndex - 1);
                        }
                        int currentMax = partMaxIndexMap.get(part);
                        int nextIndex = currentMax + 1;
                        partMaxIndexMap.put(part, nextIndex);
                        return nextIndex;
                }
        }

        @Override
        @Transactional
        public List<BankToeicQuestionResponse> contributeManyToeicQuestions(List<Integer> ids) {
                // Check trùng khi đóng góp cùng 1 câu hỏi vào ngân hàng nhiều lần
                List<BankToeicQuestion> existed = bankToeicRepo.findBySourceToeicIds(ids);

                if (!existed.isEmpty()) {
                        List<BankToeicQuestionResponse> res = existed.stream()
                                        .map(bankMapper::mapToResponse)
                                        .toList();
                        throw new DuplicateQuestionInBankException(res);
                }

                List<ToeicQuestion> questions = toeicRepo.findAllById(ids);
                if (questions.size() != ids.size()) {
                        throw new RuntimeException("Some questions not found");
                }

                User contributor = getCurrentUser();

                // FIX: Copy media khi contribute để tránh mất data khi xóa gốc
                List<BankToeicQuestion> banks = questions.stream()
                                .map(q -> {
                                        BankToeicQuestion b = mapToeicToBankWithMediaCopy(q);
                                        b.setContributor(contributor);
                                        q.setIsContribute(true);
                                        return b;
                                })
                                .toList();

                bankToeicRepo.saveAll(banks);
                toeicRepo.saveAll(questions); // lưu isContribute


                return banks.stream()
                        .map(bankMapper::mapToResponse)
                        .toList();
        }

        /**
         * Map ToeicQuestion to BankToeicQuestion with MEDIA COPY
         * Creates new MinIO keys for images and audio
         */
        private BankToeicQuestion mapToeicToBankWithMediaCopy(ToeicQuestion q) {
                BankToeicQuestion b = new BankToeicQuestion();
                b.setPart(q.getPart());
                b.setDetail(q.getDetail());
                b.setResult(q.getResult());
                b.setClarify(q.getClarify());
                b.setIsPublic(true);
                b.setSourceToeicId(q.getId());

                // AUDIO: Copy to new key
                if (q.getAudio() != null && !q.getAudio().isBlank()) {
                        String newAudioKey = minIOMediaService.copyFile(q.getAudio());
                        b.setAudio(newAudioKey);
                }

                // OPTIONS
                List<BankToeicOption> options = q.getOptions().stream().map(o -> {
                        BankToeicOption x = new BankToeicOption();
                        x.setMark(o.getMark());
                        x.setDetail(o.getDetail());
                        x.setQuestion(b);
                        return x;
                }).toList();
                b.setOptions(options);

                // IMAGES: Copy to new keys
                List<BankImage> images = q.getImages().stream().map(i -> {
                        BankImage x = new BankImage();
                        String newImageKey = minIOMediaService.copyFile(i.getUrl());
                        x.setUrl(newImageKey);
                        x.setQuestion(b);
                        return x;
                }).toList();
                b.setImages(images);

                return b;
        }

        private User getCurrentUser() {
                String accountName = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();
                log.info("Current user: {}", accountName);
                User user = IUser_Repository.findByAccountName(accountName);
                if (user == null) {
                        throw new RuntimeException("User not found");
                }
                return user;
        }

        @Override
        @Transactional
        public List<BankGroupQuestionResponse> contributeManyGroupQuestions(List<Integer> ids) {

                // Check trùng trong bank
                List<Integer> existedIds = bankGroupQuestionRepository.findExistingSourceIds(ids);

                if (!existedIds.isEmpty()) {
                        List<BankGroupQuestion> existed = bankGroupQuestionRepository.findBySourceGroupIds(existedIds);

                        List<BankGroupQuestionResponse> res = existed.stream()
                                        .map(bankMapper::mapGroupToResponse)
                                        .toList();

                        throw new DuplicateGroupInBankException(res);
                }

                // Load group + images + audios
                List<GroupQuestion> groups = groupQuestionRepository.findGroupsWithMedia(ids);

                if (groups.size() != ids.size()) {
                        throw new RuntimeException("Some group questions not found");
                }

                // Load questions + options
                List<ToeicQuestion> questions = toeicQuestionRepository.findQuestionsWithOptionsByGroupIds(ids);

                // Group questions by groupId
                Map<Integer, List<ToeicQuestion>> questionMap = questions.stream().collect(Collectors.groupingBy(
                                q -> q.getGroup().getId()));

                User contributor = getCurrentUser();

                // FIX: Map to bank with MEDIA COPY
                List<BankGroupQuestion> banks = groups.stream()
                                .map(g -> mapGroupToBankWithMediaCopy(
                                                g,
                                                contributor,
                                                questionMap.getOrDefault(g.getId(), List.of())))
                                .toList();

                bankGroupQuestionRepository.saveAll(banks);

                // ================== FIX: SET isContribute + bankGroupId ==================

                Map<Integer, BankGroupQuestion> bankMap = banks.stream()
                        .collect(Collectors.toMap(
                                b -> b.getSourceGroupId(),
                                b -> b));

                for (GroupQuestion g : groups) {
                        BankGroupQuestion bank = bankMap.get(g.getId());
                        g.setIsContribute(true);              //  group đã được contribute
                      //  g.setBankGroupId(bank.getId());       //  để FE biết bank id
                }

                groupQuestionRepository.saveAll(groups);

                // ========================================================================

                return banks.stream()
                        .map(bankMapper::mapGroupToResponse)
                        .toList();
        }

        /**
         * Map GroupQuestion to BankGroupQuestion with MEDIA COPY
         */
        private BankGroupQuestion mapGroupToBankWithMediaCopy(
                        GroupQuestion g,
                        User contributor,
                        List<ToeicQuestion> questions) {
                BankGroupQuestion bg = new BankGroupQuestion();
                bg.setPart(g.getPart());
                bg.setContent(g.getContent());
                bg.setContributor(contributor);
                bg.setSourceGroupId(g.getId());

                // IMAGES: Copy to new keys
                Set<BankGroupImage> imgs = g.getImages().stream()
                                .map(i -> {
                                        BankGroupImage x = new BankGroupImage();
                                        String newKey = minIOMediaService.copyFile(i.getUrl());
                                        x.setImageKey(newKey);
                                        x.setGroup(bg);
                                        return x;
                                })
                                .collect(Collectors.toSet());
                bg.setImages(imgs);

                // AUDIOS: Copy to new keys
                Set<BankGroupAudio> auds = g.getAudios().stream()
                                .map(a -> {
                                        BankGroupAudio x = new BankGroupAudio();
                                        String newKey = minIOMediaService.copyFile(a.getUrl());
                                        x.setAudioKey(newKey);
                                        x.setGroup(bg);
                                        return x;
                                })
                                .collect(Collectors.toSet());
                bg.setAudios(auds);

                // Child questions (no media to copy for child questions in group)
                List<BankGroupChildQuestion> children = questions.stream().map(q -> {
                        BankGroupChildQuestion c = new BankGroupChildQuestion();
                        c.setIndexNumber(q.getIndexNumber());
                        c.setDetail(q.getDetail());
                        c.setResult(q.getResult());
                        c.setClarify(q.getClarify());
                        c.setGroup(bg);

                        List<BankToeicOption> ops = q.getOptions().stream().map(o -> {
                                BankToeicOption bo = new BankToeicOption();
                                bo.setMark(o.getMark());
                                bo.setDetail(o.getDetail());
                                bo.setChildQuestion(c);
                                return bo;
                        }).toList();

                        c.setOptions(ops);
                        return c;
                }).toList();

                bg.setQuestions(children);

                return bg;
        }

        // ==========================
        // BANK → EXAM (SỬ DỤNG)
        // ==========================

//        @Override
//        @Transactional
//        public List<BankUseSingleQuestionResponse> useSingleQuestions(List<Integer> ids, int examId) {
//
//                // 0. Check exam tồn tại
//                Exam exam = examRepository.findById(examId)
//                                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
//
//                // 1. Load bank questions
//                List<BankToeicQuestion> bankQuestions = bankToeicRepo.findWithImages(ids);
//
//                // 2. Check câu nào đã được dùng trong exam
//                List<Integer> bankIds = bankQuestions.stream()
//                                .map(BankToeicQuestion::getId)
//                                .toList();
//
//                List<Integer> usedIds = toeicQuestionRepository.findUsedBankQuestionIds(examId, bankIds);
//
//                Set<Integer> usedSet = new HashSet<>(usedIds);
//
//                // 3. Filter chỉ giữ câu CHƯA dùng
//                List<BankToeicQuestion> newQuestions = bankQuestions.stream()
//                                .filter(bq -> !usedSet.contains(bq.getId()))
//                                .toList();
//
//                if (newQuestions.isEmpty()) {
//                        throw new RuntimeException("All selected questions already exist in this exam");
//                }
//
//                // 4. Load options CHỈ cho câu mới
//                List<Integer> newIds = newQuestions.stream()
//                                .map(BankToeicQuestion::getId)
//                                .toList();
//
//                List<BankToeicOption> bankOptions = bankToeicOptionRepoitory.findOptionsByQuestionIds(newIds);
//
//                Map<Integer, List<BankToeicOption>> optionMap = bankOptions.stream().collect(Collectors.groupingBy(
//                                o -> o.getQuestion().getId()));
//
//                // FIX: Chuẩn bị map để track max index theo part
//                Map<String, Integer> partMaxIndexMap = new HashMap<>();
//
//                // 5. Add câu hỏi vào exam
//                for (BankToeicQuestion bq : newQuestions) {
//
//                        // FIX: Calculate index đúng theo TOEIC standard
//                        int indexNumber = calculateNextIndex(exam, bq.getPart(), partMaxIndexMap);
//
//                        // Map question với MEDIA COPY
//                        ToeicQuestion q = mapToeicQuestionFromBankWithMediaCopy(bq, exam, indexNumber);
//                        toeicQuestionRepository.save(q);
//
//                        // Map options
//                        List<BankToeicOption> ops = optionMap.getOrDefault(bq.getId(), List.of());
//
//                        Set<ToeicQuestionOption> examOps = ops.stream()
//                                        .map(o -> bankMapper.mapOptionFromBank(o, q))
//                                        .collect(Collectors.toSet());
//
//                        toeicQuestionOptionRepository.saveAll(examOps);
//                        q.setOptions(examOps);
//
//                        // FIX: Map images với COPY
//                        if (bq.getImages() != null && !bq.getImages().isEmpty()) {
//                                List<ToeicQuestionImage> imgs = bq.getImages().stream()
//                                                .map(i -> mapImageFromBankWithCopy(i, q))
//                                                .toList();
//                                toeicQuestionImageRepository.saveAll(imgs);
//                                q.setImages(imgs);
//                        }
//                }
//
//                // 6. Trả response
//                return newQuestions.stream()
//                                .map(bankMapper::toSingleResponse)
//                                .toList();
//        }
        @Override
        @Transactional
        public List<BankUseSingleQuestionResponse> useSingleQuestions(List<Integer> ids, int examId) {

                // 0. Check exam tồn tại
                Exam exam = examRepository.findById(examId)
                        .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

                // 1. Load bank questions
                List<BankToeicQuestion> bankQuestions = bankToeicRepo.findWithImages(ids);

                if (bankQuestions.isEmpty()) {
                        throw new RuntimeException("No bank questions found");
                }

                List<Integer> bankIds = bankQuestions.stream()
                        .map(BankToeicQuestion::getId)
                        .toList();

                // 2. Câu đã được dùng trong exam
                List<Integer> usedIds =
                        toeicQuestionRepository.findUsedBankQuestionIds(examId, bankIds);

                Set<Integer> usedSet = new HashSet<>(usedIds);

                // 3. Lấy toàn bộ questionId của exam hiện tại
                List<Integer> examQuestionIds =
                        toeicQuestionRepository.findQuestionIdsByExamId(examId);

                Set<Integer> examQuestionIdSet = new HashSet<>(examQuestionIds);

                // 4. Filter câu hợp lệ
                List<BankToeicQuestion> validQuestions = bankQuestions.stream()
                        // chưa dùng trong exam
                        .filter(bq -> !usedSet.contains(bq.getId()))
                        // không phải contribute từ chính exam này
                        .filter(bq -> bq.getSourceToeicId() == null
                                || !examQuestionIdSet.contains(bq.getSourceToeicId()))
                        .toList();

                // ===== PHÂN BIỆT LỖI =====

                if (validQuestions.isEmpty()) {

                        boolean allAlreadyUsed =
                                bankQuestions.stream()
                                        .allMatch(bq -> usedSet.contains(bq.getId()));

                        boolean allFromThisExam =
                                bankQuestions.stream()
                                        .allMatch(bq -> bq.getSourceToeicId() != null
                                                && examQuestionIdSet.contains(bq.getSourceToeicId()));

                        if (allAlreadyUsed) {
                                throw new RuntimeException("All selected questions already exist in this exam");
                        }

                        if (allFromThisExam) {
                                throw new RuntimeException(
                                        "Các câu hỏi được đóng góp từ đề thi này không thể được sử dụng lại trong cùng một đề thi.");
                        }

                        throw new RuntimeException("No valid questions to add to exam");
                }

                // 5. Load options cho câu hợp lệ
                List<Integer> newIds = validQuestions.stream()
                        .map(BankToeicQuestion::getId)
                        .toList();

                List<BankToeicOption> bankOptions =
                        bankToeicOptionRepoitory.findOptionsByQuestionIds(newIds);

                Map<Integer, List<BankToeicOption>> optionMap =
                        bankOptions.stream().collect(Collectors.groupingBy(
                                o -> o.getQuestion().getId()));

                // track max index theo part
                Map<String, Integer> partMaxIndexMap = new HashMap<>();

                // 6. Add câu hỏi vào exam
                for (BankToeicQuestion bq : validQuestions) {

                        int indexNumber = calculateNextIndex(exam, bq.getPart(), partMaxIndexMap);

                        ToeicQuestion q =
                                mapToeicQuestionFromBankWithMediaCopy(bq, exam, indexNumber);

                        toeicQuestionRepository.save(q);

                        List<BankToeicOption> ops = optionMap.getOrDefault(bq.getId(), List.of());

                        Set<ToeicQuestionOption> examOps = ops.stream()
                                .map(o -> bankMapper.mapOptionFromBank(o, q))
                                .collect(Collectors.toSet());

                        toeicQuestionOptionRepository.saveAll(examOps);
                        q.setOptions(examOps);

                        if (bq.getImages() != null && !bq.getImages().isEmpty()) {
                                List<ToeicQuestionImage> imgs = bq.getImages().stream()
                                        .map(i -> mapImageFromBankWithCopy(i, q))
                                        .toList();
                                toeicQuestionImageRepository.saveAll(imgs);
                                q.setImages(imgs);
                        }
                }

                // 7. Response
                return validQuestions.stream()
                        .map(bankMapper::toSingleResponse)
                        .toList();
        }
        /**
         * Map BankToeicQuestion to ToeicQuestion with MEDIA COPY
         */
        private ToeicQuestion mapToeicQuestionFromBankWithMediaCopy(
                        BankToeicQuestion bq, Exam exam, int indexNumber) {
                ToeicQuestion q = new ToeicQuestion();

                q.setExam(exam);
                q.setGroup(null);

                q.setPart(bq.getPart());
                q.setDetail(bq.getDetail());
                q.setResult(bq.getResult());
                q.setClarify(bq.getClarify());
                q.setBankQuestionId(bq.getId());
                q.setIndexNumber(indexNumber);

                // AUDIO: Copy to new key
                if (bq.getAudio() != null && !bq.getAudio().isBlank()) {
                        String newAudioKey = minIOMediaService.copyFile(bq.getAudio());
                        q.setAudio(newAudioKey);
                }

                return q;
        }

        /**
         * Map BankImage to ToeicQuestionImage with COPY
         */
        private ToeicQuestionImage mapImageFromBankWithCopy(BankImage bi, ToeicQuestion q) {
                ToeicQuestionImage img = new ToeicQuestionImage();
                img.setToeicQuestion(q);
                String newKey = minIOMediaService.copyFile(bi.getUrl());
                img.setUrl(newKey);
                return img;
        }

//        @Override
//        @Transactional
//        public List<BankUseGroupQuestionResponse> useGroupQuestions(List<Long> groupIds, int examId) {
//
//                // 1. Check exam
//                Exam exam = examRepository.findById(examId)
//                                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
//
//                // 2. Load bank groups (children + images + audios)
//                List<BankGroupQuestion> bankGroups = bankGroupQuestionRepository.findGroupsWithMedia(groupIds);
//
//                // 3. Chống trùng group
//                List<Long> bankGroupIds = bankGroups.stream()
//                                .map(BankGroupQuestion::getId)
//                                .toList();
//
//                List<Long> usedIds = groupQuestionRepository.findUsedBankGroupIds(examId, bankGroupIds);
//
//                Set<Long> usedSet = new HashSet<>(usedIds);
//
//                List<BankGroupQuestion> newGroups = bankGroups.stream()
//                                .filter(g -> !usedSet.contains(g.getId()))
//                                .toList();
//
//                if (newGroups.isEmpty()) {
//                        throw new RuntimeException("All selected groups already exist in this exam");
//                }
//
//                // FIX: Chuẩn bị map để track max index theo part
//                Map<String, Integer> partMaxIndexMap = new HashMap<>();
//
//                // 4. Add groups
//                for (BankGroupQuestion bg : newGroups) {
//
//                        // Save group với MEDIA COPY
//                        GroupQuestion examGroup = mapGroupFromBankWithMediaCopy(bg, exam);
//                        groupQuestionRepository.save(examGroup);
//
//                        // FIX: Images với COPY
//                        if (bg.getImages() != null && !bg.getImages().isEmpty()) {
//                                Set<GroupQuestionImage> imgs = bg.getImages().stream()
//                                                .map(i -> mapGroupImageFromBankWithCopy(i, examGroup))
//                                                .collect(Collectors.toSet());
//                                groupQuestionImageRepository.saveAll(imgs);
//                                examGroup.setImages(imgs);
//                        }
//
//                        // FIX: Audios với COPY
//                        if (bg.getAudios() != null && !bg.getAudios().isEmpty()) {
//                                Set<GroupQuestionAudio> audios = bg.getAudios().stream()
//                                                .map(a -> mapGroupAudioFromBankWithCopy(a, examGroup))
//                                                .collect(Collectors.toSet());
//                                groupQuestionAudioRepository.saveAll(audios);
//                                examGroup.setAudios(audios);
//                        }
//
//                        // Add child questions
//                        for (BankGroupChildQuestion bq : bg.getQuestions()) {
//
//                                // FIX: Calculate index đúng theo TOEIC standard
//                                int indexNumber = calculateNextIndex(exam, bg.getPart(), partMaxIndexMap);
//
//                                ToeicQuestion q = bankMapper.mapToeicQuestionFromBank(bq, examGroup, exam, indexNumber);
//                                toeicQuestionRepository.save(q);
//
//                                // Gắn vào group list (để tính range)
//                                examGroup.getQuestions().add(q);
//
//                                // Options
//                                if (bq.getOptions() != null && !bq.getOptions().isEmpty()) {
//                                        Set<ToeicQuestionOption> ops = bq.getOptions().stream()
//                                                        .map(o -> bankMapper.mapOptionFromBank(o, q))
//                                                        .collect(Collectors.toSet());
//                                        toeicQuestionOptionRepository.saveAll(ops);
//                                        q.setOptions(ops);
//                                }
//                        }
//
//                        // Set title + questionRange
//                        List<ToeicQuestion> qs = examGroup.getQuestions();
//
//                        int min = qs.stream().mapToInt(ToeicQuestion::getIndexNumber).min().orElse(0);
//                        int max = qs.stream().mapToInt(ToeicQuestion::getIndexNumber).max().orElse(0);
//
//                        examGroup.setQuestionRange(min + "-" + max);
//                        examGroup.setTitle("Questions " + min + " - " + max);
//
//                        groupQuestionRepository.save(examGroup);
//                }
//
//                // 5. Response từ BANK
//                return newGroups.stream()
//                                .map(bg -> bankMapper.toGroupResponse(bg, bg.getQuestions()))
//                                .toList();
//        }

        @Override
        @Transactional
        public List<BankUseGroupQuestionResponse> useGroupQuestions(List<Long> groupIds, int examId) {

                // 1. Check exam
                Exam exam = examRepository.findById(examId)
                        .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

                // 2. Load bank groups (children + images + audios)
                List<BankGroupQuestion> bankGroups =
                        bankGroupQuestionRepository.findGroupsWithMedia(groupIds);

                if (bankGroups.isEmpty()) {
                        throw new RuntimeException("No bank groups found");
                }

                List<Long> bankGroupIds = bankGroups.stream()
                        .map(BankGroupQuestion::getId)
                        .toList();

                // 3. Group đã được dùng trong exam
                List<Long> usedIds =
                        groupQuestionRepository.findUsedBankGroupIds(examId, bankGroupIds);

                Set<Long> usedSet = new HashSet<>(usedIds);

                // 4. Lấy toàn bộ groupId của exam hiện tại
                List<Integer> examGroupIds =
                        groupQuestionRepository.findGroupIdsByExamId(examId);

                Set<Integer> examGroupIdSet = new HashSet<>(examGroupIds);

                // 5. Filter group hợp lệ
                List<BankGroupQuestion> validGroups = bankGroups.stream()
                        // chưa dùng trong exam
                        .filter(bg -> !usedSet.contains(bg.getId()))
                        // không phải contribute từ chính exam này
                        .filter(bg -> bg.getSourceGroupId() == null
                                || !examGroupIdSet.contains(bg.getSourceGroupId()))
                        .toList();

                // ===== PHÂN BIỆT LỖI =====

                if (validGroups.isEmpty()) {

                        boolean allAlreadyUsed =
                                bankGroups.stream()
                                        .allMatch(bg -> usedSet.contains(bg.getId()));

                        boolean allFromThisExam =
                                bankGroups.stream()
                                        .allMatch(bg -> bg.getSourceGroupId() != null
                                                && examGroupIdSet.contains(bg.getSourceGroupId()));

                        if (allAlreadyUsed) {
                                throw new RuntimeException("All selected groups already exist in this exam");
                        }

                        if (allFromThisExam) {
                                throw new RuntimeException(
                                        "Các nhóm câu hỏi được đóng góp từ bài đề thi này không thể được sử dụng lại trong cùng một bài đề thi.");
                        }

                        throw new RuntimeException("No valid groups to add to exam");
                }

                // FIX: track max index theo part
                Map<String, Integer> partMaxIndexMap = new HashMap<>();

                // 6. Add groups
                for (BankGroupQuestion bg : validGroups) {

                        // Save group + MEDIA COPY
                        GroupQuestion examGroup = mapGroupFromBankWithMediaCopy(bg, exam);
                        groupQuestionRepository.save(examGroup);

                        // Images COPY
                        if (bg.getImages() != null && !bg.getImages().isEmpty()) {
                                Set<GroupQuestionImage> imgs = bg.getImages().stream()
                                        .map(i -> mapGroupImageFromBankWithCopy(i, examGroup))
                                        .collect(Collectors.toSet());
                                groupQuestionImageRepository.saveAll(imgs);
                                examGroup.setImages(imgs);
                        }

                        // Audios COPY
                        if (bg.getAudios() != null && !bg.getAudios().isEmpty()) {
                                Set<GroupQuestionAudio> audios = bg.getAudios().stream()
                                        .map(a -> mapGroupAudioFromBankWithCopy(a, examGroup))
                                        .collect(Collectors.toSet());
                                groupQuestionAudioRepository.saveAll(audios);
                                examGroup.setAudios(audios);
                        }

                        // Child questions
                        for (BankGroupChildQuestion bq : bg.getQuestions()) {

                                int indexNumber =
                                        calculateNextIndex(exam, bg.getPart(), partMaxIndexMap);

                                ToeicQuestion q =
                                        bankMapper.mapToeicQuestionFromBank(bq, examGroup, exam, indexNumber);

                                toeicQuestionRepository.save(q);

                                examGroup.getQuestions().add(q);

                                if (bq.getOptions() != null && !bq.getOptions().isEmpty()) {
                                        Set<ToeicQuestionOption> ops = bq.getOptions().stream()
                                                .map(o -> bankMapper.mapOptionFromBank(o, q))
                                                .collect(Collectors.toSet());
                                        toeicQuestionOptionRepository.saveAll(ops);
                                        q.setOptions(ops);
                                }
                        }

                        // Set title + range
                        List<ToeicQuestion> qs = examGroup.getQuestions();

                        int min = qs.stream().mapToInt(ToeicQuestion::getIndexNumber).min().orElse(0);
                        int max = qs.stream().mapToInt(ToeicQuestion::getIndexNumber).max().orElse(0);

                        examGroup.setQuestionRange(min + "-" + max);
                        examGroup.setTitle("Questions " + min + " - " + max);

                        groupQuestionRepository.save(examGroup);
                }

                // 7. Response từ BANK
                return validGroups.stream()
                        .map(bg -> bankMapper.toGroupResponse(bg, bg.getQuestions()))
                        .toList();
        }
        /**
         * Map BankGroupQuestion to GroupQuestion (NO media - handled separately)
         */
        private GroupQuestion mapGroupFromBankWithMediaCopy(BankGroupQuestion bg, Exam exam) {
                GroupQuestion g = new GroupQuestion();

                g.setExam(exam);
                g.setBankGroupId(bg.getId());

                g.setPart(bg.getPart());
                g.setContent(bg.getContent());

                return g;
        }

        /**
         * Map BankGroupImage to GroupQuestionImage with COPY
         */
        private GroupQuestionImage mapGroupImageFromBankWithCopy(
                        BankGroupImage bi,
                        GroupQuestion group) {
                GroupQuestionImage img = new GroupQuestionImage();
                img.setGroup(group);
                String newKey = minIOMediaService.copyFile(bi.getImageKey());
                img.setUrl(newKey);
                return img;
        }

        /**
         * Map BankGroupAudio to GroupQuestionAudio with COPY
         */
        private GroupQuestionAudio mapGroupAudioFromBankWithCopy(
                        BankGroupAudio ba,
                        GroupQuestion group) {
                GroupQuestionAudio audio = new GroupQuestionAudio();
                audio.setGroup(group);
                String newKey = minIOMediaService.copyFile(ba.getAudioKey());
                audio.setUrl(newKey);
                return audio;
        }

        // ==========================
        // GET DETAIL
        // ==========================

        @Override
        public BankToeicQuestionResponse getSingleDetail(Integer id) {

                BankToeicQuestion q = bankToeicRepo.findWithImagesById(id)
                                .orElseThrow(() -> new RuntimeException("Question not found"));

                List<BankToeicOption> options = bankToeicOptionRepoitory.findByQuestionId(id);

                q.setOptions(options);

                return bankMapper.mapSingleToResponse(q);
        }

        @Override
        public BankGroupQuestionResponse getGroupDetail(Long id) {

                BankGroupQuestion g = bankGroupQuestionRepository.findGroupWithMedia(id)
                                .orElseThrow(() -> new RuntimeException("Group not found"));

                List<BankGroupChildQuestion> children = bankGroupChildQuestionRepository
                                .findChildrenWithOptionsByGroupId(id);

                return bankMapper.mapGroupToResponse(g, children);
        }

        // ==========================
        // GET ALL (PAGINATION)
        // ==========================

        @Override
        public PageResponse<?> getAllQuestionFromBank(int pageNo, int pageSize, String sortBy, boolean isGroup,
                        String[] search) {
                if (!isGroup) {
                        return getAllSingleQuestion(pageNo, pageSize, sortBy, search);
                } else {
                        return getAllGroupQuestion(pageNo, pageSize, sortBy, search);
                }
        }

        public PageResponse<?> getAllSingleQuestion(int pageNo, int pageSize, String sortBy, String[] search) {

                List<SearchCriteria> criteriaList = SearchCriteriaUtils.convert(search);
                SearchQueryCriteriaConsumer<BankToeicQuestion> consumer = new GenericSearchQueryCriteriaConsumer<>(null,
                                null, null);

                PageResponse<?> rawPage = genericSearchRepository.searchByCriteria(
                                BankToeicQuestion.class,
                                pageNo,
                                pageSize,
                                criteriaList,
                                sortBy,
                                consumer);

                List<BankToeicQuestion> questions = (List<BankToeicQuestion>) rawPage.getItems();

                List<BankToeicQuestionResponse> dtoList = bankMapper.toSingleQuestionDTOList(questions);

                return PageResponse.<List<BankToeicQuestionResponse>>builder()
                                .pageNo(rawPage.getPageNo())
                                .pageSize(rawPage.getPageSize())
                                .totalPage(rawPage.getTotalPage())
                                .items(dtoList)
                                .build();
        }

        public PageResponse<?> getAllGroupQuestion(int pageNo, int pageSize, String sortBy, String[] search) {

                List<SearchCriteria> criteriaList = SearchCriteriaUtils.convert(search);
                SearchQueryCriteriaConsumer<BankGroupQuestion> consumer = new GenericSearchQueryCriteriaConsumer<>(null,
                                null, null);

                PageResponse<?> rawPage = genericSearchRepository.searchByCriteria(
                                BankGroupQuestion.class,
                                pageNo,
                                pageSize,
                                criteriaList,
                                sortBy,
                                consumer);

                List<BankGroupQuestion> questions = (List<BankGroupQuestion>) rawPage.getItems();

                List<BankGroupQuestionResponse> dtoList = bankMapper.toGroupQuestionDTOList(questions);

                return PageResponse.<List<BankGroupQuestionResponse>>builder()
                                .pageNo(rawPage.getPageNo())
                                .pageSize(rawPage.getPageSize())
                                .totalPage(rawPage.getTotalPage())
                                .items(dtoList)
                                .build();
        }
}