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

import java.util.Set;

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

        switch ((RedisService) request.getService()) {
            case PUBLISH -> {
                PublishInstruction instruction = (PublishInstruction) request.getData();
                if(pubSubAPI.publish(instruction).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED,
                            "Instruction sended to redis. Channel : " + instruction.channel() +
                                    " | Message : " + instruction.message());
                } else {
                    return sendToRetainedService(request);
                }
            }
            case SET -> {
                KeyValueInstruction<String> instruction = (KeyValueInstruction<String>) request.getData();
                if(RedisConnection.set(instruction.key(), instruction.value()).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Sended");
                } else {
                    return sendToRetainedService(request);
                }
            }
            case SETEX -> {
                KeyValueIntegerInstruction<String> instruction = (KeyValueIntegerInstruction<String>) request.getData();
                if(RedisConnection.set(instruction.key(), instruction.value(), instruction.integer()).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Sended");
                } else {
                    return sendToRetainedService(request);
                }
            }
            case DEL -> {
                String key = request.getData().toString();
                if(RedisConnection.del(key).getType().equals(RedisResult.Type.SUCCESS))
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Sended");
                else
                    return sendToRetainedService(request);
            }
            case GET -> {
                String key = request.getData().toString();
                RedisResult<String> rtn = RedisConnection.get(key);
                if(rtn.getType().equals(RedisResult.Type.SUCCESS))
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, rtn);
                else
                    return sendToRetainedService(request);
            }
            case EX -> {
                KeyValueInstruction<Integer> instruction = (KeyValueInstruction<Integer>) request.getData();
                if(RedisConnection.expire(instruction.key(), instruction.value()).getType().equals(RedisResult.Type.SUCCESS)) {
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Sended");
                } else {
                    return sendToRetainedService(request);
                }
            }
            case GETKEYS -> {
                String key = request.getData().toString();
                RedisResult<Set<String>> rtn = RedisConnection.getKeys(key);
                if(rtn.getType().equals(RedisResult.Type.SUCCESS))
                    return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, rtn);
                else
                    return sendToRetainedService(request);
            }
            case RECONNECT -> {
                reconnect();
                return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Sended");
            }
            case CLOSE -> {
                RedisConnection.close();
                return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Connection closed");
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

    public RedisTaskManager getRedisTaskManager() {
        return redisTaskManager;
    }
}
