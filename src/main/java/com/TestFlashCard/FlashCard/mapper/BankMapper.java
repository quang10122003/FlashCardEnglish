package com.TestFlashCard.FlashCard.mapper;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.response.*;
import com.TestFlashCard.FlashCard.service.MediaService;
import com.TestFlashCard.FlashCard.service.MinIO_MediaService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BankMapper {

    private final MinIO_MediaService minIO_MediaService;
    private final MediaService mediaService;

    public BankMapper(MinIO_MediaService minIO_MediaService, MediaService mediaService) {
        this.minIO_MediaService = minIO_MediaService;
        this.mediaService = mediaService;
    }

    public BankToeicQuestion mapToeicToBank(ToeicQuestion q) {

        BankToeicQuestion b = new BankToeicQuestion();
        b.setPart(q.getPart());
        b.setDetail(q.getDetail());
        b.setResult(q.getResult());
        b.setClarify(q.getClarify());
        b.setAudio(q.getAudio()); // KEY
        b.setIsPublic(true);
        b.setSourceToeicId(q.getId());

        // OPTIONS
        List<BankToeicOption> options = q.getOptions().stream().map(o -> {
            BankToeicOption x = new BankToeicOption();
            x.setMark(o.getMark());
            x.setDetail(o.getDetail());
            x.setQuestion(b);
            return x;
        }).toList();

        // IMAGES (url = key)
        List<BankImage> images = q.getImages().stream().map(i -> {
            BankImage x = new BankImage();
            x.setUrl(i.getUrl());
            x.setQuestion(b);
            return x;
        }).toList();

        b.setOptions(options);
        b.setImages(images);

        return b;
    }

    public BankToeicQuestionResponse mapToResponse(BankToeicQuestion q) {

        // ===== OPTIONS =====
        List<BankToeicQuestionResponse.OptionResponse> options =
                q.getOptions().stream()
                        .map(o -> new BankToeicQuestionResponse.OptionResponse(
                                o.getMark(),
                                o.getDetail()
                        ))
                        .toList();

        // ===== IMAGES =====
        List<String> imageKeys = q.getImages().stream()
                .map(BankImage::getUrl)
                .toList();

        List<String> imageUrls = imageKeys.stream()
                .map(k -> minIO_MediaService.getPresignedURL(k, Duration.ofDays(1)))
                .toList();

        // ===== AUDIO =====
        String audioKey = q.getAudio();
        String audioUrl = audioKey != null
                ? minIO_MediaService.getPresignedURL(audioKey, Duration.ofDays(1))
                : null;

        return new BankToeicQuestionResponse(
                q.getId(),
                q.getPart(),
                q.getDetail(),
                q.getResult(),
                q.getClarify(),
                imageUrls,
                imageKeys,
                audioUrl,
                audioKey,
                options
        );
    }
public BankGroupQuestion mapGroupToBank(
        GroupQuestion g,
        User contributor,
        List<ToeicQuestion> questions
) {

    BankGroupQuestion bg = new BankGroupQuestion();
    bg.setPart(g.getPart());
    bg.setContent(g.getContent());
    bg.setContributor(contributor);
    bg.setSourceGroupId(g.getId());

    // ===== images =====
    Set<BankGroupImage> imgs = g.getImages().stream()
            .map(i -> {
                BankGroupImage x = new BankGroupImage();
                x.setImageKey(i.getUrl());
                x.setGroup(bg);
                return x;
            })
            .collect(Collectors.toSet());
    bg.setImages(imgs);

    // ===== audios =====
    Set<BankGroupAudio> auds = g.getAudios().stream()
            .map(a -> {
                BankGroupAudio x = new BankGroupAudio();
                x.setAudioKey(a.getUrl());
                x.setGroup(bg);
                return x;
            })
            .collect(Collectors.toSet());
    bg.setAudios(auds);

    // ===== child questions =====
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


    // ===== BANK GROUP → RESPONSE =====
    public BankGroupQuestionResponse mapGroupToResponse(BankGroupQuestion bg) {

        BankGroupQuestionResponse dto = new BankGroupQuestionResponse();
        dto.setId(bg.getId());
        dto.setPart(bg.getPart());
        dto.setContent(bg.getContent());
        dto.setSourceGroupId(bg.getSourceGroupId());

        dto.setImages(
                bg.getImages().stream()
                        .map(i -> minIO_MediaService.getPresignedURL(i.getImageKey(), Duration.ofDays(1)))
                        .toList()
        );

        dto.setAudios(
                bg.getAudios().stream()
                        .map(a -> minIO_MediaService.getPresignedURL(a.getAudioKey(), Duration.ofDays(1)))
                        .toList()
        );

        dto.setQuestions(
                bg.getQuestions().stream().map(q -> {
                    BankGroupChildQuestionResponse c = new BankGroupChildQuestionResponse();
                    c.setId(q.getId());
                    c.setIndexNumber(q.getIndexNumber());
                    c.setDetail(q.getDetail());
                    c.setResult(q.getResult());
                    c.setClarify(q.getClarify());

                    c.setOptions(
                            q.getOptions().stream()
                                    .map(o -> new BankToeicOptionResponse(o.getMark(), o.getDetail()))
                                    .toList()
                    );
                    return c;
                }).toList()
        );

        return dto;
    }

    // ===== SINGLE =====
    public BankUseSingleQuestionResponse toSingleResponse(BankToeicQuestion q) {

        BankUseSingleQuestionResponse res = new BankUseSingleQuestionResponse();

        res.setId(q.getId());
        res.setPart(q.getPart());
        res.setDetail(q.getDetail());
        res.setResult(q.getResult());
        res.setClarify(q.getClarify());

        res.setImages(
                q.getImages().stream()
                        .map(i -> new MediaFileResponse(minIO_MediaService.getPresignedURL(i.getUrl(), Duration.ofDays(1)),i.getUrl()))
                        .toList()
        );

        if (q.getAudio() != null) {
            res.setAudio(new MediaFileResponse(
                    minIO_MediaService.getPresignedURL(q.getAudio(), Duration.ofDays(1)),q.getAudio()
            ));
        }

        res.setOptions(
                q.getOptions().stream()
                        .map(o -> new BankToeicOptionResponse(o.getMark(), o.getDetail()))
                        .toList()
        );

        return res;
    }

    public BankUseGroupQuestionResponse toGroupResponse(
            BankGroupQuestion g,
            List<BankGroupChildQuestion> children
    ) {

        BankUseGroupQuestionResponse res = new BankUseGroupQuestionResponse();

        res.setId(g.getId());
        res.setPart(g.getPart());
        res.setContent(g.getContent());

        res.setImages(
                g.getImages().stream()
                        .map(i -> new MediaFileResponse(minIO_MediaService.getPresignedURL(i.getImageKey(), Duration.ofDays(1)),i.getImageKey()))
                        .toList()
        );

        res.setAudios(
                g.getAudios().stream()
                        .map(a -> new MediaFileResponse(minIO_MediaService.getPresignedURL(a.getAudioKey(), Duration.ofDays(1)),a.getAudioKey()))
                        .toList()
        );

        res.setQuestions(
                children.stream()
                        .map(c -> new BankUseGroupQuestionResponse.ChildQuestion(
                                c.getId(),
                                c.getIndexNumber(),
                                c.getDetail(),
                                c.getResult(),
                                c.getClarify(),
                                c.getOptions().stream()
                                        .map(o -> new BankToeicOptionResponse(o.getMark(), o.getDetail()))
                                        .toList()
                        ))
                        .toList()
        );

        return res;
    }



    public BankToeicQuestionResponse mapSingleToResponse(BankToeicQuestion q) {

        List<String> imageKeys = q.getImages().stream()
                .map(BankImage::getUrl)
                .toList();

        List<String> imageUrls = imageKeys.stream()
                .map(k -> minIO_MediaService.getPresignedURL(k, Duration.ofDays(1)))
                .toList();

        String audioKey = q.getAudio();
        String audioUrl = audioKey == null ? null :
                minIO_MediaService.getPresignedURL(audioKey, Duration.ofDays(1));

        return new BankToeicQuestionResponse(
                q.getId(),
                q.getPart(),
                q.getDetail(),
                q.getResult(),
                q.getClarify(),
                imageUrls,
                imageKeys,
                audioUrl,
                audioKey,
                q.getOptions().stream()
                        .map(o -> new BankToeicQuestionResponse.OptionResponse(
                                o.getMark(), o.getDetail()
                        ))
                        .toList()
        );
    }

    public List<BankToeicQuestionResponse> toSingleQuestionDTOList(List<BankToeicQuestion> entities) {
        return entities.stream()
                .map(this::mapSingleToResponse)
                .collect(Collectors.toList());
    }

    public BankGroupQuestionResponse mapGroupToResponse(
            BankGroupQuestion g,
            List<BankGroupChildQuestion> children
    ) {

        BankGroupQuestionResponse dto = new BankGroupQuestionResponse();

        dto.setId(g.getId());
        dto.setPart(g.getPart());
        dto.setContent(g.getContent());
        dto.setSourceGroupId(g.getSourceGroupId());

        dto.setImages(
                g.getImages().stream()
                        .map(i -> minIO_MediaService.getPresignedURL(i.getImageKey(), Duration.ofDays(1)))
                        .toList()
        );

        dto.setAudios(
                g.getAudios().stream()
                        .map(a -> minIO_MediaService.getPresignedURL(a.getAudioKey(), Duration.ofDays(1)))
                        .toList()
        );

        dto.setQuestions(
                children.stream().map(c -> {
                    BankGroupChildQuestionResponse cr = new BankGroupChildQuestionResponse();
                    cr.setId(c.getId());
                    cr.setIndexNumber(c.getIndexNumber());
                    cr.setDetail(c.getDetail());
                    cr.setResult(c.getResult());
                    cr.setClarify(c.getClarify());
                    cr.setOptions(
                            c.getOptions().stream()
                                    .map(o -> new BankToeicOptionResponse(o.getMark(), o.getDetail()))
                                    .toList()
                    );
                    return cr;
                }).toList()
        );

        return dto;
    }

    public List<BankGroupQuestionResponse> toGroupQuestionDTOList(List<BankGroupQuestion> entities) {
        return entities.stream()
                .map(this::mapGroupToResponse)
                .collect(Collectors.toList());
    }


    public ToeicQuestion mapToeicQuestionFromBank(
            BankToeicQuestion bq, Exam exam, int indexNumber
    ) {

        ToeicQuestion q = new ToeicQuestion();

        q.setExam(exam);
        q.setGroup(null);

        q.setPart(bq.getPart());
        q.setDetail(bq.getDetail());
        q.setResult(bq.getResult());
        q.setClarify(bq.getClarify());
        q.setAudio(bq.getAudio());
        q.setBankQuestionId(bq.getId());
        q.setIndexNumber(indexNumber);

        return q;
    }
    public ToeicQuestion mapToeicQuestionFromBank(
            BankGroupChildQuestion bq,
            GroupQuestion examGroup,
            Exam exam,
            int indexNumber
    ) {

        ToeicQuestion q = new ToeicQuestion();

        q.setExam(exam);
        q.setGroup(examGroup);

        q.setPart(examGroup.getPart());   // part của group
        q.setDetail(bq.getDetail());
        q.setResult(bq.getResult());
        q.setClarify(bq.getClarify());
        q.setIndexNumber(indexNumber);

        return q;
    }
    public ToeicQuestionOption mapOptionFromBank(BankToeicOption bo, ToeicQuestion q) {

        ToeicQuestionOption o = new ToeicQuestionOption();

        o.setToeicQuestion(q);
        o.setDetail(bo.getDetail());
        o.setMark(bo.getMark()); // A/B/C/D

        return o;
    }
    public ToeicQuestionImage mapImageFromBank(BankImage bi, ToeicQuestion q) {
        ToeicQuestionImage img = new ToeicQuestionImage();
        img.setToeicQuestion(q);
        img.setUrl(bi.getUrl());
        return img;
    }

    public GroupQuestion mapGroupFromBank(BankGroupQuestion bg, Exam exam) {

        GroupQuestion g = new GroupQuestion();

        g.setExam(exam);
        g.setBankGroupId(bg.getId());

        g.setPart(bg.getPart());
        g.setContent(bg.getContent());


//        g.setTitle(null);
//        g.setQuestionRange(null);

        return g;
    }
    public GroupQuestionImage mapGroupImageFromBank(
            BankGroupImage bi,
            GroupQuestion group
    ) {
        GroupQuestionImage img = new GroupQuestionImage();
        img.setGroup(group);

        // nếu bạn lưu bằng key → đổi thành url theo rule project
        img.setUrl(bi.getImageKey());

        return img;
    }
    public GroupQuestionAudio mapGroupAudioFromBank(
            BankGroupAudio ba,
            GroupQuestion group
    ) {
        GroupQuestionAudio audio = new GroupQuestionAudio();
        audio.setGroup(group);

        audio.setUrl(ba.getAudioKey());

        return audio;
    }

}
