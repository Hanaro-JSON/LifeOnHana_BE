package com.example.lifeonhana.repository;

import com.example.lifeonhana.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByAuthId(String authId);
	Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
