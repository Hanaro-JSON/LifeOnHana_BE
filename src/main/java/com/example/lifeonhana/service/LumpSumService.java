package com.example.lifeonhana.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.request.LumpSumRequestDTO;
import com.example.lifeonhana.dto.response.LumpSumResponseDTO;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.entity.LumpSum;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;
import com.example.lifeonhana.repository.AccountRepository;
import com.example.lifeonhana.repository.LumpSumRepository;
import com.example.lifeonhana.repository.UserRepository;
import com.example.lifeonhana.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LumpSumService {
	private final LumpSumRepository lumpSumRepository;
	private final AccountRepository accountRepository;
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;

	public LumpSumResponseDTO createLumpSum(String authId, LumpSumRequestDTO lumpSumRequestDTO){
		User user = userRepository.getUserByAuthId(authId);
		Account account = validateRequest(authId, lumpSumRequestDTO);

		LumpSum lumpSum = LumpSum.builder()
			.user(user)
			.amount(lumpSumRequestDTO.amount())
			.source(lumpSumRequestDTO.source())
			.reason(lumpSumRequestDTO.reason())
			.reasonDetail(lumpSumRequestDTO.reasonDetail())
			.requestDate(LocalDateTime.now())
			.build();
		LumpSum lumpSumRequest = lumpSumRepository.save(lumpSum);
		return LumpSumResponseDTO.fromEntity(lumpSumRequest, account.getBalance());
	}

	private Account validateRequest(String authId, LumpSumRequestDTO lumpSumRequestDTO) {
		Wallet wallet =
			walletRepository.findWalletIdByUserAuthId(authId).orElseThrow(() -> new BaseException(ErrorCode.WALLET_NOT_FOUND));
		Account account = accountRepository.findByAccountId(lumpSumRequestDTO.accountId())
			.orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_NOT_FOUND));
		if (lumpSumRequestDTO.amount().compareTo(account.getBalance()) > 0) {
			throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE, Map.of("balance", account.getBalance()));
		}
		account.setBalance(account.getBalance().subtract(lumpSumRequestDTO.amount()));
		accountRepository.save(account);
		wallet.setWalletAmount(wallet.getWalletAmount() + lumpSumRequestDTO.amount().longValue());
		walletRepository.save(wallet);

		return account;
	}
}
