package com.TestFlashCard.FlashCard.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PageResponse<T>{

    int pageNo;
    int pageSize;
    int totalPage;
    T items;
}