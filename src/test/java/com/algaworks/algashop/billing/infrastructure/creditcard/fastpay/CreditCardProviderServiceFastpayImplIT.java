package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import com.algaworks.algashop.billing.infrastructure.AbstractFastpayIT;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
class CreditCardProviderServiceFastpayImplIT extends AbstractFastpayIT {

    @BeforeAll
    public static void beforeAll() {
        startMock();
    }

    @AfterAll
    public static void afterAll() {
        stopMock();
    }

    @Test
    public void shouldRegisterCreditCard() {
        LimitedCreditCard limitedCreditCard = registerCard();
        Assertions.assertThat(limitedCreditCard.getGatewayCode()).isNotBlank();
    }

    @Test
    public void shouldFindRegisteredCreditCard() {
        LimitedCreditCard limitedCreditCard = registerCard();

        LimitedCreditCard limitedCreditCardFound = creditCardProvider.findById(limitedCreditCard.getGatewayCode()).orElseThrow();

        Assertions.assertThat(limitedCreditCardFound.getGatewayCode()).isEqualTo(limitedCreditCard.getGatewayCode());
    }

    @Test
    public void shouldDeleteRegisteredCreditCard() {
        LimitedCreditCard limitedCreditCard = registerCard();

        creditCardProvider.delete(limitedCreditCard.getGatewayCode());

      //  Optional<LimitedCreditCard> possibleCreditCard = creditCardProvider.findById(limitedCreditCard.getGatewayCode());
//duas linhas removidas na 07.13 de Cap 07 de Nivel 03
        //Assertions.assertThat(possibleCreditCard).isEmpty();
    }

}