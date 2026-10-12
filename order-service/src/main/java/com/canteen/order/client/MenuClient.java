package com.canteen.order.client;

import com.canteen.common.dto.MenuItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "menu-service")
public interface MenuClient {

    @GetMapping("/menu/{id}")
    MenuItem getItem(@PathVariable("id") Long id,
                     @RequestHeader("Authorization") String authorization);
}
