package com.habibah.springapp.dto;

import com.habibah.springapp.entity.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
        Long id,
        String customerName,
        String customerEmail,
        String shippingAddress,
        PurchaseStatus status,
        LocalDateTime createdAt,
        BigDecimal total,
        List<DetailResponse> items
) {
}
