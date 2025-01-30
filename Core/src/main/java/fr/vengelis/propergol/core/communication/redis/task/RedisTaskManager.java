package fr.vengelis.propergol.core.communication.redis.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisTaskManager {

    private final List<AbstractRedisTask> redisTasks = new ArrayList<>();

    public List<AbstractRedisTask> getRedisTasks() {
        return redisTasks;
    }

    public void register(AbstractRedisTask task) {
        redisTasks.add(task);
    }

    public void register(AbstractRedisTask... tasks) {
        redisTasks.addAll(Arrays.asList(tasks));
    }

}
