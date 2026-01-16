package springboot.billgates.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import springboot.billgates.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long>{

}
