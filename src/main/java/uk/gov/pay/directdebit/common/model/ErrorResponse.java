package uk.gov.pay.directdebit.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ErrorResponse {
    
    @JsonProperty("error_identifier")
    private final ErrorIdentifier identifier;
    
    @JsonProperty("message")
    private final List<String> messages;
    
    public ErrorResponse(ErrorIdentifier identifier, List<String> messages) {
        this.identifier = identifier;
        this.messages = messages;
    }

    public ErrorResponse(ErrorIdentifier identifier, String message) {
        this(identifier, List.of(message));
    }

    public ErrorIdentifier getIdentifier() {
        return identifier;
    }

    public List<String> getMessages() {
        return messages;
    }
}
