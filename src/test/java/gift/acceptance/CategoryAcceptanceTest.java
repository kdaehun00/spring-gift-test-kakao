package gift.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

class CategoryAcceptanceTest extends AcceptanceTestBase {

    @DisplayName("카테고리 목록을 조회한다")
    @Sql(scripts = {"/sql/cleanup.sql", "/sql/category-data.sql"})
    @Test
    void retrieveCategories() {
        // when & then: 카테고리 목록 조회
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", equalTo("식품"));
    }

    @DisplayName("카테고리를 생성하면 조회 시 포함된다")
    @Sql(scripts = {"/sql/cleanup.sql"})
    @Test
    void createCategory() {
        // when: 카테고리 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "식품"))
                .when()
                .post("/api/categories")
                .then()
                .statusCode(200)
                .body("name", equalTo("식품"));

        // then: 조회 시 생성한 카테고리가 포함된다
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("name", hasItem("식품"))
                .body("", hasSize(1));
    }
}
