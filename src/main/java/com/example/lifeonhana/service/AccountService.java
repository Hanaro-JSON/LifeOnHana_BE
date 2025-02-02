package com.example.lifeonhana.service;

import java.math.BigDecimal;
import java.util.*;


import com.example.lifeonhana.global.exception.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lifeonhana.dto.request.AccountTransferRequest;
import com.example.lifeonhana.dto.response.AccountListResponseDTO;
import com.example.lifeonhana.dto.response.AccountResponseDTO;
import com.example.lifeonhana.dto.response.AccountTransferResponse;
import com.example.lifeonhana.dto.response.SalaryAccountResponseDTO;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.entity.User;
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
		Account mainAccount = accountRepository.findByMydata_User_UserIdAndServiceAccount(userId, Account.ServiceAccount.SALARY)
			.orElseThrow(() -> new NotFoundException(ErrorCode.MAIN_ACCOUNT_NOT_FOUND));

		List<Account> allAccounts = accountRepository.findByMydata_User_UserId(userId);

		List<AccountResponseDTO> otherAccounts = allAccounts.stream()
			.filter(account -> !account.getServiceAccount().equals(Account.ServiceAccount.SALARY))
			.map(this::toAccountResponseDTO)
			.toList();

		return new AccountListResponseDTO(toAccountResponseDTO(mainAccount), otherAccounts);
	}

	@Transactional(readOnly = true)
	public SalaryAccountResponseDTO getSalaryAccount(String authId) {
		User user = userRepository.findByAuthId(authId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
			
		Account salaryAccount = accountRepository.findByMydata_User_UserIdAndServiceAccount(
			user.getUserId(), Account.ServiceAccount.SALARY)
			.orElseThrow(() -> new NotFoundException(ErrorCode.SALARY_ACCOUNT_NOT_FOUND));
			
		return new SalaryAccountResponseDTO(
			salaryAccount.getAccountId(),
			salaryAccount.getBalance()
		);
	}

	@Transactional
	public AccountTransferResponse transfer(String authId, AccountTransferRequest request) {
		if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException(ErrorCode.NEGATIVE_TRANSFER_AMOUNT);
		}

		if (request.fromAccountId().equals(request.toAccountId())) {
			throw new BadRequestException(ErrorCode.TRANSFER_SAME_ACCOUNT);
		}

		Account fromAccount = accountRepository.findByAccountIdAndMydata_User_AuthId(
			request.fromAccountId(), authId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

		Account toAccount = accountRepository.findById(request.toAccountId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

		if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
			throw new InsufficientBalanceException(ErrorCode.INSUFFICIENT_BALANCE);
		}

		fromAccount.withdraw(request.amount());
		toAccount.deposit(request.amount());

		accountRepository.saveAll(List.of(fromAccount, toAccount));

		return new AccountTransferResponse(
			request.amount(),
			toAccountResponseDTO(fromAccount),
			toAccountResponseDTO(toAccount)
		);
	}

	@Scheduled(cron = "0 0 0 1 * *")
	@Transactional
	public void applyMonthlyInterest() {
		try {
			Account salaryAccounts = accountRepository.findByServiceAccount(Account.ServiceAccount.SALARY);
			
			BigDecimal interest = salaryAccounts.getBalance().multiply(new BigDecimal("0.02"));
			salaryAccounts.deposit(interest);
			accountRepository.save(salaryAccounts);
			
		} catch (Exception e) {
			throw new InternalServerException(ErrorCode.INTEREST_CALCULATION_FAILED, e.getMessage());
		}
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
