package com.canteen.auth;

import com.canteen.common.dto.AuthResponse;
import com.canteen.common.dto.ValidateResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthStore {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> tokens = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        add("1", "alice", "password", "Alice");
        add("2", "bob", "password", "Bob");
        add("3", "carol", "password", "Carol");
    }

    public Optional<AuthResponse> login(String username, String password) {
        User user = users.get(username);
        if (user == null || !user.password().equals(password)) {
            return Optional.empty();
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, user);
        return Optional.of(new AuthResponse(token, user.userId(), user.name()));
    }

    public Optional<ValidateResponse> validate(String token) {
        User user = tokens.get(token);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(new ValidateResponse(user.userId(), user.name()));
    }

    private void add(String userId, String username, String password, String name) {
        users.put(username, new User(userId, username, password, name));
    }

    record User(String userId, String username, String password, String name) {
    }
}
