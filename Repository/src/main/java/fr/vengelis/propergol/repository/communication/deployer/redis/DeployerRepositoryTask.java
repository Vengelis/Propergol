package fr.vengelis.propergol.repository.communication.deployer.redis;

import fr.vengelis.propergol.core.communication.redis.task.RedisTask;

public class DeployerRepositoryTask extends RedisTask {

    public DeployerRepositoryTask() {
        super("R-DEPLOYER-LISTENER-REGISTER");
    }

    @Override
    public void run(String message) {

    }
}
