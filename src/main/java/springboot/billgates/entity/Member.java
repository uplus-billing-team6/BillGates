package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;

import lombok.Getter;
import lombok.NoArgsConstructor;


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
}