package com.payment.paymentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.payment.config.CommonDatabaseConfig;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.payment"})
@Import({CommonDatabaseConfig.class})
@ComponentScan(basePackages = { "com.payment"})
public class PaymentApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaymentApiApplication.class, args);
	}
}