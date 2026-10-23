package com.habibah.springapp.controller;

import com.habibah.springapp.dto.PurchaseRequest;
import com.habibah.springapp.dto.PurchaseResponse;
import com.habibah.springapp.entity.enums.PurchaseStatus;
import com.habibah.springapp.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody PurchaseRequest request, Authentication authentication) {
        return purchaseService.create(request, authentication.getName());
    }

    @GetMapping("/me")
    public List<PurchaseResponse> myPurchases(Authentication authentication) {
        return purchaseService.findByUser(authentication.getName());
    }

    @GetMapping
    public List<PurchaseResponse> purchases() {
        return purchaseService.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseResponse get(@PathVariable Long id) {
        return purchaseService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public PurchaseResponse updateStatus(@PathVariable Long id, @RequestParam PurchaseStatus status) {
        return purchaseService.updateStatus(id, status);
    }
}
