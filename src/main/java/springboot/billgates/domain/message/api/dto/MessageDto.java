package springboot.billgates.domain.message.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

	// 🔹 messageId가 너무 길어서 프론트에서 messageId를 중간에 반올림하여 받아옴. -> 문자열로 붙여 전달하도록 수정함.
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;
    private Long memberId;
    private Long billingId;
    private String channel;
    private String status;
    private Long templateCode;   

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservedAt;
}
