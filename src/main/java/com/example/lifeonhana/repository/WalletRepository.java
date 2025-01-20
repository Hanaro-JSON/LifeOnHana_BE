package com.example.lifeonhana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	Wallet findWalletIdByUserUserId(Long userId);

	@Query("SELECT CAST(COALESCE(SUM(w.walletAmount), 0) AS int) FROM Wallet w WHERE w.user = :user")
	Integer findCurrentBalance(@Param("user") User user);
}
