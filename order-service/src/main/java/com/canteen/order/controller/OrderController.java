package com.canteen.order.controller;

import com.canteen.common.dto.ApiResponse;
import com.canteen.common.dto.Order;
import com.canteen.common.dto.OrderDetailResponse;
import com.canteen.common.dto.OrderRequest;
import com.canteen.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) OrderRequest request) {
        if (request == null) {
            return badRequest("request body is required");
        }
        if (request.menuItemId() == null) {
            return badRequest("menuItemId is required");
        }
        if (request.quantity() <= 0) {
            return badRequest("quantity must be greater than zero");
        }
        Order order = orders.place(request);
        return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable long id) {
        return orders.find(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderDetailResponse> detail(@PathVariable long id) {
        return orders.detail(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> unreadableBody() {
        return badRequest("request body is missing or malformed");
    }

    private static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, null));
    }
}
