package com.canteen.common.dto;

public record AuthResponse(String token, String userId, String name) {
}
