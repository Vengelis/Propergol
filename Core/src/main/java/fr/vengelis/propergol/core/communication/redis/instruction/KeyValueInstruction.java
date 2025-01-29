package fr.vengelis.propergol.core.communication.redis.instruction;

public record KeyValueInstruction<T>(String key, T value) {
}
