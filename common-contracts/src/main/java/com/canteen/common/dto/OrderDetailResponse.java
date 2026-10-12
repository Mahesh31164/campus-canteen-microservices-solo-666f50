package com.canteen.common.dto;

public record OrderDetailResponse(Order order, String buyerName, String itemName) {
}
