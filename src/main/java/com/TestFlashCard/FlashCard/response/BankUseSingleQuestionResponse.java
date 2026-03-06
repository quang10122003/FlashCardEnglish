package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankUseSingleQuestionResponse {

    private Integer id;
    private String part;
    private String detail;
    private String result;
    private String clarify;

    private List<MediaFileResponse> images;
    private MediaFileResponse audio;

    private List<BankToeicOptionResponse> options;
}

