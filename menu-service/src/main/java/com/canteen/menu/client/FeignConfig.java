package com.canteen.menu.client;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
