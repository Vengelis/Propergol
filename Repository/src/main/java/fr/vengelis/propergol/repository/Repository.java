package fr.vengelis.propergol.repository;


import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.application.ApplicationType;
import fr.vengelis.propergol.core.communication.HandlerService;
import fr.vengelis.propergol.core.communication.InstructionRequest;
import fr.vengelis.propergol.core.communication.InstructionResponse;
import fr.vengelis.propergol.core.communication.postgres.PostgreService;
import fr.vengelis.propergol.core.communication.postgres.executor.QueryExecutor;
import fr.vengelis.propergol.core.communication.redis.RedisService;
import fr.vengelis.propergol.core.communication.redis.instruction.PublishInstruction;
import fr.vengelis.propergol.core.communication.retention.Retention;

public class Repository {

    public static void main(String[] args) {
        Core core = new Core(ApplicationType.REPOSITORY);
        core.boot();

//        core.getRedisCommunicationSystem().getPubSubAPI().subscribe("TEST-CHANNEL", (c,m) -> {
//            System.out.println(c + " : " + m);
//        });

        new Thread(() -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            InstructionResponse<String> rep = core.getRedisCommunicationSystem().send(
//                    new InstructionRequest<>(
//                            Retention.OBLIGATORY,
//                            InstructionRequest.Handler.REDIS,
//                            RedisService.PUBLISH,
//                            new PublishInstruction("TEST-CHANNEL", "Message")));

            InstructionResponse<String> rep = (InstructionResponse<String>) core.getPostgreCommunicationSystem().send(
                    new InstructionRequest<QueryExecutor<String>>(
                            Retention.OBLIGATORY,
                            InstructionRequest.Handler.POSTGRE,
                            PostgreService.QUERY,
                            connection -> {
                                try (var statement = connection.createStatement();
                                     var resultSet = statement.executeQuery("SELECT data FROM test_table WHERE id = 5")) {
                                    return resultSet.next() ? resultSet.getString("data").toString() : "null";
                                }
                            }));

            System.out.println("Instruction response : " + rep.getSystemResult().name() + " - " + rep.getResult().get());
        }).start();
    }

}
