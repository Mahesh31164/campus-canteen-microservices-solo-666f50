package com.canteen.order.service;

import com.canteen.common.dto.Order;
import com.canteen.common.dto.OrderDetailResponse;
import com.canteen.common.dto.OrderRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Order workflow with stub buyer/item data. T07 is deliberately local: the
 * stub constants below are the seams that later tickets replace with real
 * auth-service and menu-service lookups.
 */
@Service
public class OrderService {

    static final String STUB_USER_ID = "stub-user";
    static final String STUB_BUYER_NAME = "Stub Buyer";
    static final String STUB_ITEM_NAME = "Stub Item";
    static final BigDecimal STUB_UNIT_PRICE = new BigDecimal("10.00");

    private final OrderStore store;

    public OrderService(OrderStore store) {
        this.store = store;
    }

    public Order place(OrderRequest request) {
        BigDecimal total = STUB_UNIT_PRICE.multiply(BigDecimal.valueOf(request.quantity()));
        return store.save(STUB_USER_ID, request.menuItemId(), request.quantity(), total);
    }

    public Optional<Order> find(long id) {
        return store.findById(id);
    }

    public Optional<OrderDetailResponse> detail(long id) {
        return store.findById(id)
                .map(order -> new OrderDetailResponse(order, STUB_BUYER_NAME, STUB_ITEM_NAME));
    }
}
