package com.example.lifeonhana.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lifeonhana.entity.LumpSum;

public interface LumpSumRepository extends JpaRepository<LumpSum,Long> {
}
