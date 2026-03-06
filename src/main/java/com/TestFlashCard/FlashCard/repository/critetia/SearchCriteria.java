package com.TestFlashCard.FlashCard.repository.critetia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String key;       // field name (vd: "age", "name")
    private String operation; // toán tử (":", ">", "<")
    private Object value;     // giá trị (vd: "John", 20)
}
