package uk.gov.pay.directdebit.payments.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import uk.gov.pay.directdebit.IntegrationTestsSuite;
import uk.gov.pay.directdebit.infra.IntegrationTest;

import javax.ws.rs.core.Response;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGES_API_PATH;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;

public class PaymentRequestResourceIT extends IntegrationTest {
    private static final String FRONTEND_CARD_DETAILS_URL = "/secure";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_GATEWAY_ACC_KEY = "gateway_account_id";
    private static final String JSON_RETURN_URL_KEY = "return_url";
    private static final String JSON_CHARGE_KEY = "charge_id";
    private static final long AMOUNT = 6234L;
    private static final String accountId = "20";

    private Gson gson = new Gson();

    @Test
    public void shouldCreateAPaymentRequest() throws Exception {

        String expectedReference = "Test reference";
        String expectedDescription = "Test description";
        String returnUrl = "http://service.url/success-page/";
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, expectedReference)
                .put(JSON_DESCRIPTION_KEY, expectedDescription)
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, returnUrl)
                .build());

        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        ValidatableResponse response = givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body(JSON_CHARGE_KEY, is(notNullValue()))
                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
                .body(JSON_REFERENCE_KEY, is(expectedReference))
                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
                .body(JSON_RETURN_URL_KEY, is(returnUrl))
                .contentType(JSON);

        String externalPaymentRequestId = response.extract().path(JSON_CHARGE_KEY).toString();
        String documentLocation = expectedPaymentRequestLocationFor(accountId, externalPaymentRequestId);
        String chargeTokenId = databaseTestHelper.getTokenByPaymentRequestExternalId(externalPaymentRequestId);

        String hrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + chargeTokenId;
        String hrefNextUrlPost = "http://Frontend" + FRONTEND_CARD_DETAILS_URL;

        response.header("Location", is(documentLocation))
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", chargeTokenId);
                }}));
        String requestPath2 = CHARGE_API_PATH
                .replace("{accountId}", accountId)
                .replace("{paymentRequestExternalId}", externalPaymentRequestId);


        ValidatableResponse getChargeResponse = givenSetup()
                .get(requestPath2)
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(JSON)
                .body(JSON_CHARGE_KEY, is(externalPaymentRequestId))
                .body(JSON_AMOUNT_KEY, isNumber(AMOUNT))
                .body(JSON_REFERENCE_KEY, is(expectedReference))
                .body(JSON_DESCRIPTION_KEY, is(expectedDescription))
                .body(JSON_RETURN_URL_KEY, is(returnUrl));


        // Reload the charge token which as it should have changed

        String newChargeTokenId = databaseTestHelper.getTokenByPaymentRequestExternalId(externalPaymentRequestId);

        String newHrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + newChargeTokenId;

        getChargeResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", newHrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", newChargeTokenId);
                }}));

    }

    @Test
    public void shouldReturn400IfMandatoryFieldsMissing() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_DESCRIPTION_KEY, "desc")
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) missing: [reference]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalidSize() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, AMOUNT)
                .put(JSON_REFERENCE_KEY, "reference")
                .put(JSON_DESCRIPTION_KEY, RandomStringUtils.randomAlphabetic(256))
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) are too big: [description]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalid() {
        String postBody = gson.toJson(ImmutableMap.builder()
                .put(JSON_AMOUNT_KEY, 10000001)
                .put(JSON_REFERENCE_KEY, "reference")
                .put(JSON_DESCRIPTION_KEY, "desc")
                .put(JSON_GATEWAY_ACC_KEY, accountId)
                .put(JSON_RETURN_URL_KEY, "http://service.url/success-page/")
                .build());
        String requestPath = CHARGES_API_PATH
                .replace("{accountId}", accountId);

        givenSetup()
                .body(postBody)
                .post(requestPath)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(JSON)
                .body("message", is("Field(s) are invalid: [amount]"));
    }

    private String expectedPaymentRequestLocationFor(String accountId, String chargeId) {
        return "http://localhost:" + IntegrationTestsSuite.env().getPort() + CHARGE_API_PATH
                .replace("{accountId}", accountId)
                .replace("{paymentRequestExternalId}", chargeId);
    }
}
