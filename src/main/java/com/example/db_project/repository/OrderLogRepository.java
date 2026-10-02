package com.example.db_project.repository;

import com.example.db_project.domain.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> { }
