package ru.yandex.practicum.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Component
public class SessionStarterFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getSession()
                .doOnNext(WebSession::start)
                .then(chain.filter(exchange));
    }
}