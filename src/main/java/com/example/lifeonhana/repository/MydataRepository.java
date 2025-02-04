package com.example.lifeonhana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lifeonhana.entity.Mydata;

@Repository
public interface MydataRepository extends JpaRepository<Mydata, Long> {
}
