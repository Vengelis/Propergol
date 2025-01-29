package fr.vengelis.propergol.core.communication.redis.instruction;

public record PublishInstruction(String channel, String message) {
}
