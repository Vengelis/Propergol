package fr.vengelis.propergol.core.communication;

import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.communication.redis.RedisConnection;
import fr.vengelis.propergol.core.communication.redis.RedisResult;
import fr.vengelis.propergol.core.communication.retention.RetainedInstruction;
import fr.vengelis.propergol.core.utils.ConsoleLogger;

import java.util.ArrayDeque;
import java.util.logging.Level;

public abstract class CommunicationSystem {

    public enum System {
        REDIS,
        DEPLOYER,
        ;
    }

    public enum Status {
        LINKED,
        RETAINED_TO_DATABASE,
        RETAINED,
        ;

    }

    private final System assigned;
    private Status serviceStatus = Status.RETAINED;
    private final ArrayDeque<RetainedInstruction<?>> retainedInstructions = new ArrayDeque<>();

    public CommunicationSystem(System assigned) {
        this.assigned = assigned;
        startRetentionSystem();
    }

    public System getAssigned() {
        return assigned;
    }

    public Status getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Status serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public abstract InstructionResponse send(InstructionRequest<?> request);

    protected void registerIntoReteined(RetainedInstruction<?> instruction) {
        retainedInstructions.addLast(instruction);
    }

    public ArrayDeque<RetainedInstruction<?>> getRetainedInstructions() {
        return retainedInstructions;
    }

    private void startRetentionSystem() {
        new Thread(() -> {
            boolean tryOne = false;
            while(true) {
                try {
                    Thread.sleep(5000);
                    if(serviceStatus.equals(Status.LINKED)) {
                        if(assigned.equals(System.REDIS)) {
                            if(Core.get().getRedisCommunicationSystem().getPubSubAPI().tryHelloWorld().getType().equals(RedisResult.Type.ERROR)) {
                                serviceStatus = Status.RETAINED;
                                ConsoleLogger.printLine(Level.INFO, "Thread : passage en RETAINED");
                            }
                        }
                    } else if(serviceStatus.equals(Status.RETAINED)) {
                        if(assigned.equals(System.REDIS)) {
                            if(Core.get().getRedisCommunicationSystem().getPubSubAPI().tryHelloWorld().getType().equals(RedisResult.Type.SUCCESS)) {
                                serviceStatus = Status.RETAINED_TO_DATABASE;
                                tryOne = true;
                                ConsoleLogger.printLine(Level.INFO, "Thread : passage en RETAINED_TO_DATABASE");
                            }
                        }
                    }

                    if(serviceStatus.equals(Status.RETAINED_TO_DATABASE)){
                        if(tryOne) {
                            RedisConnection.reconnect();
                            ConsoleLogger.printLine(Level.INFO, "Thread : redis reconnection...");
                            if(Core.get().getRedisCommunicationSystem().getPubSubAPI().tryHelloWorld().getType().equals(RedisResult.Type.SUCCESS)) {
                                tryOne = false;
                                ConsoleLogger.printLine(Level.INFO, "Thread : redis connected !");

                                int size = retainedInstructions.size();
                                ConsoleLogger.printLine(Level.INFO, "Thread : instructions en attente : " + size);
                                for(int i = 0; i < size; i++) {
                                    RetainedInstruction<?> instructionRequest = retainedInstructions.poll();
                                    InstructionRequest<?> newInstruction = instructionRequest.getInstructionRequest();
                                    ConsoleLogger.printLine(Level.INFO, "Try resending : " +
                                            newInstruction.getHandler().name() + "|" +
                                            newInstruction.getService().name() + "|" +
                                            newInstruction.getData().toString());
                                    InstructionResponse<String> rep = send(newInstruction);
                                    if(rep.getSystemResult().equals(InstructionResponse.SystemResult.RETAINED)) {
                                        tryOne = true;
                                    }
                                    ConsoleLogger.printLine(Level.INFO, "Resended result : " + rep.getSystemResult().name() + " : " + rep.getResult().get());
                                }
                            } else {
                                ConsoleLogger.printLine(Level.INFO, "Thread : redis not connected !");
                            }
                        }


                        if(retainedInstructions.isEmpty()) {
                            serviceStatus = Status.LINKED;
                            ConsoleLogger.printLine(Level.INFO, "Thread : passage en LINKED");
                        }

                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
