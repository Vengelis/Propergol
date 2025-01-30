package fr.vengelis.propergol.core.communication.redis;

import fr.vengelis.propergol.core.communication.CommunicationSystem;
import fr.vengelis.propergol.core.communication.InstructionRequest;
import fr.vengelis.propergol.core.communication.InstructionResponse;
import fr.vengelis.propergol.core.communication.redis.instruction.KeyValueInstruction;
import fr.vengelis.propergol.core.communication.redis.instruction.KeyValueIntegerInstruction;
import fr.vengelis.propergol.core.communication.redis.instruction.PublishInstruction;
import fr.vengelis.propergol.core.communication.redis.task.RedisTaskManager;
import fr.vengelis.propergol.core.communication.retention.RetainedInstruction;
import fr.vengelis.propergol.core.communication.retention.Retention;

public class RedisCommunicationSystem extends CommunicationSystem {

    private final PubSubAPI pubSubAPI = new PubSubAPI();
    private final RedisTaskManager redisTaskManager = new RedisTaskManager();

    public RedisCommunicationSystem() {
        super(System.REDIS);
    }

    public PubSubAPI getPubSubAPI() {
        return pubSubAPI;
    }

    public void boot() {
        pubSubAPI.psubscribe("*", ((pattern,channel,message) -> {
            redisTaskManager.getRedisTasks().stream()
                    .filter(redisTask -> redisTask.getChannel().equalsIgnoreCase(channel))
                    .forEach(redisTask -> redisTask.run(message));
        }));
    }

    @Override
    public InstructionResponse send(InstructionRequest<?> request) {

        if(getServiceStatus().equals(Status.RETAINED))
            return sendToRetainedService(request);

        // TODO : Finir les requetes
        switch ((RedisService) request.getService()) {
            case PUBLISH -> {
                PublishInstruction instruction = (PublishInstruction) request.getData();
                if(pubSubAPI.publish(instruction).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<String>(InstructionResponse.SystemResult.SENDED,
                            "Instruction sended to redis. Channel : " + instruction.channel() +
                                    " | Message : " + instruction.message());
                } else {
                    return sendToRetainedService(request);
                }
            }
            case SET -> {
                KeyValueInstruction<String> instruction = (KeyValueInstruction<String>) request.getData();
                if(RedisConnection.set(instruction.key(), instruction.value()).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<String>(InstructionResponse.SystemResult.SENDED, "Sended");
                } else {
                    return sendToRetainedService(request);
                }
            }
            case SETEX -> {
                KeyValueIntegerInstruction<String> instruction = (KeyValueIntegerInstruction<String>) request.getData();
                if(RedisConnection.set(instruction.key(), instruction.value(), instruction.integer()).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<String>(InstructionResponse.SystemResult.SENDED, "Sended");
                } else {
                    return sendToRetainedService(request);
                }
            }
            case DEL -> {
                String key = request.getData().toString();
                if(RedisConnection.del(key).getType().equals(RedisResult.Type.SUCCESS))
                    return new InstructionResponse<String>(InstructionResponse.SystemResult.SENDED, "Sended");
                else
                    return sendToRetainedService(request);
            }
        }
        return null;
    }

    @Override
    protected void reconnect() {
        RedisConnection.reconnect();
    }

    @Override
    protected boolean isReconnected() {
        return getPubSubAPI().tryHelloWorld().getType().equals(RedisResult.Type.SUCCESS);
    }

    @Override
    protected boolean tryHelloWorldSuccess() {
        return pubSubAPI.tryHelloWorld().getType().equals(RedisResult.Type.SUCCESS);
    }


}
