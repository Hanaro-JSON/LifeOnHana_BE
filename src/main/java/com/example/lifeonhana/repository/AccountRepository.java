package com.example.lifeonhana.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	List<Account> findByMydata_User_UserId(Long userId);

	Account findByMydata_User_UserIdAndServiceAccount(Long userId, Account.ServiceAccount serviceAccount);

	Account findByMydata_User_UserIdAndServiceAccountAndAccountName(Long userId, Account.ServiceAccount serviceAccount, String accountName);
}
