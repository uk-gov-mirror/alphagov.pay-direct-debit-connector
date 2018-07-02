package uk.gov.pay.directdebit.events.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class DirectDebitEventsResponse {
    
    @JsonProperty("results")
    private final List<DirectDebitEvent> events;
    @JsonProperty
    private final Integer page;
    @JsonProperty
    private final int total;
    @JsonProperty
    private final int count;
    @JsonProperty("_links")
    private DirectDebitEventsPagination pagination;
    
    public DirectDebitEventsResponse(List<DirectDebitEvent> events, int page, int total, DirectDebitEventsPagination directDebitEventsPagination) {
        this.events = events;
        this.total = total;
        this.page = page;
        this.count = events.size();
        this.pagination = directDebitEventsPagination;
    }
}