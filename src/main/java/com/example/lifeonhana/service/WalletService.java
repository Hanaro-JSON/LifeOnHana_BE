package com.example.lifeonhana.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.request.WalletRequestDTO;
import com.example.lifeonhana.dto.response.WalletResponseDTO;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;
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
			.orElseThrow(() -> new BaseException(ErrorCode.WALLET_NOT_FOUND, authId));

		return convertToResponse(wallet);
	}

	public WalletResponseDTO creatWallet(WalletRequestDTO wallet, String authId) {
		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND, authId));

		if (walletRepository.findWalletIdByUserAuthId(authId).isPresent()) {
			throw new BaseException(ErrorCode.WALLET_ALREADY_EXISTS, authId);
		}

		Wallet newWallet = new Wallet();
		newWallet.setUser(user);
		return setWallet(wallet, newWallet);
	}

	public WalletResponseDTO updateWallet(WalletRequestDTO walletDTO, String authId) {
		Wallet wallet = walletRepository.findWalletIdByUserAuthId(authId)
			.orElseThrow(() -> new BaseException(ErrorCode.WALLET_NOT_FOUND, authId));

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
			throw new BaseException(ErrorCode.INVALID_DATE_FORMAT, e);
		}
		walletRepository.save(wallet);

		return new WalletResponseDTO(
			wallet.getWalletId(),
			wallet.getWalletAmount(),
			wallet.getPaymentDay().getValue(),
			formatDate(wallet.getStartDate()),
			formatDate(wallet.getEndDate())
		);
	}

	private WalletResponseDTO convertToResponse(Wallet wallet) {
		return new WalletResponseDTO(
			wallet.getWalletId(),
			wallet.getWalletAmount(),
			wallet.getPaymentDay().getValue(),
			formatDate(wallet.getStartDate()),
			formatDate(wallet.getEndDate())
		);
	}

	private String formatDate(TemporalAccessor date) {
		return DateTimeFormatter.ofPattern("yyyy-MM").format(date);
	}
}
