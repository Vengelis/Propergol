package fr.vengelis.propergol.repository;


import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.application.ApplicationType;
import fr.vengelis.propergol.core.communication.HandlerService;
import fr.vengelis.propergol.core.communication.InstructionRequest;
import fr.vengelis.propergol.core.communication.InstructionResponse;
import fr.vengelis.propergol.core.communication.redis.RedisService;
import fr.vengelis.propergol.core.communication.redis.instruction.PublishInstruction;
import fr.vengelis.propergol.core.communication.retention.Retention;

public class Repository {

    public static void main(String[] args) {
        Core core = new Core(ApplicationType.REPOSITORY);
        core.boot();

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            InstructionResponse<String> rep = core.getRedisCommunicationSystem().send(
                    new InstructionRequest<>(
                            Retention.OBLIGATORY,
                            InstructionRequest.Handler.REDIS,
                            RedisService.PUBLISH,
                            new PublishInstruction("TEST-CHANNEL", "Message")));

            System.out.println("Instruction response : " + rep.getSystemResult().name() + " - " + rep.getResult().get());
        }).start();
    }

}
