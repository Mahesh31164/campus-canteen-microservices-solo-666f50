package com.canteen.menu;

import com.canteen.common.dto.MenuItem;
import com.canteen.common.dto.ReserveRequest;
import org.springframework.http.ResponseEntity;
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
        MenuItem item = catalog.findById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/menu/{id}/reserve")
    public ResponseEntity<MenuItem> reserve(@PathVariable long id, @RequestBody ReserveRequest request) {
        MenuCatalog.ReserveOutcome outcome = catalog.reserve(id, request.qty());
        return switch (outcome.kind()) {
            case RESERVED -> ResponseEntity.ok(outcome.item());
            case MISSING -> ResponseEntity.notFound().build();
            case SHORT_STOCK -> ResponseEntity.status(409).build();
            case INVALID -> ResponseEntity.badRequest().build();
        };
    }
}
