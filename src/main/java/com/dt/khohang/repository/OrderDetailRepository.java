package com.dt.khohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dt.khohang.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
