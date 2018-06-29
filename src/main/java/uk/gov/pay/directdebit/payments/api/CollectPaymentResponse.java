package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class CollectPaymentResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks = new ArrayList<>();

    @JsonProperty("charge_id")
    private String transactionExternalId;

    @JsonProperty
    private Long amount;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private ExternalPaymentState state;

    public CollectPaymentResponse(String transactionExternalId, ExternalPaymentState state, Long amount, String description, String reference, String createdDate, List<Map<String, Object>> dataLinks) {
        this.transactionExternalId = transactionExternalId;
        this.state = state;
        this.dataLinks = dataLinks;
        this.amount = amount;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
    }

    public List<Map<String, Object>> getDataLinks() {
        return dataLinks;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public URI getLink(String rel) {
        return dataLinks.stream()
                .filter(map -> rel.equals(map.get("rel")))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .get();
    }

    
    public static CollectPaymentResponse from(Transaction transaction, List<Map<String, Object>> dataLinks) {
        return new CollectPaymentResponse(
                transaction.getExternalId(),
                transaction.getState().toExternal(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getReference(),
                transaction.getCreatedDate().toString(),
                dataLinks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectPaymentResponse that = (CollectPaymentResponse) o;

        if (dataLinks != null ? !dataLinks.equals(that.dataLinks) : that.dataLinks != null) return false;
        if (!transactionExternalId.equals(that.transactionExternalId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (!createdDate.equals(that.createdDate)) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = dataLinks != null ? dataLinks.hashCode() : 0;
        result = 31 * result + transactionExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CollectPaymentResponse{" +
                "dataLinks=" + dataLinks +
                ", transactionExternalId='" + transactionExternalId + '\'' +
                ", state='" + state.getState() + '\'' +
                ", amount=" + amount +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

}

