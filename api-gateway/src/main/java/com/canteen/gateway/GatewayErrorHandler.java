package com.canteen.gateway;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Component
@Order(-1)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String message = status == HttpStatus.SERVICE_UNAVAILABLE
                ? "Service unavailable"
                : "Request failed";
        byte[] body = ("{\"success\":false,\"message\":\"" + message + "\"}").getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    private static HttpStatus resolveStatus(Throwable ex) {
        if (isUnavailable(ex)) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    private static boolean isUnavailable(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof ConnectException || current instanceof TimeoutException) {
                return true;
            }
            String name = current.getClass().getName();
            if (name.contains("NotFoundException")
                    || name.contains("ConnectException")
                    || name.contains("TimeoutException")
                    || name.contains("UnknownHostException")
                    || name.contains("WebClientRequestException")) {
                return true;
            }
        }
        return false;
    }
}
