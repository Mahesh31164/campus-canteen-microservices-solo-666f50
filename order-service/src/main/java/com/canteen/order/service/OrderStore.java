package com.canteen.order.service;

import com.canteen.common.dto.Order;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderStore {

    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Order save(String userId, Long menuItemId, int quantity, BigDecimal total) {
        long id = sequence.incrementAndGet();
        Order order = new Order(id, userId, menuItemId, quantity, total);
        orders.put(id, order);
        return order;
    }

    public Optional<Order> findById(long id) {
        return Optional.ofNullable(orders.get(id));
    }
}
