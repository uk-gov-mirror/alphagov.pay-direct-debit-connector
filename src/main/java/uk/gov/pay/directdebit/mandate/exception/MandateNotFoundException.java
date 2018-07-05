package uk.gov.pay.directdebit.mandate.exception;


import uk.gov.pay.directdebit.common.exception.NotFoundException;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import static java.lang.String.format;

public class MandateNotFoundException extends NotFoundException {

    public MandateNotFoundException(String mandateId) {
        super(format("Couldn't find mandate with id: %s", mandateId));
    }

    public MandateNotFoundException(MandateExternalId mandateExternalId) {
        super(format("Couldn't find mandate with id: %s", mandateExternalId));
    }
}
