package com.canteen.common.dto;

import java.math.BigDecimal;

public record Order(Long id, String userId, Long menuItemId, int quantity, BigDecimal total) {
}
