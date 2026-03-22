package com.algaworks.algashop.billing.application.invoice.management;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.InvoiceRepository;
import com.algaworks.algashop.billing.domain.model.invoice.InvoicingService;
import com.algaworks.algashop.billing.domain.model.invoice.PaymentMethod;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceManagementApplicationServiceTest {

    private final PaymentGatewayService paymentGatewayService = Mockito.mock(PaymentGatewayService.class);
    private final InvoicingService invoicingService = Mockito.mock(InvoicingService.class);
    private final InvoiceRepository invoiceRepository = Mockito.mock(InvoiceRepository.class);
    private final CreditCardRepository creditCardRepository = Mockito.mock(CreditCardRepository.class);

    private final InvoiceManagementApplicationService service =
            new InvoiceManagementApplicationService(paymentGatewayService, invoicingService, invoiceRepository, creditCardRepository);



    @Test
    void shouldThrowExceptionWhenCreditCardDoesNotBelongToCustomer() {
        UUID customerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        Mockito.when(creditCardRepository.existsByIdAndCustomerId(cardId, customerId))
                .thenReturn(false);

        GenerateInvoiceInput input = GenerateInvoiceInput.builder()
                .customerId(customerId)
                .paymentSettings(PaymentSettingsInput.builder()
                        .method(PaymentMethod.CREDIT_CARD)
                        .creditCardId(cardId)
                        .build())
                .payer(new PayerData("John Doe", "123456789", "john@email.com", "999999999",
                        new AddressData("Street", "10", null, "Neighborhood", "City", "State", "12345")))
                .items(List.of(new LineItemInput("Item", BigDecimal.TEN)))
                .build();

        assertThrows(CreditCardNotFoundException.class, () -> service.generate(input));
    }
}
