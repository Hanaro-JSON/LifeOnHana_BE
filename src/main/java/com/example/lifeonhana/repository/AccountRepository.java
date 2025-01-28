package com.example.lifeonhana.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findByMydata_User_UserId(Long userId);

	Optional<Account> findByAccountIdAndMydata_User_AuthId(Long accountId, String authId);

	Account findByMydata_User_UserIdAndServiceAccount(Long userId, Account.ServiceAccount serviceAccount);

	Account findByMydata_User_UserIdAndServiceAccountAndAccountName(Long userId, Account.ServiceAccount serviceAccount, String accountName);

	Optional<Account> findByAccountId(Long accountId);
	Account findByServiceAccount(Account.ServiceAccount serviceAccount);
}
