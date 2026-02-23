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

class ProductAcceptanceTest extends AcceptanceTestBase {

    @DisplayName("상품 목록을 조회한다")
    @Sql(scripts = {"/sql/cleanup.sql", "/sql/product-data.sql"})
    @Test
    void retrieveProducts() {
        // when & then: 상품 목록 조회
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].name", equalTo("떡볶이"))
                .body("[0].price", equalTo(5000))
                .body("[0].category.name", equalTo("식품"));
    }

    @DisplayName("상품을 생성하면 조회 시 포함된다")
    @Sql(scripts = {"/sql/cleanup.sql", "/sql/category-data.sql"})
    @Test
    void createProduct() {
        // when: 상품 생성
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "떡볶이",
                        "price", 5000,
                        "imageUrl", "http://example.com/image.png",
                        "categoryId", 1
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .body("name", equalTo("떡볶이"))
                .body("price", equalTo(5000));

        // then: 조회 시 생성한 상품이 포함된다
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("name", hasItem("떡볶이"))
                .body("", hasSize(1));
    }

    @DisplayName("존재하지 않는 카테고리로 상품 생성 시 실패한다")
    @Sql(scripts = {"/sql/cleanup.sql"})
    @Test
    void createProductWithInvalidCategory() {
        // when & then: 존재하지 않는 카테고리로 상품 생성 요청 시 서버 에러 발생
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "떡볶이",
                        "price", 5000,
                        "imageUrl", "http://example.com/image.png",
                        "categoryId", 9999
                ))
                .when()
                .post("/api/products")
                .then()
                .statusCode(404);
    }
}
