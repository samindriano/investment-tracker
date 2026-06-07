package com.sam.finance.sahamlog.shared.dto;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableFactory {

    private PageableFactory() {
    }

    public static Pageable create(int page, int size, List<String> sortParams, List<Sort.Order> defaultOrders) {
        return PageRequest.of(page, size, resolveSort(sortParams, defaultOrders));
    }

    private static Sort resolveSort(List<String> sortParams, List<Sort.Order> defaultOrders) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by(defaultOrders);
        }

        List<Sort.Order> orders = sortParams.stream()
            .map(PageableFactory::parseOrder)
            .filter(order -> order != null)
            .toList();

        return orders.isEmpty() ? Sort.by(defaultOrders) : Sort.by(orders);
    }

    private static Sort.Order parseOrder(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return null;
        }

        String[] parts = sortParam.split(",", 2);
        String property = parts[0].trim();
        if (property.isEmpty()) {
            return null;
        }

        Sort.Direction direction = parts.length < 2
            ? Sort.Direction.ASC
            : Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.ASC);

        return new Sort.Order(direction, property);
    }
}
