package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * Created by jt on 2019-07-23.
 */
@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    private final PreAuthActions preAuthActions;

    private final AuthActions authActions;

    private final PaymentGuards paymentGuards;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                .action(preAuthActions.preAuthAction()).guard(paymentGuards.paymentIdGuard())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .action(preAuthActions.preAuthApproved())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .action(preAuthActions.preAuthDeclined());

        transitions.withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                .action(authActions.authAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
                .action(authActions.authApproved())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED)
                .action(authActions.authDeclined());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to: %s)", from, to));
            }
        };

        config.withConfiguration()
                .listener(adapter);
    }

}
