package com.example.lifeonhana.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.lifeonhana.dto.request.LumpSumRequestDTO;
import com.example.lifeonhana.dto.response.LumpSumResponseDTO;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.entity.History;
import com.example.lifeonhana.entity.LumpSum;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;
import com.example.lifeonhana.global.exception.BadRequestException;
import com.example.lifeonhana.repository.AccountRepository;
import com.example.lifeonhana.repository.HistoryRepository;
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
	private final HistoryRepository historyRepository;
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
			walletRepository.findWalletIdByUserAuthId(authId).orElseThrow(() -> new BadRequestException("하나지갑이 존재하지 않습니다."));
		Account account = accountRepository.findByAccountId(lumpSumRequestDTO.accountId())
			.orElseThrow(() -> new BadRequestException("유효하지 않은 계좌 id 입니다."));
		if (lumpSumRequestDTO.amount().compareTo(account.getBalance()) > 0) {
			throw new BadRequestException("잔액이 부족합니다.", Map.of("balance", account.getBalance()));
		}
		account.setBalance(account.getBalance().subtract(lumpSumRequestDTO.amount()));
		accountRepository.save(account);
		wallet.setWalletBalance(wallet.getWalletBalance() + lumpSumRequestDTO.amount().longValue());
		walletRepository.save(wallet);

		History history = new History();
		history.setUser(wallet.getUser());
		history.setCategory(History.Category.DEPOSIT);
		history.setAmount(lumpSumRequestDTO.amount());
		history.setDescription("목돈 지급");
		history.setHistoryDatetime(LocalDateTime.now());
		history.setIsFixed(false);
		history.setIsExpense(false);

		historyRepository.save(history);


		return account;
	}
}
