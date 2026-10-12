package com.canteen.order;

import com.canteen.common.dto.Order;
import com.canteen.common.dto.OrderRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderStore {

    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    public Order create(OrderRequest request) {
        long id = ids.getAndIncrement();
        Order order = new Order(
                id,
                "stub-user",
                request.menuItemId(),
                request.quantity(),
                BigDecimal.ZERO
        );
        orders.put(id, order);
        return order;
    }

    public Optional<Order> findById(long id) {
        return Optional.ofNullable(orders.get(id));
    }
}
