package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime; // 👈 시간 타입 import 필수!

@Table(name = "MEMBER")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 500)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 500)
    private String phoneNumber;

    // 👇 [추가] 방해금지(DND) 관련 필드 3대장
    @Column(name = "use_dnd")
    private Boolean useDnd;         // 방해금지 사용 여부 (true/false)

    @Column(name = "dnd_start_time")
    private LocalTime dndStartTime; // 시작 시간 (예: 22:00:00)

    @Column(name = "dnd_end_time")
    private LocalTime dndEndTime;   // 종료 시간 (예: 07:00:00)
}