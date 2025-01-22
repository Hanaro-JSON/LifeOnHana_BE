package com.example.lifeonhana.service;

import java.util.*;
import java.util.stream.Collectors;

import javax.security.auth.login.AccountNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lifeonhana.dto.request.AccountTransferRequest;
import com.example.lifeonhana.dto.response.AccountListResponseDTO;
import com.example.lifeonhana.dto.response.AccountResponseDTO;
import com.example.lifeonhana.dto.response.AccountTransferResponse;
import com.example.lifeonhana.dto.response.SalaryAccountResponseDTO;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.global.exception.InsufficientBalanceException;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.AccountRepository;
import com.example.lifeonhana.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public AccountListResponseDTO getAccounts(Long userId) {
		Account mainAccount = accountRepository.findByMydata_User_UserIdAndServiceAccount(userId, Account.ServiceAccount.SALARY);
		if (mainAccount == null) {
			throw new NotFoundException("메인 계좌를 찾을 수 없습니다.");
		}

		List<Account> allAccounts = accountRepository.findByMydata_User_UserId(userId);

		List<AccountResponseDTO> otherAccounts = allAccounts.stream()
			.filter(account -> !account.getServiceAccount().equals(Account.ServiceAccount.SALARY))
			.map(this::toAccountResponseDTO)
			.collect(Collectors.toList());

		AccountResponseDTO mainAccountDTO = toAccountResponseDTO(mainAccount);

		return new AccountListResponseDTO(mainAccountDTO, otherAccounts);
	}

	@Transactional(readOnly = true)
	public SalaryAccountResponseDTO getSalaryAccount(String authId) {
		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
			
		Account salaryAccount = accountRepository.findByMydata_User_UserIdAndServiceAccount(
			user.getUserId(), Account.ServiceAccount.SALARY);
			
		if (salaryAccount == null) {
			throw new NotFoundException("급여 계좌를 찾을 수 없습니다.");
		}
		
		return new SalaryAccountResponseDTO(
			salaryAccount.getAccountId(),
			salaryAccount.getBalance()
		);
	}

	@Transactional
	public AccountTransferResponse transfer(String authId, AccountTransferRequest request) {
		Account fromAccount = accountRepository.findByAccountIdAndMydata_User_AuthId(
			request.fromAccountId(), authId
		).orElseThrow(() -> new NotFoundException("출금 계좌를 찾을 수 없습니다."));

		Account toAccount = accountRepository.findById(request.toAccountId())
			.orElseThrow(() -> new NotFoundException("입금 계좌를 찾을 수 없습니다."));

		if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
			throw new InsufficientBalanceException("잔액이 부족합니다.");
		}

		fromAccount.withdraw(request.amount());
		toAccount.deposit(request.amount());

		accountRepository.save(fromAccount);
		accountRepository.save(toAccount);

		return new AccountTransferResponse(
			request.amount(),
			toAccountResponseDTO(fromAccount),
			toAccountResponseDTO(toAccount)
		);
	}

	private AccountResponseDTO toAccountResponseDTO(Account account) {
		return new AccountResponseDTO(
			account.getAccountId(),
			account.getBank().name(),
			account.getAccountNumber(),
			account.getAccountName(),
			account.getBalance()
		);
	}
}
