package com.canteen.order;

import com.canteen.common.dto.Order;
import com.canteen.common.dto.OrderDetailResponse;
import com.canteen.common.dto.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderStore store;

    public OrderController(OrderStore store) {
        this.store = store;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> create(@RequestBody(required = false) OrderRequest request) {
        if (request == null || request.menuItemId() == null || request.quantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(store.create(request));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> get(@PathVariable long id) {
        return store.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/{id}/detail")
    public ResponseEntity<OrderDetailResponse> detail(@PathVariable long id) {
        return store.findById(id)
                .map(order -> ResponseEntity.ok(
                        new OrderDetailResponse(order, "Stub Buyer", "Stub Item")))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Void> unreadableBody() {
        return ResponseEntity.badRequest().build();
    }
}
