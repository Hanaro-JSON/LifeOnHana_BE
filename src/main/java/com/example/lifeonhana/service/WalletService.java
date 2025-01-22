package com.example.lifeonhana.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.request.WalletRequestDTO;
import com.example.lifeonhana.dto.response.WalletResponseDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.WalletRepository;

@Service
public class WalletService {
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;

	public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
	}

	public WalletResponseDTO getUserWallet(String authId) {
		Wallet wallet = walletRepository.findWalletIdByUserAuthId(authId)
			.orElseThrow(() -> new NotFoundException("하나지갑이 존재하지 않습니다."));

		return new WalletResponseDTO(
			wallet.getWalletId(),
			wallet.getWalletAmount(),
			String.valueOf(wallet.getPaymentDay()),
			String.valueOf(wallet.getStartDate()),
			String.valueOf(wallet.getEndDate())
		);
	}

	public WalletResponseDTO creatWallet(WalletRequestDTO wallet, String authId) {
		User user = userRepository.getUserByAuthId(authId);
		if (walletRepository.findWalletIdByUserAuthId(authId).isPresent()) {
			throw new BadRequestException("이미 하나지갑 정보가 존재합니다.");
		}
		Wallet newWallet = new Wallet();
		newWallet.setUser(user);
		return setWallet(wallet, newWallet);
	}

	public WalletResponseDTO updateWallet(WalletRequestDTO walletDTO, String authId) {
		Wallet wallet = walletRepository.findWalletIdByUserAuthId(authId)
			.orElseThrow(() -> new NotFoundException("하나지갑이 존재하지 않습니다."));
		return setWallet(walletDTO, wallet);

	}

	private WalletResponseDTO setWallet(WalletRequestDTO walletDTO, Wallet wallet) {
		wallet.setWalletAmount(walletDTO.walletAmount());
		wallet.setPaymentDay(Wallet.PaymentDay.fromValue(walletDTO.paymentDay()));
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			int paymentDay = Integer.parseInt(walletDTO.paymentDay());

			YearMonth startYearMonth = YearMonth.parse(walletDTO.startDate(), formatter);
			YearMonth endYearMonth = YearMonth.parse(walletDTO.endDate(), formatter);

			LocalDate startDate = startYearMonth.atDay(Math.min(paymentDay, startYearMonth.lengthOfMonth()));
			LocalDate endDate = endYearMonth.atDay(Math.min(paymentDay, endYearMonth.lengthOfMonth()));

			wallet.setStartDate(startDate.atStartOfDay());
			wallet.setEndDate(endDate.atStartOfDay());
		} catch (DateTimeParseException | NumberFormatException e) {
			throw new IllegalArgumentException("날짜 형식이 잘못되었습니다. 'yyyy-MM' 형식이어야 합니다.", e);
		}
		walletRepository.save(wallet);

		return new WalletResponseDTO(
			wallet.getWalletId(),
			wallet.getWalletAmount(),
			wallet.getPaymentDay().toString(),
			wallet.getStartDate().toString(),
			wallet.getEndDate().toString()
			);
	}
}
