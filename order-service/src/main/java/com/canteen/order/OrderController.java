package com.canteen.order;

import com.canteen.common.dto.MenuItem;
import com.canteen.common.dto.Order;
import com.canteen.common.dto.OrderDetailResponse;
import com.canteen.common.dto.OrderRequest;
import com.canteen.common.dto.ValidateResponse;
import com.canteen.order.client.MenuClient;
import com.canteen.order.client.TokenValidator;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OrderController {

    private final OrderStore store;
    private final TokenValidator tokens;
    private final MenuClient menuClient;

    public OrderController(OrderStore store, TokenValidator tokens, MenuClient menuClient) {
        this.store = store;
        this.tokens = tokens;
        this.menuClient = menuClient;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @RequestBody(required = false) OrderRequest request) {
        tokens.require(authorization);
        if (request == null || request.menuItemId() == null || request.quantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(store.create(request));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> get(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @PathVariable long id) {
        tokens.require(authorization);
        return store.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/{id}/detail")
    public ResponseEntity<OrderDetailResponse> detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long id) {
        ValidateResponse buyer = tokens.require(authorization);
        return store.findById(id)
                .map(order -> ResponseEntity.ok(enrich(order, buyer, authorization)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private OrderDetailResponse enrich(Order order, ValidateResponse buyer, String authorization) {
        String itemName = "Unknown item";
        try {
            MenuItem item = menuClient.getItem(order.menuItemId(), authorization);
            if (item != null && item.name() != null) {
                itemName = item.name();
            }
        } catch (FeignException.NotFound ignored) {
            // keep fallback name
        } catch (RetryableException | FeignException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable");
        }
        return new OrderDetailResponse(order, buyer.name(), itemName);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Void> unreadableBody() {
        return ResponseEntity.badRequest().build();
    }
}
