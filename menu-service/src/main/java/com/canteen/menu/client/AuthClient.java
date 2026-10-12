package com.canteen.menu.client;

import com.canteen.common.dto.ValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/auth/validate")
    ValidateResponse validate(@RequestParam("token") String token,
                              @RequestHeader("Authorization") String authorization);
}
