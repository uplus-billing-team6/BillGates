package springboot.billgates.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {
	
	private final MemberRepository memberRepository;
	
	@Transactional(readOnly = true)
	public long getMemberCount() {
		return memberRepository.count();
	}
}
