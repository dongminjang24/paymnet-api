package com.payment.paymentapi.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.payment.common.dto.ConvertPaymentRequestDto;
import com.payment.common.response.CommonResponse;
import com.payment.paymentapi.config.TossPaymentConfig;
import com.payment.paymentapi.dto.PaymentDto;
import com.payment.paymentapi.dto.PaymentFailDto;
import com.payment.paymentapi.dto.PaymentSuccessDto;
import com.payment.paymentapi.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
	private final PaymentService paymentService;
	private final TossPaymentConfig tossPaymentConfig;

	@PostMapping("/toss")
	public ResponseEntity<?> requestTossPayment(
		@RequestBody @Valid PaymentDto paymentDto) {
		ConvertPaymentRequestDto paymentRequestDto = paymentService.requestPayment(paymentDto.toEntity(),
			paymentDto.getEmail()).toPaymentRequestDto();
		paymentRequestDto.setSuccessUrl(tossPaymentConfig.getSuccessUrl());
		paymentRequestDto.setFailUrl(tossPaymentConfig.getFailUrl());
		return ResponseEntity.ok(new CommonResponse<>(paymentRequestDto));
	}


	@GetMapping("/toss/success")
	public ResponseEntity<?> getPaymentSuccess(
		@RequestParam String orderId,
		@RequestParam String paymentKey,
		@RequestParam Long amount
		) {

		PaymentSuccessDto paymentSuccessDto = paymentService.paymentSuccess(paymentKey, orderId, amount);
		return ResponseEntity.ok(new CommonResponse<>(paymentSuccessDto));
	}

	@GetMapping("/toss/fail")
	public ResponseEntity<?> tossPaymentFail(
		@RequestParam String code,
		@RequestParam String message,
		@RequestParam String orderId) {
		paymentService.paymentFail(code,message, orderId);

		return ResponseEntity.ok(new CommonResponse<>(
			PaymentFailDto.builder()
				.errorCode(code)
				.errorMessage(message)
				.orderId(orderId)
				.build()
		));

	}

	@GetMapping("/toss/cancel/point")
	public ResponseEntity<?> tossPaymentCancelPoint(
		@RequestParam String email,
		@RequestParam String paymentKey,
		@RequestParam String cancelReason) {

		return ResponseEntity.ok().body(new CommonResponse<>(
			paymentService
				.cancelPaymentPoint(email,paymentKey,cancelReason)));
	}


	@GetMapping("/history")
	public ResponseEntity<?> tossPaymentAllHistory(
		@RequestParam String email,
		@PageableDefault(size = 10, sort = "paymentId", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(new CommonResponse<>(
			paymentService.findAllChargingHistories(email,pageable)
		));
	}



}