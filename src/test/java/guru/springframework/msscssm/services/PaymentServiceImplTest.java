package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println("Should be NEW");
        System.out.println(savedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println("Should be PRE_AUTH or PRE_AUTH_ERROR");
        System.out.println(sm.getState().getId());

        System.out.println(preAuthedPayment);


        StateMachine<PaymentState, PaymentEvent> authorizedPayment = paymentService.authorizePayment(savedPayment.getId());
        System.out.println("should be AUTH");
        System.out.println(paymentRepository.getOne(savedPayment.getId()));
        System.out.println(authorizedPayment.getState().getId());
    }
    @Transactional
    @Test
    @RepeatedTest(10)
    void auth() {
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());


        if(sm.getState().getId().equals(PaymentState.PRE_AUTH)){
            StateMachine<PaymentState, PaymentEvent> authorizedPayment = paymentService.authorizePayment(savedPayment.getId());
            System.out.println("Should be AUTH OR AUTH ERROR");
            System.out.println(authorizedPayment.getState());
        }
        else{
            System.out.println("declined, should be PRE AUTH ERROR");
            System.out.println(paymentRepository.getOne(savedPayment.getId()));
        }


    }

}