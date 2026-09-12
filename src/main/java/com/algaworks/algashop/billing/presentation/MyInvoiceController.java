package com.algaworks.algashop.billing.presentation;

import com.algaworks.algashop.billing.application.invoice.query.InvoiceOutput;
import com.algaworks.algashop.billing.application.invoice.query.InvoiceQueryService;
import com.algaworks.algashop.billing.application.security.SecurityChecks;
import com.algaworks.algashop.billing.infrastructure.security.SecurityAnnotations;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/me/orders/{orderId}/invoice")
@RequiredArgsConstructor
public class MyInvoiceController {

    private final InvoiceQueryService invoiceQueryService;
    private final SecurityChecks securityChecks;

    @GetMapping
    @SecurityAnnotations.CanReadMyInvoices
    public InvoiceOutput findByOrder(@PathVariable String orderId) {
        return invoiceQueryService.findByOrderIdAndCustomerId(orderId, securityChecks.getAuthenticatedUserId());
    }

}

