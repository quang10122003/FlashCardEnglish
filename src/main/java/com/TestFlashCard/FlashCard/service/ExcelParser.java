package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.TestFlashCard.FlashCard.entity.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExcelParser {

    @Autowired
    private MinIO_MediaService minIO_MediaService;

    // Parts that should be grouped
    private static final Set<String> GROUP_PARTS = Set.of("3", "4", "6", "7");

    // Parts that are single questions
    private static final Set<String> SINGLE_PARTS = Set.of("1", "2", "5");

    /**
     * Parse result containing both single questions and group questions
     */
    public static class ParseResult {
        private List<ToeicQuestion> singleQuestions = new ArrayList<>();
        private List<GroupQuestion> groupQuestions = new ArrayList<>();

        public List<ToeicQuestion> getSingleQuestions() {
            return singleQuestions;
        }

        public List<GroupQuestion> getGroupQuestions() {
            return groupQuestions;
        }
    }

    /**
     * Temporary holder for raw question data from Excel
     */
    private static class RawQuestionData {
        int rowIndex;
        String part;
        String detail;
        String result;
        String imageFileName;
        String audioFileName;
        String optionA;
        String optionB;
        String optionC;
        String optionD;
        String conversation;
        String clarify;
        String content; // New column L - passage/conversation content for group
    }

    /**
     * Main parse method - returns both single questions and group questions
     */
    public ParseResult parseQuestionsWithGroups(File excelFile, File mediaFolder, Exam exam) throws IOException {
        ParseResult result = new ParseResult();

        // Step 1: Read all raw data from Excel
        List<RawQuestionData> allRawData = readExcelData(excelFile);

        // Step 2: Separate by Part type
        List<RawQuestionData> singlePartRawData = allRawData.stream()
                .filter(r -> SINGLE_PARTS.contains(r.part))
                .collect(Collectors.toList());

        List<RawQuestionData> groupPartRawData = allRawData.stream()
                .filter(r -> GROUP_PARTS.contains(r.part))
                .collect(Collectors.toList());

        // Step 3: From group parts, separate:
        // - Conversation có giá trị → group questions
        // - Conversation trống → single questions
        List<RawQuestionData> groupPartWithConversation = groupPartRawData.stream()
                .filter(r -> r.conversation != null && !r.conversation.isBlank())
                .collect(Collectors.toList());

        List<RawQuestionData> groupPartWithoutConversation = groupPartRawData.stream()
                .filter(r -> r.conversation == null || r.conversation.isBlank())
                .collect(Collectors.toList());

        // Step 4: Combine single questions
        // = Part 1,2,5 + Part 3,4,6,7 với conversation trống
        List<RawQuestionData> allSingleRawData = new ArrayList<>();
        allSingleRawData.addAll(singlePartRawData);
        allSingleRawData.addAll(groupPartWithoutConversation);

        // Step 5: Process single questions
        result.singleQuestions = processSingleQuestions(allSingleRawData, mediaFolder, exam);

        // Step 6: Process group questions (Part 3,4,6,7 với conversation có giá trị)
        result.groupQuestions = processGroupQuestions(groupPartWithConversation, mediaFolder, exam);

        return result;
    }

    /**
     * Legacy method for backward compatibility
     * 
     * @deprecated Use parseQuestionsWithGroups instead
     */
    @Deprecated
    public List<ToeicQuestion> parseQuestions(File excelFile, File mediaFolder) {
        List<ToeicQuestion> questions = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int indexQuestion = 1;
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header

                ToeicQuestion question = new ToeicQuestion();
                question.setIndexNumber(indexQuestion);
                question.setPart(getCellValue(row.getCell(0)));
                question.setDetail(getCellValue(row.getCell(1)));
                question.setResult(getCellValue(row.getCell(2)));
                question.setConversation(getCellValue(row.getCell(9)));
                question.setClarify(getCellValue(row.getCell(10)));

                indexQuestion++;

                // Upload image
                String imageFileName = getCellValue(row.getCell(3));
                if (imageFileName != null && !imageFileName.isBlank()) {
                    File imageFile = new File(mediaFolder, imageFileName);
                    if (!imageFile.exists()) {
                        throw new FileNotFoundException("Image file not found: " + imageFile.getPath());
                    }
                    String imageUrl = minIO_MediaService.uploadFile(imageFile);
                    ToeicQuestionImage img = new ToeicQuestionImage();
                    img.setUrl(imageUrl);
                    img.setToeicQuestion(question);
                    if (question.getImages() == null) {
                        question.setImages(new ArrayList<>());
                    }
                    question.getImages().add(img);
                }

                // Upload audio
                String audioFileName = getCellValue(row.getCell(4));
                if (audioFileName != null && !audioFileName.isBlank()) {
                    File audioFile = new File(mediaFolder, audioFileName);
                    if (audioFile.exists()) {
                        String audioUrl = minIO_MediaService.uploadFile(audioFile);
                        question.setAudio(audioUrl);
                    } else {
                        throw new FileNotFoundException("Audio file not found: " + audioFile.getPath());
                    }
                }

                // Options
                List<ToeicQuestionOption> options = new ArrayList<>();
                for (int i = 5; i <= 8; i++) {
                    String detail = getCellValue(row.getCell(i));
                    if (detail != null && !detail.trim().isEmpty()) {
                        ToeicQuestionOption option = new ToeicQuestionOption();
                        option.setDetail(detail);
                        option.setToeicQuestion(question);
                        if (i == 5)
                            option.setMark("A");
                        else if (i == 6)
                            option.setMark("B");
                        else if (i == 7)
                            option.setMark("C");
                        else if (i == 8)
                            option.setMark("D");
                        options.add(option);
                    }
                }
                question.setOptions(new HashSet<>(options));
                questions.add(question);
            }

        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file Excel", e);
        }

        return questions;
    }

    /**
     * Read all raw data from Excel file
     */
    private List<RawQuestionData> readExcelData(File excelFile) throws IOException {
        List<RawQuestionData> rawDataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header

                RawQuestionData data = new RawQuestionData();
                data.rowIndex = rowIndex++;
                data.part = getCellValue(row.getCell(0));
                data.detail = getCellValue(row.getCell(1));
                data.result = getCellValue(row.getCell(2));
                data.imageFileName = getCellValue(row.getCell(3));
                data.audioFileName = getCellValue(row.getCell(4));
                data.optionA = getCellValue(row.getCell(5));
                data.optionB = getCellValue(row.getCell(6));
                data.optionC = getCellValue(row.getCell(7));
                data.optionD = getCellValue(row.getCell(8));
                data.conversation = getCellValue(row.getCell(9));
                data.clarify = getCellValue(row.getCell(10));

                // Column L (index 11) - Group Question content
                // Only first question of each group has content
                data.content = getCellValue(row.getCell(11));

                // Debug log for content column
                if (data.content != null && !data.content.isBlank()) {
                    System.out.println(
                            "[ExcelParser] Row " + row.getRowNum() + " - Part: " + data.part + " - Content found: "
                                    + data.content.substring(0, Math.min(50, data.content.length())) + "...");
                }

                // Skip empty rows
                if (data.part == null || data.part.isBlank())
                    continue;

                rawDataList.add(data);
            }
        }

        return rawDataList;
    }

    /**
     * Process single questions
     * - Part 1, 2, 5: always single
     * - Part 3, 4, 6, 7: single when conversation is empty
     */
    private List<ToeicQuestion> processSingleQuestions(
            List<RawQuestionData> rawDataList,
            File mediaFolder,
            Exam exam) throws IOException {

        List<ToeicQuestion> questions = new ArrayList<>();

        // Index counters for each part
        Map<String, Integer> partIndexCounters = new HashMap<>();
        partIndexCounters.put("1", 1); // Part 1: 1-6
        partIndexCounters.put("2", 7); // Part 2: 7-31
        partIndexCounters.put("3", 32); // Part 3: 32-70
        partIndexCounters.put("4", 71); // Part 4: 71-100
        partIndexCounters.put("5", 101); // Part 5: 101-130
        partIndexCounters.put("6", 131); // Part 6: 131-146
        partIndexCounters.put("7", 147); // Part 7: 147-200

        for (RawQuestionData data : rawDataList) {
            ToeicQuestion question = createToeicQuestion(data, mediaFolder, exam, null);

            // Set index number based on part
            int currentIndex = partIndexCounters.getOrDefault(data.part, 1);
            question.setIndexNumber(currentIndex);
            partIndexCounters.put(data.part, currentIndex + 1);

            questions.add(question);
        }

        return questions;
    }

    /**
     * Process group questions (Part 3, 4, 6, 7)
     * Groups questions by 'conversation' field
     * Only processes questions that HAVE conversation value
     */
    private List<GroupQuestion> processGroupQuestions(
            List<RawQuestionData> rawDataList,
            File mediaFolder,
            Exam exam) throws IOException {

        List<GroupQuestion> groupQuestions = new ArrayList<>();

        // All questions here should have conversation value (already filtered)
        // Group raw data by conversation value
        // Use LinkedHashMap to preserve order
        Map<String, List<RawQuestionData>> groupedByConversation = new LinkedHashMap<>();

        for (RawQuestionData data : rawDataList) {
            String groupKey = data.conversation;
            // Skip if no conversation (should not happen, but safety check)
            if (groupKey == null || groupKey.isBlank()) {
                continue;
            }

            groupedByConversation
                    .computeIfAbsent(groupKey, k -> new ArrayList<>())
                    .add(data);
        }

        // Index counters for each part
        Map<String, Integer> partIndexCounters = new HashMap<>();
        partIndexCounters.put("3", 32); // Part 3: 32-70
        partIndexCounters.put("4", 71); // Part 4: 71-100
        partIndexCounters.put("6", 131); // Part 6: 131-146
        partIndexCounters.put("7", 147); // Part 7: 147-200

        // Process each group
        for (Map.Entry<String, List<RawQuestionData>> entry : groupedByConversation.entrySet()) {
            List<RawQuestionData> groupData = entry.getValue();
            if (groupData.isEmpty())
                continue;

            // Get first question data for group metadata
            RawQuestionData firstData = groupData.get(0);
            String part = firstData.part;

            // Create GroupQuestion
            GroupQuestion group = new GroupQuestion();
            group.setPart(part);
            group.setExam(exam);

            // Set content from group questions
            // Find first non-empty content from any question in group
            String groupContent = null;
            for (RawQuestionData data : groupData) {
                if (data.content != null && !data.content.isBlank()) {
                    groupContent = data.content;
                    break;
                }
            }

            if (groupContent != null) {
                group.setContent(groupContent);
            }

            // Calculate question range
            int startIndex = partIndexCounters.getOrDefault(part, 1);
            int endIndex = startIndex + groupData.size() - 1;
            group.setQuestionRange(startIndex + "-" + endIndex);

            // Set title (optional - can be customized)
            group.setTitle("Questions " + startIndex + "-" + endIndex);

            // Upload group media from FIRST question
            // Group Images
            if (firstData.imageFileName != null && !firstData.imageFileName.isBlank()) {
                File imageFile = new File(mediaFolder, firstData.imageFileName);
                if (imageFile.exists()) {
                    String imageKey = minIO_MediaService.uploadFile(imageFile);
                    GroupQuestionImage groupImage = new GroupQuestionImage();
                    groupImage.setUrl(imageKey);
                    groupImage.setGroup(group);
                    if (group.getImages() == null) {
                        group.setImages(new HashSet<>());
                    }
                    group.getImages().add(groupImage);
                }
            }

            // Group Audio
            if (firstData.audioFileName != null && !firstData.audioFileName.isBlank()) {
                File audioFile = new File(mediaFolder, firstData.audioFileName);
                if (audioFile.exists()) {
                    String audioKey = minIO_MediaService.uploadFile(audioFile);
                    GroupQuestionAudio groupAudio = new GroupQuestionAudio();
                    groupAudio.setUrl(audioKey);
                    groupAudio.setGroup(group);
                    if (group.getAudios() == null) {
                        group.setAudios(new HashSet<>());
                    }
                    group.getAudios().add(groupAudio);
                }
            }

            // Create child questions
            List<ToeicQuestion> childQuestions = new ArrayList<>();
            int questionIndex = startIndex;

            for (int i = 0; i < groupData.size(); i++) {
                RawQuestionData data = groupData.get(i);

                // For child questions, skip media upload (already in group)
                // But if it's not the first question and has its own media, we might want to
                // include it
                ToeicQuestion question = createToeicQuestionForGroup(
                        data,
                        mediaFolder,
                        exam,
                        group,
                        i > 0 // skipGroupMedia for non-first questions
                );
                question.setIndexNumber(questionIndex++);

                childQuestions.add(question);
            }

            group.setQuestions(childQuestions);

            // Update index counter for next group
            partIndexCounters.put(part, questionIndex);

            groupQuestions.add(group);
        }

        return groupQuestions;
    }

    /**
     * Create a ToeicQuestion entity from raw data (for single questions)
     */
    private ToeicQuestion createToeicQuestion(
            RawQuestionData data,
            File mediaFolder,
            Exam exam,
            GroupQuestion group) throws IOException {

        ToeicQuestion question = new ToeicQuestion();
        question.setPart(data.part);
        question.setDetail(data.detail);
        question.setResult(data.result);
        question.setConversation(data.conversation);
        question.setClarify(data.clarify);
        question.setExam(exam);
        question.setGroup(group);

        // Upload image
        if (data.imageFileName != null && !data.imageFileName.isBlank()) {
            File imageFile = new File(mediaFolder, data.imageFileName);
            if (!imageFile.exists()) {
                throw new FileNotFoundException("Image file not found: " + imageFile.getPath());
            }
            String imageKey = minIO_MediaService.uploadFile(imageFile);

            ToeicQuestionImage img = new ToeicQuestionImage();
            img.setUrl(imageKey);
            img.setToeicQuestion(question);

            if (question.getImages() == null) {
                question.setImages(new ArrayList<>());
            }
            question.getImages().add(img);
        }

        // Upload audio
        if (data.audioFileName != null && !data.audioFileName.isBlank()) {
            File audioFile = new File(mediaFolder, data.audioFileName);
            if (!audioFile.exists()) {
                throw new FileNotFoundException("Audio file not found: " + audioFile.getPath());
            }
            String audioKey = minIO_MediaService.uploadFile(audioFile);
            question.setAudio(audioKey);
        }

        // Create options
        Set<ToeicQuestionOption> options = new HashSet<>();

        if (data.optionA != null && !data.optionA.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("A");
            opt.setDetail(data.optionA);
            opt.setToeicQuestion(question);
            options.add(opt);
        }
        if (data.optionB != null && !data.optionB.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("B");
            opt.setDetail(data.optionB);
            opt.setToeicQuestion(question);
            options.add(opt);
        }
        if (data.optionC != null && !data.optionC.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("C");
            opt.setDetail(data.optionC);
            opt.setToeicQuestion(question);
            options.add(opt);
        }
        if (data.optionD != null && !data.optionD.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("D");
            opt.setDetail(data.optionD);
            opt.setToeicQuestion(question);
            options.add(opt);
        }

        question.setOptions(options);

        return question;
    }

    /**
     * Create a ToeicQuestion entity for group (child question)
     * Media handling is different - group media is already uploaded
     */
    private ToeicQuestion createToeicQuestionForGroup(
            RawQuestionData data,
            File mediaFolder,
            Exam exam,
            GroupQuestion group,
            boolean skipGroupMedia) throws IOException {

        ToeicQuestion question = new ToeicQuestion();
        question.setPart(data.part);
        question.setDetail(data.detail);
        question.setResult(data.result);
        question.setConversation(data.conversation);
        question.setClarify(data.clarify);
        question.setExam(exam);
        question.setGroup(group);

        // For child questions in group:
        // - First question's media goes to Group (already handled)
        // - Other questions may have their own specific media (e.g., individual images
        // for Part 7)

        if (!skipGroupMedia) {
            // This is the first question - media already uploaded to group
            // Initialize empty lists
            question.setImages(new ArrayList<>());
        } else {
            // Non-first questions - check if they have their own media
            if (data.imageFileName != null && !data.imageFileName.isBlank()) {
                File imageFile = new File(mediaFolder, data.imageFileName);
                if (imageFile.exists()) {
                    String imageKey = minIO_MediaService.uploadFile(imageFile);
                    ToeicQuestionImage img = new ToeicQuestionImage();
                    img.setUrl(imageKey);
                    img.setToeicQuestion(question);
                    question.setImages(new ArrayList<>());
                    question.getImages().add(img);
                }
            }

            if (data.audioFileName != null && !data.audioFileName.isBlank()) {
                File audioFile = new File(mediaFolder, data.audioFileName);
                if (audioFile.exists()) {
                    String audioKey = minIO_MediaService.uploadFile(audioFile);
                    question.setAudio(audioKey);
                }
            }
        }

        // Create options
        Set<ToeicQuestionOption> options = new HashSet<>();

        if (data.optionA != null && !data.optionA.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("A");
            opt.setDetail(data.optionA);
            opt.setToeicQuestion(question);
            options.add(opt);
        }
        if (data.optionB != null && !data.optionB.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("B");
            opt.setDetail(data.optionB);
            opt.setToeicQuestion(question);
            options.add(opt);
        }
        if (data.optionC != null && !data.optionC.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("C");
            opt.setDetail(data.optionC);
            opt.setToeicQuestion(question);
            options.add(opt);
        }
        if (data.optionD != null && !data.optionD.trim().isEmpty()) {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setMark("D");
            opt.setDetail(data.optionD);
            opt.setToeicQuestion(question);
            options.add(opt);
        }

        question.setOptions(options);

        return question;
    }

    private String getCellValue(Cell cell) {
        if (cell == null)
            return null;

        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case FORMULA -> {
                    // Handle formula cells - get cached value
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e) {
                        try {
                            yield String.valueOf((int) cell.getNumericCellValue());
                        } catch (Exception e2) {
                            yield null;
                        }
                    }
                }
                case BLANK -> null;
                default -> null;
            };
        } catch (Exception e) {
            System.err.println("Error reading cell value at row " + cell.getRowIndex() + ", col "
                    + cell.getColumnIndex() + ": " + e.getMessage());
            return null;
        }
    }
}