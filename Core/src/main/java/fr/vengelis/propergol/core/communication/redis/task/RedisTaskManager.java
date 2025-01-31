package fr.vengelis.propergol.core.communication.redis.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisTaskManager {

    private final List<RedisTask> redisTasks = new ArrayList<>();

    public List<RedisTask> getRedisTasks() {
        return redisTasks;
    }

    public void register(RedisTask task) {
        redisTasks.add(task);
    }

    public void register(RedisTask... tasks) {
        redisTasks.addAll(Arrays.asList(tasks));
    }

}
