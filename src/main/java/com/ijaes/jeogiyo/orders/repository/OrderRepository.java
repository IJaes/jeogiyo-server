package com.ijaes.jeogiyo.orders.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ijaes.jeogiyo.orders.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

}