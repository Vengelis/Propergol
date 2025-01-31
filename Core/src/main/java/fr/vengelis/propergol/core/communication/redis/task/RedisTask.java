package fr.vengelis.propergol.core.communication.redis.task;

public abstract class RedisTask {

    private final String channel;

    public RedisTask(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public abstract void run(String message);

}
