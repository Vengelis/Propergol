package fr.vengelis.propergol.core.communication.redis;

public interface IPacketsReceiver {
    void receive(String channel, String message) throws Exception;
}
