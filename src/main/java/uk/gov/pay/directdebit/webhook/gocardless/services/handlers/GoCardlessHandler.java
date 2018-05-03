package uk.gov.pay.directdebit.webhook.gocardless.services.handlers;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.services.GoCardlessAction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class GoCardlessHandler implements GoCardlessActionHandler {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessHandler.class);

    protected TransactionService transactionService;
    PayerService payerService;
    GoCardlessService goCardlessService;

    protected abstract Map<GoCardlessAction, Function<Transaction, PaymentRequestEvent>> getHandledActions();

    protected abstract Optional<PaymentRequestEvent> process(GoCardlessEvent event);

    GoCardlessHandler(TransactionService transactionService,
                      PayerService payerService,
                      GoCardlessService goCardlessService) {
        this.transactionService = transactionService;
        this.payerService = payerService;
        this.goCardlessService = goCardlessService;
    }

    public void handle(GoCardlessEvent event) {
        process(event).ifPresent((paymentRequestEvent) -> {
            event.setPaymentRequestEventId(paymentRequestEvent.getId());
            LOGGER.info("handled gocardless event with id: {}, resource type: {}", event.getEventId(), event.getResourceType().toString());
        });
        goCardlessService.storeEvent(event);
    }
}
