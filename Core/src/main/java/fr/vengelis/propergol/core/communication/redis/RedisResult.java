package fr.vengelis.propergol.core.communication.redis;

import java.util.Optional;

public class RedisResult<T> {

    public enum Type {
        SUCCESS,
        ERROR,
        ;
    }

    private final Type type;
    private final Optional<T> data;

    public RedisResult(Type type) {
        this.type = type;
        this.data = Optional.empty();
    }

    public RedisResult(Type type, T data) {
        this.type = type;
        this.data = Optional.of(data);
    }

    public Type getType() {
        return type;
    }

    public Optional<T> getData() {
        return data;
    }
}
