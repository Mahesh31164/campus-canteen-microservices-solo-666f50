package com.canteen.common.dto;

import java.math.BigDecimal;

public record MenuItem(Long id, String name, BigDecimal price, int stock) {
}
