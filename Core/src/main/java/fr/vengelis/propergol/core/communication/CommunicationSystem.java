package fr.vengelis.propergol.core.communication;

import fr.vengelis.propergol.core.communication.retention.RetainedInstruction;
import fr.vengelis.propergol.core.communication.retention.Retention;
import fr.vengelis.propergol.core.utils.ConsoleLogger;

import java.util.ArrayDeque;
import java.util.logging.Level;

public abstract class CommunicationSystem {

    public enum System {
        POSTGRE,
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

    protected void registerIntoRetained(RetainedInstruction<?> instruction) {
        retainedInstructions.addLast(instruction);
    }

    public ArrayDeque<RetainedInstruction<?>> getRetainedInstructions() {
        return retainedInstructions;
    }

    protected abstract void reconnect();
    protected abstract boolean isReconnected();
    protected abstract boolean tryHelloWorldSuccess();

    protected abstract void boot();

    protected <I> InstructionResponse<String> sendToRetainedService(InstructionRequest<I> request) {
        if(request.getRetention().equals(Retention.OBLIGATORY)) {
            RetainedInstruction<I> retainedInstruction =
                    new RetainedInstruction<>(request.getRetention(), request);
            registerIntoRetained(retainedInstruction);
            return new InstructionResponse<String>(InstructionResponse.SystemResult.RETAINED,
                    "Instruction was placed in retention service, it will be sent as soon " +
                            "as the connection is reestablished");
        } else {
            return new InstructionResponse<String>(InstructionResponse.SystemResult.FORGOTTEN,
                    "Communication with the redis service is not available, " +
                            "the request has been abandoned");
        }
    }

    private void startRetentionSystem() {
        new Thread(() -> {
            boolean tryOne = false;
            while(true) {
                try {
                    Thread.sleep(5000);
                    if(serviceStatus.equals(Status.LINKED)) {
                        if(!tryHelloWorldSuccess()) {
                            ConsoleLogger.printLine(Level.INFO, "CommThread : " + serviceStatus + " -> RETAINED");
                            serviceStatus = Status.RETAINED;
                        }
                    } else if(serviceStatus.equals(Status.RETAINED)) {
                        if(tryHelloWorldSuccess()) {
                            ConsoleLogger.printLine(Level.INFO, "CommThread : " + serviceStatus + " -> RETAINED_TO_DATABASE");
                            serviceStatus = Status.RETAINED_TO_DATABASE;
                            tryOne = true;
                        }
                    }

                    if(serviceStatus.equals(Status.RETAINED_TO_DATABASE)){
                        if(tryOne) {
                            ConsoleLogger.printLine(Level.INFO, "CommThread : " + assigned.name() + " reconnection...");
                            reconnect();
                            if (isReconnected()) {
                                tryOne = false;
                                ConsoleLogger.printLine(Level.INFO, "CommThread : " + assigned.name() + " connected !");

                                int size = retainedInstructions.size();
                                ConsoleLogger.printLine(Level.INFO, "CommThread : waiting instructions : " + size);
                                for (int i = 0; i < size; i++) {
                                    RetainedInstruction<?> instructionRequest = retainedInstructions.poll();
                                    InstructionRequest<?> newInstruction = instructionRequest.getInstructionRequest();
                                    ConsoleLogger.printLine(Level.INFO, "Try resending : " +
                                            newInstruction.getHandler().name() + "|" +
                                            newInstruction.getService().name() + "|" +
                                            newInstruction.getData().toString());
                                    InstructionResponse<String> rep = send(newInstruction);
                                    if (rep.getSystemResult().equals(InstructionResponse.SystemResult.RETAINED)) {
                                        tryOne = true;
                                    }
                                    ConsoleLogger.printLine(Level.INFO, "Resended result : " + rep.getSystemResult().name() + " : " + rep.getResult().get());
                                }
                            } else {
                                ConsoleLogger.printLine(Level.INFO, "CommThread : " + assigned.name() + " not reconnected !");
                            }

                        }

                        if(retainedInstructions.isEmpty()) {
                            ConsoleLogger.printLine(Level.INFO, "CommThread : " + serviceStatus + " -> LINKED");
                            serviceStatus = Status.LINKED;
                        }

                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


}
