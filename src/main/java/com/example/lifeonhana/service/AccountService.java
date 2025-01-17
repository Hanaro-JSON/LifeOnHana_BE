package com.example.lifeonhana.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lifeonhana.dto.response.AccountListResponseDTO;
import com.example.lifeonhana.dto.response.AccountResponseDTO;
import com.example.lifeonhana.entity.Account;
import com.example.lifeonhana.global.exception.NotFoundException;
import com.example.lifeonhana.repository.AccountRepository;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

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

	private AccountResponseDTO toAccountResponseDTO(Account account) {
		return new AccountResponseDTO(
			account.getBank().name(),
			account.getAccountNumber(),
			account.getAccountName(),
			account.getBalance()
		);
	}
}
