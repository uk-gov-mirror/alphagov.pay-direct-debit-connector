package uk.gov.pay.directdebit.events.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.pay.directdebit.payments.links.PaginationLink;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;


public class DirectDebitEventsPagination {

    @JsonProperty("self") @Getter
    private PaginationLink selfLink;
    @JsonProperty("first_page") @Getter
    private PaginationLink firstLink;
    @JsonProperty("last_page") @Getter
    private PaginationLink lastLink;
    @JsonProperty("prev_page") @Getter
    private PaginationLink prevLink;
    @JsonProperty("next_page") @Getter
    private PaginationLink nextLink;

    public DirectDebitEventsPagination(DirectDebitEventSearchParams searchParams, int total, UriInfo uriInfo) {
        selfLink = PaginationLink.ofValue(uriWithParams(searchParams, uriInfo).toString());
        firstLink = PaginationLink.ofValue(uriWithParams(searchParams.copy().page(1).build(), uriInfo).toString());
        int lastPage = Math.round(total / searchParams.getPageSize());
        lastLink = PaginationLink.ofValue(uriWithParams(searchParams.copy().page(lastPage).build(), uriInfo).toString());
        Integer currentPage = searchParams.getPage();
        prevLink = currentPage == 1 ? null : 
                PaginationLink.ofValue(uriWithParams(searchParams.copy().page(currentPage - 1).build(), uriInfo).toString());
        nextLink = currentPage == lastPage ? null : 
                PaginationLink.ofValue(uriWithParams(searchParams.copy().page(currentPage + 1).build(), uriInfo).toString());
    }

    private URI uriWithParams(DirectDebitEventSearchParams params, UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(uriInfo.getPath());
        params.getParamsAsMap().forEach((k, v) -> uriBuilder.queryParam(k, v));
        return uriBuilder.build();
    }
}