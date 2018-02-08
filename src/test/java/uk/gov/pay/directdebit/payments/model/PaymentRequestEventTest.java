package uk.gov.pay.directdebit.payments.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.MANDATE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.PAYER;

public class PaymentRequestEventTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldGetPaymentEventFromString() throws UnsupportedPaymentRequestEventException {
        assertThat(PaymentRequestEvent.SupportedEvent.fromString("TOKEN_EXCHANGED"), is(TOKEN_EXCHANGED));
    }

    @Test
    public void shouldThrowExceptionIfUnknownEvent() throws UnsupportedPaymentRequestEventException {
        thrown.expect(Exception.class);
        thrown.expectMessage("Event \"blabla\" is not supported");
        thrown.reportMissingExceptionWithMessage("UnknownPaymentRequestEventException expected");
        PaymentRequestEvent.SupportedEvent.fromString("blabla");
    }

    @Test
    public void paidOut_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.paidOut(paymentRequestId);

        assertThat(event.getEvent(), is(PAID_OUT));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }

    @Test
    public void mandateCreated_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.mandateCreated(paymentRequestId);

        assertThat(event.getEvent(), is(MANDATE_CREATED));
        assertThat(event.getEventType(), is(MANDATE));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }

    @Test
    public void directDebitDetailsConfirmed_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.directDebitDetailsConfirmed(paymentRequestId);

        assertThat(event.getEvent(), is(DIRECT_DEBIT_DETAILS_CONFIRMED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }

    @Test
    public void payerCreated_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.payerCreated(paymentRequestId);

        assertThat(event.getEvent(), is(PAYER_CREATED));
        assertThat(event.getEventType(), is(PAYER));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }

    @Test
    public void directDebitDetailsReceived_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.directDebitDetailsReceived(paymentRequestId);

        assertThat(event.getEvent(), is(DIRECT_DEBIT_DETAILS_RECEIVED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }

    @Test
    public void tokenExchanged_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.tokenExchanged(paymentRequestId);

        assertThat(event.getEvent(), is(TOKEN_EXCHANGED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }

    @Test
    public void chargeCreated_shouldReturnExpectedEvent() {

        long paymentRequestId = 1L;
        PaymentRequestEvent event = PaymentRequestEvent.chargeCreated(paymentRequestId);

        assertThat(event.getEvent(), is(CHARGE_CREATED));
        assertThat(event.getEventType(), is(CHARGE));
        assertThat(event.getPaymentRequestId(), is(paymentRequestId));
    }
}
