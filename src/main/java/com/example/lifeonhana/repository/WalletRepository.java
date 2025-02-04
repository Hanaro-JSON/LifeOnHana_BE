package com.example.lifeonhana.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.lifeonhana.entity.User;
import com.example.lifeonhana.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	Optional<Wallet> findWalletIdByUserAuthId(String authId);

	@Query("SELECT CAST(COALESCE(SUM(w.walletBalance), 0) AS int) FROM Wallet w WHERE w.user = :user")
	Integer findCurrentBalance(@Param("user") User user);
}
