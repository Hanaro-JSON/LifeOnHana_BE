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
			throw new NotFoundException("하나지갑이 존재하지 않습니다.");
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
		return setWallet(wallet, newWallet);
	}

	public WalletDTO updateWallet(WalletDTO walletDTO, String token) {
		Long userId = getUserIdFromToken(token);
		Wallet wallet = walletRepository.findWalletIdByUserUserId(userId);
		if (wallet == null) {
			throw new NotFoundException("하나지갑이 존재하지 않습니다.");
		}
		return setWallet(walletDTO, wallet);

	}

	private WalletDTO setWallet(WalletDTO walletDTO, Wallet wallet) {
		wallet.setWalletAmount(walletDTO.getWalletAmount());
		wallet.setPaymentDay(Wallet.PaymentDay.fromValue(walletDTO.getPaymentDay()));
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			int paymentDay = Integer.parseInt(walletDTO.getPaymentDay());

			YearMonth startYearMonth = YearMonth.parse(walletDTO.getStartDate(), formatter);
			YearMonth endYearMonth = YearMonth.parse(walletDTO.getEndDate(), formatter);

			LocalDate startDate = startYearMonth.atDay(Math.min(paymentDay, startYearMonth.lengthOfMonth()));
			LocalDate endDate = endYearMonth.atDay(Math.min(paymentDay, endYearMonth.lengthOfMonth()));

			wallet.setStartDate(startDate.atStartOfDay());
			wallet.setEndDate(endDate.atStartOfDay());
		} catch (DateTimeParseException | NumberFormatException e) {
			throw new IllegalArgumentException("날짜 형식이 잘못되었습니다. 'yyyy-MM' 형식이어야 합니다.", e);
		}
		walletRepository.save(wallet);

		return WalletDTO.builder()
			.walletId(wallet.getWalletId())
			.walletAmount(wallet.getWalletAmount())
			.paymentDay(wallet.getPaymentDay().toString())
			.startDate(wallet.getStartDate().toString())
			.endDate(wallet.getEndDate().toString())
			.build();
	}
}
