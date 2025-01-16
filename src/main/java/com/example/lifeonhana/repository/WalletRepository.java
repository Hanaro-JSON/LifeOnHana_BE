package com.example.lifeonhana.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lifeonhana.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	Wallet findWalletIdByUserUserId(Long userId);
}
