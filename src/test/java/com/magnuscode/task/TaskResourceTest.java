package com.magnuscode.task;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class TaskResourceTest {

        @Test
        @TestSecurity(user = "user", roles = "user")
        @SuppressWarnings("all")
        void list() {
                given()
                                .body("{\"title\":\"to-be-listed\"}")
                                .contentType(ContentType.JSON)
                                .when().post("/api/v1/tasks").as(Task.class);
                given()
                                .when().get("/api/v1/tasks")
                                .then()
                                .statusCode(200)
                                .body("$",
                                                allOf(
                                                                hasItem(
                                                                                hasEntry("title", "to-be-listed")),
                                                                everyItem(
                                                                                hasEntry(
                                                                                                is("user"),
                                                                                                (Matcher) hasEntry(
                                                                                                                "name",
                                                                                                                "user"))))

                                );
        }

        @Test
        @TestSecurity(user = "user", roles = "user")
        void create() {
                given()
                                .body("{\"title\":\"task-create\"}")
                                .contentType(ContentType.JSON)
                                .when().post("/api/v1/tasks")
                                .then()
                                .statusCode(201)
                                .body(
                                                "title", is("task-create"),
                                                "created", not(emptyString()));
        }

        @Test
        @TestSecurity(user = "user", roles = "user")
        void update() {
                var toUpdate = given()
                                .body("{\"title\":\"to-update\"}")
                                .contentType(ContentType.JSON)
                                .post("/api/v1/tasks").as(Task.class);
                toUpdate.title = "updated";
                given()
                                .body(toUpdate)
                                .contentType(ContentType.JSON)
                                .when().put("/api/v1/tasks/{id}", toUpdate.id)
                                .then()
                                .statusCode(200)
                                .body(
                                                "title", is("updated"),
                                                "version", is(toUpdate.version + 1));
        }

        @Test
        @TestSecurity(user = "user", roles = "user")
        void updateNotFound() {
                given()
                                .body("{\"title\":\"updated\"}")
                                .contentType(ContentType.JSON)
                                .when().put("/api/v1/tasks/1337")
                                .then()
                                .statusCode(404);
        }

}