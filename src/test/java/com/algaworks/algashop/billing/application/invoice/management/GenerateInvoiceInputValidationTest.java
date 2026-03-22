package com.algaworks.algashop.billing.application.invoice.management;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GenerateInvoiceInputValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailWhenCustomerIdIsNull() {
        GenerateInvoiceInput input = GenerateInvoiceInput.builder()
                .orderId("123")
                .paymentSettings(new PaymentSettingsInput())
                .payer(new PayerData("John Doe", "123456789", "john@email.com", "999999999", new AddressData("Street", "10", null, "Neighborhood", "City", "State", "12345")))
                .items(List.of(new LineItemInput("Item", BigDecimal.TEN)))
                .build();

        Set<ConstraintViolation<GenerateInvoiceInput>> violations = validator.validate(input);

        assertThat(violations)
                .anyMatch(v -> v.getMessageTemplate().equals("{jakarta.validation.constraints.NotNull.message}"));    }

}
