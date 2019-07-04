package uk.gov.pay.directdebit.mandate.params;

import com.google.common.collect.Range;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.time.ZonedDateTime;
import java.util.Optional;

public class MandateSearchParams {

    private String reference;
    private MandateState mandateState;
    private MandateBankStatementReference mandateBankStatementReference;
    private String name;
    private String email;
    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;
    private int page;
    private int displaySize = 500;
    private String gatewayAccountExternalId;

    private MandateSearchParams() {
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    public Optional<MandateState> getMandateState() {
        return Optional.ofNullable(mandateState);
    }

    public Optional<MandateBankStatementReference> getMandateBankStatementReference() {
        return Optional.ofNullable(mandateBankStatementReference);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<ZonedDateTime> getFromDate() {
        return Optional.ofNullable(fromDate);
    }

    public Optional<ZonedDateTime> getToDate() {
        return Optional.ofNullable(toDate);
    }

    public int getPage() {
        return page;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public static MandateSearchParams aMandateSearchParams() {
        return new MandateSearchParams();
    }

    public MandateSearchParams withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public MandateSearchParams withMandateState(MandateState mandateState) {
        this.mandateState = mandateState;
        return this;
    }

    public MandateSearchParams withMandateBankStatementReference(MandateBankStatementReference mandateBankStatementReference) {
        this.mandateBankStatementReference = mandateBankStatementReference;
        return this;
    }

    public MandateSearchParams withName(String name) {
        this.name = name;
        return this;
    }

    public MandateSearchParams withEmail(String email) {
        this.email = email;
        return this;
    }

    public MandateSearchParams withFromDate(ZonedDateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public MandateSearchParams withToDate(ZonedDateTime toDate) {
        this.toDate = toDate;
        return this;
    }

    public MandateSearchParams withPage(Integer page) {
        if (page != null && page < 1) {
            throw new BadRequestException("Query param 'page' should be a non zero positive integer.");
        }
        this.page = Optional.ofNullable(page).orElse(0);
        return this;
    }

    public MandateSearchParams withDisplaySize(Integer displaySize) {
        int size = Optional.ofNullable(displaySize).orElse(500);
        if (!Range.closed(1, 500).contains(size)) {
            throw new BadRequestException("Query param 'display_size' should be between 1 and 500.");
        }
        this.displaySize = size;
        return this;
    }

    public MandateSearchParams withGatewayAccountId(String gatewayAccountId) {
        this.gatewayAccountExternalId = gatewayAccountId;
        return this;
    }
}
