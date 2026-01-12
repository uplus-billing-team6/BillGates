package springboot.billgates.global.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import springboot.billgates.global.exception.CustomException;
import springboot.billgates.global.exception.ErrorCode;

import static org.assertj.core.api.Assertions.*;

class EncryptUtilsTest {

    private EncryptUtils encryptUtils;

    @BeforeEach
    void setUp() {
        encryptUtils = new EncryptUtils();

        // 테스트용 키 32bytes
        String testSecret = "12345678901234567890123456789012";
        // 테스트용 iv 16bytes
        String testIv = "1234567890123456";

        ReflectionTestUtils.setField(encryptUtils, "secretKey", testSecret);
        ReflectionTestUtils.setField(encryptUtils, "iv", testIv);
    }

    @Test
    @DisplayName("암호화하고 복호화하면 원본과 같아야 한다")
    void encrypt_decrypt_success() {
        // given
        String originalText = "010-1234-5678";

        // when
        String encrypted = encryptUtils.encrypt(originalText);
        String decrypted = encryptUtils.decrypt(encrypted);

        // then
        System.out.println("Original : " + originalText);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);

        assertThat(encrypted).isNotEqualTo(originalText); // 암호화된 건 원본이랑 달라야 함
        assertThat(decrypted).isEqualTo(originalText);    // 복호화된 건 원본이랑 같아야 함
    }

    @Test
    @DisplayName("복호화 실패 시 CustomException이 발생해야 한다")
    void decrypt_fail_exception() {
        // given
        String invalidText = "NotBase64String!!";

        // when & then
        assertThatThrownBy(() -> encryptUtils.decrypt(invalidText))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECRYPTION_FAILED);
    }

    @Test
    @DisplayName("이메일 마스킹이 정상 동작해야 한다")
    void maskEmail() {
        // Case 1: 아이디가 3글자 이상
        assertThat(encryptUtils.maskEmail("testuser@gmail.com"))
            .isEqualTo("tes***@gmail.com");

        // Case 2: 아이디가 3글자 미만
        assertThat(encryptUtils.maskEmail("ab@naver.com"))
            .isEqualTo("a***@naver.com");

        // Case 3: 이메일 형식이 아님
        assertThat(encryptUtils.maskEmail("not-email"))
            .isEqualTo("not-email");
    }

    @Test
    @DisplayName("전화번호 마스킹이 정상 동작해야 한다")
    void maskPhoneNumber() {
        // Case 1: 11자리 (정상)
        assertThat(encryptUtils.maskPhoneNumber("010-1234-5678"))
            .isEqualTo("010-****-5678");
        assertThat(encryptUtils.maskPhoneNumber("01012345678"))
            .isEqualTo("010-****-5678");

        // Case 2: 10자리 (옛날 번호)
        assertThat(encryptUtils.maskPhoneNumber("011-123-4567"))
            .isEqualTo("011-****-4567");

        // Case 3: 이상한 번호
        assertThat(encryptUtils.maskPhoneNumber("123"))
            .isEqualTo("123");
    }
}