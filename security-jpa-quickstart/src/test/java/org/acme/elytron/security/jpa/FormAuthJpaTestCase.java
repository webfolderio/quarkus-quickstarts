package org.acme.elytron.security.jpa;

import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;

@Testcontainers
@QuarkusTest
public class FormAuthJpaTestCase {

	@Container
	@SuppressWarnings({ "rawtypes", "deprecation" })
    public static final PostgreSQLContainer DATABASE = new PostgreSQLContainer<>()
            .withDatabaseName("elytron_security_jpa")
            .withUsername("quarkus")
            .withPassword("quarkus")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd ->
                    cmd
                            .withHostName("localhost")
                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(5432), new ExposedPort(5432)))
            );

    @Test
    public void testFormBasedAuthSuccess() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        CookieFilter cookies = new CookieFilter();

        RestAssured
                .given()
                .filter(cookies)
                .redirects().follow(false)
                .when()
                .formParam("j_username", "user")
                .formParam("j_password", "user")
                .post("/j_security_check")
                .then()
                .assertThat()
                .statusCode(302)
                .header("location", containsString("/index.html"));
    }

    @Test
    public void testFormBasedAuthInvalidUser() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        CookieFilter cookies = new CookieFilter();

        RestAssured
                .given()
                .filter(cookies)
                .redirects().follow(false)
                .when()
                .formParam("j_username", "foo")
                .formParam("j_password", "bar")
                .post("/j_security_check")
                .then()
                .assertThat()
                .statusCode(403);
    }
}
