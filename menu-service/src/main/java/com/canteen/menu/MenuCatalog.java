package com.canteen.menu;

import com.canteen.common.dto.MenuItem;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MenuCatalog {

    private final ConcurrentHashMap<Long, MenuItem> items = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        add(1L, "Veg Puff", "25.00", 20);
        add(2L, "Samosa", "15.00", 30);
        add(3L, "Masala Dosa", "50.00", 15);
        add(4L, "Filter Coffee", "20.00", 40);
        add(5L, "Veg Biryani", "80.00", 10);
    }

    public Collection<MenuItem> findAll() {
        return List.copyOf(items.values());
    }

    public Optional<MenuItem> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    public ReserveOutcome reserve(long id, int qty) {
        if (qty <= 0) {
            return ReserveOutcome.invalid();
        }
        boolean[] missing = {false};
        boolean[] shortStock = {false};
        MenuItem updated = items.compute(id, (key, current) -> {
            if (current == null) {
                missing[0] = true;
                return null;
            }
            if (qty > current.stock()) {
                shortStock[0] = true;
                return current;
            }
            return new MenuItem(current.id(), current.name(), current.price(), current.stock() - qty);
        });
        if (missing[0]) {
            return ReserveOutcome.missing();
        }
        if (shortStock[0]) {
            return ReserveOutcome.shortStock();
        }
        return ReserveOutcome.reserved(updated);
    }

    private void add(long id, String name, String price, int stock) {
        items.put(id, new MenuItem(id, name, new BigDecimal(price), stock));
    }

    record ReserveOutcome(Kind kind, MenuItem item) {
        enum Kind { RESERVED, MISSING, SHORT_STOCK, INVALID }

        static ReserveOutcome reserved(MenuItem item) {
            return new ReserveOutcome(Kind.RESERVED, item);
        }

        static ReserveOutcome missing() {
            return new ReserveOutcome(Kind.MISSING, null);
        }

        static ReserveOutcome shortStock() {
            return new ReserveOutcome(Kind.SHORT_STOCK, null);
        }

        static ReserveOutcome invalid() {
            return new ReserveOutcome(Kind.INVALID, null);
        }
    }
}
