    package com.TestFlashCard.FlashCard.response;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.Setter;

    import java.util.List;

    @Getter
    @Setter
    @AllArgsConstructor
    public class CardChoiceRespoinse {
        private String terminology;
        private List<String> hint;
        private String cardOptionOne;
        private String cardOptionTwo;
        private String cardOptionThree;
        private String cardOptionFour;
        private String answer;
    }
