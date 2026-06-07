package com.sam.finance.sahamlog.dividend.dto;

import java.util.List;

public record DividendCalendarResponse(
    int year,
    int month,
    List<DividendCalendarItemResponse> items) {
}
