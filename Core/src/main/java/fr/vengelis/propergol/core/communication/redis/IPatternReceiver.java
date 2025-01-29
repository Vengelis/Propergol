package fr.vengelis.propergol.core.communication.redis;

public interface IPatternReceiver {
    void receive(String pattern, String channel, String message);
}
