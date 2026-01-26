package springboot.billgates.domain.message.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import springboot.billgates.domain.message.api.dto.MessageDto;
import springboot.billgates.domain.message.api.service.MessageService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<Response<Page<MessageDto>>> getMessages(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "channel", defaultValue = "ALL") String channel 
    ) {
        // 서비스에 channel 전달
        Page<MessageDto> messages = messageService.getMessages(page, size, channel);
        return ResponseEntity.ok(Response.success("메시지 조회 성공", messages));
    }
}








