package com.payment.paymentapi.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.payment.model.entity.Member;
import com.payment.repository.MemberRepository;

@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner initData(MemberRepository memberRepository) {
		return args -> {
			// 데이터베이스에 Member가 없는 경우에만 생성
			if (memberRepository.count() == 0) {
				Member member = Member.builder()
					.email("user@example.com")
					.name("Test User")
					.phoneNumber("010-1234-5678")
					.point(0L)
					.build();

				memberRepository.save(member);

				System.out.println("Test Member created: " + member.getEmail());
			}
		};
	}
}