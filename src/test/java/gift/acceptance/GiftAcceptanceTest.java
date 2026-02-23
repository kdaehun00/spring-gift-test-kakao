package gift.acceptance;

import gift.model.Option;
import gift.model.OptionRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Sql(scripts = {"/sql/cleanup.sql", "/sql/gift-data.sql"})
class GiftAcceptanceTest extends AcceptanceTestBase {

    @Autowired
    private OptionRepository optionRepository;

    @DisplayName("선물을 보내면 옵션 재고가 감소한다")
    @Test
    void giftDecreasesOptionQuantity() {
        // when: 옵션 1(재고 10)으로 수량 3 선물 전송
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1L)
                .body(Map.of(
                        "optionId", 1L,
                        "quantity", 3,
                        "receiverId", 2L,
                        "message", "선물입니다"
                ))
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(200);

        // then: 재고가 10에서 7로 감소
        Option updated = optionRepository.findById(1L).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(7);
    }

    @DisplayName("재고 부족 시 선물 전송이 실패한다")
    @Test
    void giftFailsWhenInsufficientStock() {
        // when & then: 옵션 2(재고 2)에 수량 5 요청 시 실패
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1L)
                .body(Map.of(
                        "optionId", 2L,
                        "quantity", 5,
                        "receiverId", 2L,
                        "message", "선물입니다"
                ))
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(400);

        // then: 재고가 변경되지 않음
        Option unchanged = optionRepository.findById(2L).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(2);
    }

    @DisplayName("존재하지 않는 옵션으로 선물 전송 시 실패한다")
    @Test
    void giftFailsWhenOptionNotFound() {
        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1L)
                .body(Map.of(
                        "optionId", 9999L,
                        "quantity", 1,
                        "receiverId", 2L,
                        "message", "선물입니다"
                ))
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(404);
    }

    @DisplayName("Member-Id 헤더 누락 시 선물 전송이 실패한다")
    @Test
    void giftFailsWhenMemberIdHeaderMissing() {
        // when & then: Member-Id 헤더 없이 요청
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "optionId", 1L,
                        "quantity", 1,
                        "receiverId", 2L,
                        "message", "선물입니다"
                ))
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @DisplayName("Member-Id에 숫자가 아닌 값을 보내면 실패한다")
    @Test
    void giftFailsWhenMemberIdNotNumeric() {
        // when & then: Member-Id에 문자열 전송
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", "abc")
                .body(Map.of(
                        "optionId", 1L,
                        "quantity", 1,
                        "receiverId", 2L,
                        "message", "선물입니다"
                ))
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @DisplayName("잘못된 형식의 요청 본문을 보내면 실패한다")
    @Test
    void giftFailsWhenRequestBodyInvalid() {
        // when & then: 잘못된 JSON 전송
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1L)
                .body("invalid json")
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @DisplayName("존재하지 않는 Member-Id로 선물 전송 시 실패한다")
    @Test
    void giftFailsWhenMemberNotFound() {
        // when & then: 존재하지 않는 회원 ID로 요청
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 9999L)
                .body(Map.of(
                        "optionId", 1L,
                        "quantity", 1,
                        "receiverId", 2L,
                        "message", "선물입니다"
                ))
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(404)
                .body("code", equalTo("MEMBER_NOT_FOUND"));
    }
}
