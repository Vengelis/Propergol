package fr.vengelis.propergol.core.communication.redis;

import fr.vengelis.propergol.core.communication.CommunicationSystem;
import fr.vengelis.propergol.core.communication.InstructionRequest;
import fr.vengelis.propergol.core.communication.InstructionResponse;
import fr.vengelis.propergol.core.communication.redis.instruction.KeyValueInstruction;
import fr.vengelis.propergol.core.communication.redis.instruction.KeyValueIntegerInstruction;
import fr.vengelis.propergol.core.communication.redis.instruction.PublishInstruction;
import fr.vengelis.propergol.core.communication.retention.RetainedInstruction;
import fr.vengelis.propergol.core.communication.retention.Retention;

public class RedisCommunicationSystem extends CommunicationSystem {

    private final PubSubAPI pubSubAPI = new PubSubAPI();

    public RedisCommunicationSystem() {
        super(System.REDIS);
    }

    public PubSubAPI getPubSubAPI() {
        return pubSubAPI;
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

    private <I> InstructionResponse<String> sendToRetainedService(InstructionRequest<I> request) {
        if(request.getRetention().equals(Retention.OBLIGATORY)) {
            RetainedInstruction<I> retainedInstruction =
                    new RetainedInstruction<>(request.getRetention(), request);
            registerIntoReteined(retainedInstruction);
            return new InstructionResponse<String>(InstructionResponse.SystemResult.RETAINED,
                    "Instruction was placed in retention service, it will be sent as soon " +
                            "as the connection is reestablished");
        } else {
            return new InstructionResponse<String>(InstructionResponse.SystemResult.FORGOTTEN,
                    "Communication with the redis service is not available, " +
                            "the request has been abandoned");
        }
    }
}
