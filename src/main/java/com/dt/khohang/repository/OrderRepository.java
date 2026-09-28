package com.dt.khohang.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dt.khohang.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    List<Order> findByStatus(String status);

    List<Order> findAllByOrderByOrderDateDesc();

    long countByStatus(String status);
}
