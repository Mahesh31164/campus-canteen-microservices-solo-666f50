package com.canteen.menu;

import com.canteen.common.dto.ApiResponse;
import com.canteen.common.dto.MenuItem;
import com.canteen.common.dto.ReserveRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class MenuController {

    private final MenuCatalog catalog;

    public MenuController(MenuCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/menu")
    public Collection<MenuItem> list() {
        return catalog.findAll();
    }

    @GetMapping("/menu/{id}")
    public ResponseEntity<MenuItem> get(@PathVariable long id) {
        return catalog.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/menu/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable long id, @RequestBody(required = false) ReserveRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        MenuCatalog.ReserveOutcome outcome = catalog.reserve(id, request.qty());
        return switch (outcome.kind()) {
            case RESERVED -> ResponseEntity.ok(outcome.item());
            case MISSING -> ResponseEntity.notFound().build();
            case SHORT_STOCK -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "Insufficient stock", null));
            case INVALID -> ResponseEntity.badRequest().build();
        };
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Void> unreadableBody() {
        return ResponseEntity.badRequest().build();
    }
}
