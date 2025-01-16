package com.example.lifeonhana.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.WalletDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
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
		Wallet newWallet = new Wallet();
		newWallet.setUser(user);
		newWallet.setWalletAmount(wallet.getWalletAmount());
		newWallet.setPaymentDay(Wallet.PaymentDay.valueOf(wallet.getPaymentDay()));
		newWallet.setStartDate(LocalDateTime.parse((wallet.getStartDate())));
		newWallet.setEndDate(LocalDateTime.parse((wallet.getEndDate())));
		walletRepository.save(newWallet);

		return WalletDTO.builder()
			.walletId(newWallet.getWalletId())
			.walletAmount(newWallet.getWalletAmount())
			.paymentDay(String.valueOf(newWallet.getPaymentDay()))
			.startDate(String.valueOf(newWallet.getStartDate()))
			.endDate(String.valueOf(newWallet.getEndDate()))
			.build();
	}
}
