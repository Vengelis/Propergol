package fr.vengelis.propergol.core.communication.redis.instruction;

public record KeyValueIntegerInstruction<T>(String key, T value, int integer) {
}
