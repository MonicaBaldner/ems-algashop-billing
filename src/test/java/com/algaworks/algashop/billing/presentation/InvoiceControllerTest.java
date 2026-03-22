package com.algaworks.algashop.billing.presentation;

import com.algaworks.algashop.billing.application.invoice.management.*;
import com.algaworks.algashop.billing.application.invoice.query.InvoiceOutput;
import com.algaworks.algashop.billing.application.invoice.query.InvoiceQueryService;
import com.algaworks.algashop.billing.application.invoice.query.PaymentSettingsOutput;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.invoice.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @Autowired
//    private ObjectMapper objectMapper;
private final ObjectMapper objectMapper = new ObjectMapper();

  //  @Autowired
   // private InvoiceRepository invoiceRepository;

    @MockitoBean
    private InvoiceQueryService invoiceQueryService;

    @MockitoBean
    private InvoiceManagementApplicationService applicationService;

    @Test
    void shouldReturnCreatedInvoiceWhenValidInput() throws Exception {
        GenerateInvoiceInput input = GenerateInvoiceInput.builder()
                .orderId("123")
                .customerId(UUID.fromString("6d0668cd-6b52-40d3-b7d0-23e3856dd0ec"))
                .paymentSettings(PaymentSettingsInput.builder()
                        .method(PaymentMethod.GATEWAY_BALANCE)
                        .build())
                .payer(PayerData.builder()
                        .fullName("John Doe")
                        .document("123456789")
                        .email("john@email.com")
                        .phone("999999999")
                        .address(AddressData.builder()
                                .street("Street")
                                .number("10")
                                .neighborhood("Neighborhood")
                                .city("City")
                                .state("State")
                                .zipCode("12345")
                                .build())
                        .build())
                .items(List.of(new LineItemInput("Item", BigDecimal.TEN)))
                .build();

        UUID invoiceId = UUID.randomUUID();

        // simula que o service retorna um ID válido
        Mockito.when(applicationService.generate(Mockito.any())).thenReturn(invoiceId);

        // simula que o query service retorna a fatura criada
        InvoiceOutput output = InvoiceOutput.builder()
                .id(UUID.randomUUID())
                .orderId("123")
                .customerId(UUID.fromString("6d0668cd-6b52-40d3-b7d0-23e3856dd0ec"))
                .issuedAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .totalAmount(BigDecimal.TEN)
                .status(InvoiceStatus.UNPAID)
                .build();

        Mockito.when(invoiceQueryService.findByOrderId("123")).thenReturn(output);

        mockMvc.perform(post("/api/v1/orders/123/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("123"))
                .andExpect(jsonPath("$.status").value("UNPAID"));
    }


    @Test
    void shouldReturnInvalidFieldsWhenCustomerIdIsNull() throws Exception {
        GenerateInvoiceInput input = GenerateInvoiceInput.builder()
                .paymentSettings(PaymentSettingsInput.builder()
                        .method(PaymentMethod.GATEWAY_BALANCE)
                        .build())
                .payer(PayerData.builder()
                        .fullName("John Doe")
                        .document("123456789")
                        .email("john@email.com")
                        .phone("999999999")
                        .address(AddressData.builder()
                                .street("Street")
                                .number("10")
                                .neighborhood("Neighborhood")
                                .city("City")
                                .state("State")
                                .zipCode("12345")
                                .build())
                        .build())
                .items(List.of(new LineItemInput("Item", BigDecimal.TEN)))
                .build();

        mockMvc.perform(post("/api/v1/orders/123/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid fields"))
                .andExpect(jsonPath("$.fields.customerId").exists());
    }

    @Test
    void shouldReturnInvoiceWhenOrderExists() throws Exception {
        // prepara domínio
        Invoice invoice = InvoiceTestDataBuilder.anInvoice()
                .orderId("123")
                .status(InvoiceStatus.UNPAID)
                .build();

        Payer payer = invoice.getPayer();

        Address address = payer.getAddress();

        AddressData addressData = AddressData.builder()
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .build();

        PayerData payerData = PayerData.builder()
                .fullName(payer.getFullName())
                .document(payer.getDocument())
                .email(payer.getEmail())
                .phone(payer.getPhone())
                .address(addressData) // agora é AddressData
                .build();

        PaymentSettingsOutput paymentSettingsOutput = new PaymentSettingsOutput(
                UUID.randomUUID(), // ou o id real do PaymentSettings, se existir
                invoice.getPaymentSettings() != null ? invoice.getPaymentSettings().getCreditCardId() : null,
                invoice.getPaymentSettings() != null ? invoice.getPaymentSettings().getMethod() : PaymentMethod.GATEWAY_BALANCE
        );

        // mapeia para o output esperado pelo controller
        InvoiceOutput output = InvoiceOutput.builder()
                .id(invoice.getId())
                .orderId("123")
                .customerId(invoice.getCustomerId())
                .issuedAt(invoice.getIssuedAt())
                .expiresAt(invoice.getExpiresAt())
                .totalAmount(invoice.getTotalAmount())
                .status(InvoiceStatus.UNPAID)
                .payer(payerData)
                .paymentSettings(paymentSettingsOutput)
                .build();

        Mockito.when(invoiceQueryService.findByOrderId("123")).thenReturn(output);

        mockMvc.perform(get("/api/v1/orders/123/invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("123"))
                .andExpect(jsonPath("$.status").value("UNPAID"));
    }


    @Test
    void shouldReturnNotFoundWhenCreditCardDoesNotBelongToCustomer() throws Exception {
        GenerateInvoiceInput input = GenerateInvoiceInput.builder()
                .customerId(UUID.randomUUID())
                .paymentSettings(PaymentSettingsInput.builder()
                        .method(PaymentMethod.CREDIT_CARD)
                        .creditCardId(UUID.randomUUID()) // cartão inexistente
                        .build())
                .payer(PayerData.builder()
                        .fullName("John Doe")
                        .document("123456789")
                        .email("john@email.com")
                        .phone("999999999")
                        .address(AddressData.builder()
                                .street("Street")
                                .number("10")
                                .neighborhood("Neighborhood")
                                .city("City")
                                .state("State")
                                .zipCode("12345")
                                .build())
                        .build())
                .items(List.of(new LineItemInput("Item", BigDecimal.TEN)))
                .build();

        // Configura o mock para lançar a exceção esperada
        Mockito.when(applicationService.generate(Mockito.any()))
                .thenThrow(new CreditCardNotFoundException("Credit card not found"));

        mockMvc.perform(post("/api/v1/orders/123/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Credit card")))
                .andExpect(jsonPath("$.type").value("/errors/not-found"));
    }
}
