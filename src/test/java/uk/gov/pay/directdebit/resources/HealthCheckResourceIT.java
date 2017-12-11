package uk.gov.pay.directdebit.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardPortValue;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.Is.is;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class HealthCheckResourceIT {

    @DropwizardPortValue
    private int port;

    @Test
    public void healthcheck_shouldReturnHealthy() {
        given()
                .port(port)
                .contentType(JSON)
                .when()
                .accept(JSON)
                .get("/healthcheck")
                .then()
                .statusCode(OK.getStatusCode())
                .body("database.healthy", is(true))
                .body("ping.healthy", is(true));
    }
}
