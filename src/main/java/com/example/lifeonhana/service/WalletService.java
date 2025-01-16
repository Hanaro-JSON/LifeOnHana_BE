package com.example.lifeonhana.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.WalletDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.WalletRepository;

@Service
public class WalletService {
	private final WalletRepository walletRepository;
	private final JwtService jwtService;
	private final UserRepository userRepository;

	public WalletService(WalletRepository walletRepository, JwtService jwtService, UserRepository userRepository) {
		this.walletRepository = walletRepository;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	private Long getUserIdFromToken(String token) {
		String accessToken = token.replace("Bearer ", "");
		return jwtService.extractUserId(accessToken);
	}

	public WalletDTO getUserWallet(String token) {
		Long userId = getUserIdFromToken(token);
		Wallet wallet = walletRepository.findWalletIdByUserUserId(userId);
		if (wallet == null) {
			throw new NotFoundException("하나지갑 내역을 찾을 수 없습니다.");
		}

		return WalletDTO.builder()
			.walletId(wallet.getWalletId())
			.walletAmount(wallet.getWalletAmount())
			.paymentDay(String.valueOf(wallet.getPaymentDay()))
			.startDate(String.valueOf(wallet.getStartDate()))
			.endDate(String.valueOf(wallet.getEndDate()))
			.build();
	}

	//1일 15일 예외처리 어떻게 할건지 정하기
	public WalletDTO creatWallet(WalletDTO wallet, String token) {
		Long userId = getUserIdFromToken(token);
		User user = userRepository.getUserByUserId(userId);
		if (walletRepository.findWalletIdByUserUserId(userId) != null) {
			throw new BadRequestException("이미 하나지갑 정보가 존재합니다.");
		}
		Wallet newWallet = new Wallet();
		newWallet.setUser(user);
		newWallet.setWalletAmount(wallet.getWalletAmount());
		newWallet.setPaymentDay(Wallet.PaymentDay.fromValue(wallet.getPaymentDay()));
		System.out.println("wallet.getStartDate() = " + wallet.getStartDate());
		System.out.println("wallet.getEndDate() = " + wallet.getEndDate());
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			int paymentDay = Integer.parseInt(wallet.getPaymentDay());

			YearMonth startYearMonth = YearMonth.parse(wallet.getStartDate(), formatter);
			YearMonth endYearMonth = YearMonth.parse(wallet.getEndDate(), formatter);

			LocalDate startDate = startYearMonth.atDay(Math.min(paymentDay, startYearMonth.lengthOfMonth()));
			LocalDate endDate = endYearMonth.atDay(Math.min(paymentDay, endYearMonth.lengthOfMonth()));

			newWallet.setStartDate(startDate.atStartOfDay());
			newWallet.setEndDate(endDate.atStartOfDay());
		} catch (DateTimeParseException | NumberFormatException e) {
			throw new IllegalArgumentException("날짜 형식이 잘못되었습니다. 'yyyy-MM' 형식이어야 합니다.", e);
		}

		walletRepository.save(newWallet);

		return WalletDTO.builder()
			.walletId(newWallet.getWalletId())
			.walletAmount(newWallet.getWalletAmount())
			.paymentDay(newWallet.getPaymentDay().toString())
			.startDate(newWallet.getStartDate().toString())
			.endDate(newWallet.getEndDate().toString())
			.build();
	}
}
