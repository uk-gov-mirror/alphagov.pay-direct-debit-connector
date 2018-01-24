package uk.gov.pay.directdebit.payments.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.ws.rs.core.Response;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.*;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGES_API_PATH;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;
import static uk.gov.pay.directdebit.util.NumberMatcher.isNumber;
import static uk.gov.pay.directdebit.util.ResponseContainsLinkMatcher.containsLink;


@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentRequestResourceIT {

    private static final String FRONTEND_CARD_DETAILS_URL = "/secure";
    private static final String JSON_AMOUNT_KEY = "amount";
    private static final String JSON_REFERENCE_KEY = "reference";
    private static final String JSON_DESCRIPTION_KEY = "description";
    private static final String JSON_GATEWAY_ACC_KEY = "gateway_account_id";
    private static final String JSON_RETURN_URL_KEY = "return_url";
    private static final String JSON_CHARGE_KEY = "charge_id";
    private static final String JSON_STATE_KEY = "state.status";
    private static final long AMOUNT = 6234L;
    private Gson gson = new Gson();

    @DropwizardTestContext
    private TestContext testContext;

    private GatewayAccountFixture testGatewayAccount;

    @Before
    public void setUp() {
        testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldCreateAPaymentRequest() throws Exception {
        String accountId = String.valueOf(testGatewayAccount.getId());
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
        String token = testContext.getDatabaseTestHelper().getTokenByPaymentRequestExternalId(externalPaymentRequestId);

        String hrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + token;
        String hrefNextUrlPost = "http://Frontend" + FRONTEND_CARD_DETAILS_URL;

        response.header("Location", is(documentLocation))
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", hrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", token);
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
                .body(JSON_STATE_KEY, is(PaymentState.NEW.toExternal().getState()))
                .body(JSON_RETURN_URL_KEY, is(returnUrl));


        // Reload the charge token which as it should have changed

        String newChargeToken = testContext.getDatabaseTestHelper().getTokenByPaymentRequestExternalId(externalPaymentRequestId);

        String newHrefNextUrl = "http://Frontend" + FRONTEND_CARD_DETAILS_URL + "/" + newChargeToken;

        getChargeResponse
                .body("links", hasSize(3))
                .body("links", containsLink("self", "GET", documentLocation))
                .body("links", containsLink("next_url", "GET", newHrefNextUrl))
                .body("links", containsLink("next_url_post", "POST", hrefNextUrlPost, "application/x-www-form-urlencoded", new HashMap<String, Object>() {{
                    put("chargeTokenId", newChargeToken);
                }}));

    }

    @Test
    public void shouldReturn400IfMandatoryFieldsMissing() {
        String accountId = String.valueOf(testGatewayAccount.getId());
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
        String accountId = String.valueOf(testGatewayAccount.getId());
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
                .body("message", is("The size of a field(s) is invalid: [description]"));
    }

    @Test
    public void shouldReturn400IfFieldsInvalid() {
        String accountId = String.valueOf(testGatewayAccount.getId());
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
        return "http://localhost:" + testContext.getPort() + CHARGE_API_PATH
                .replace("{accountId}", accountId)
                .replace("{paymentRequestExternalId}", chargeId);
    }

    private RequestSpecification givenSetup() {
        return given().port(testContext.getPort())
                .contentType(JSON);
    }
}
