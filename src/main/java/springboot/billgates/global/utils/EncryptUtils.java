package springboot.billgates.global.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springboot.billgates.global.exception.CustomException;
import springboot.billgates.global.exception.ErrorCode;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EncryptUtils {

    @Value("${encryption.secret}")
    private String secretKey;

    @Value("${encryption.iv}")
    private String iv;

    /**
     * [AES-256 암호화]
     * 평문을 받아 AES-256 알고리즘으로 암호화 한 뒤, Base64 문자열로 반환합니다.
     *
     * @param plainText 암호화할 원본 문자열 (ex. "user@email.com")
     * @return 암호화된 Base64 인코딩 문자열 (실패 시 RuntimeException 발생)
     * */
    public String encrypt(String plainText) {
        try {
            // null check
            if (plainText == null) return null;

            // 1. key & IV 객체 생성

            // SecretKeySpec : 바이트 배열 -> AES 알고리즘용 key 객체
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            // IvParameterSpec : 암호화 초기 블록 벡터값 설정
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            // 2. Cipher 인스턴스 생성. AES 알고리즘, CBC 모드, PKCS5 패딩 방식
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // 3. 암호화 모드로 초기화
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

            // 4. 암호화 수행. 문자열 -> 바이트배열 -> 암호화된 바이트 배열
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 5. 암호화된 바이트 배열 -> Base64로 변환하여 return
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ENCRYPTION_FAILED);
        }
    }

    /**
     * [AES-256 복호화]
     * 암호화된 Base64 문자열을 받아 평문으로 복호화합니다.
     *
     * @param cipherText 암호화된 문자열 (Base64 Encoded)
     * @return 복호화된 원본 문자열
     * */
    public String decrypt(String cipherText) {
        try {
            // null check
            if (cipherText == null) return null;

            // 1. key & IV 객체 생성 (위와 동일)
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            // 2. Cipher 인스턴스 생성. AES 알고리즘, CBC 모드, PKCS5 패딩 방식
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // 3. 복호화 모드로 초기화
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);

            // 4. Base64 문자열 -> 바이트 배열로 디코딩
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // 5. 암호화된 바이트 배열 -> 복호화된 바이트 배열
            byte[] decrypted = cipher.doFinal(decoded);

            // 6. 복호화된 바이트 배열 -> UTF-8 문자열 로 변환 후 return
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DECRYPTION_FAILED);
        }
    }

    /**
     * [이메일 마스킹]
     * 이메일 주소의 ID 부분을 일부 가림 처리합니다.
     * ex) testuser@gmail.com -> tes***@gmail.com
     *
     * @param email 원본 이메일 주소
     * @return 마스킹 처리된 이메일 (형식이 맞지 않으면 원본 반환)
     * */
    public String maskEmail(String email) {
        // null, empty check
        if (!StringUtils.hasText(email)) return email;

        // 정규식: (ID부분) @ (도메인부분) 그룹핑
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(email);

        if (matcher.find()) {
            String id = matcher.group(1);       // @ 앞부분 (ID)
            String domain = matcher.group(2);   // @ 뒷부분 (도메인)

            int length = id.length();
            String maskedId;

            // id 길이에 따른 마스킹 처리
            if (length < 3)
                // 3글자 미만 : 첫글자만 보여주고 뒤에 ***
                maskedId = id.charAt(0) + "***";
            else
                // 3글자 이상 : 앞 3글자 보여주고 뒤에 ***
                maskedId = id.substring(0, 3) + "***";
            return maskedId + "@" + domain;
        }
        return email; // input 이 이메일 형식이 아니면 그대로 return
    }
    /**
     * [전화번호 마스킹]
     * 전화번호의 가운데 자리를 가림 처리합니다.
     * ex) 010-1234-5678 -> 010-****-5678
     *
     * @param phoneNumber 원본 전화번호 (하이픈 포함 여부 상관없음)
     * @return 마스킹 처리된 전화번호
     */
    public String maskPhoneNumber(String phoneNumber) {
        // null, empty check
        if (!StringUtils.hasText(phoneNumber)) return phoneNumber;

        // 숫자만 남기고 모두 제거
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

        // 11자리 형식인 경우
        if (cleanNumber.length() == 11)
            return cleanNumber.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-****-$3");
        // 10자리 형식인 경우
        else if (cleanNumber.length() == 10)
            return cleanNumber.replaceAll("(\\d{3})(\\d{3})(\\d{4})", "$1-****-$3");

        return phoneNumber; // input 이 규격에 맞지 않으면 그대로 return
    }
}
