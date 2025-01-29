package fr.vengelis.propergol.core.communication.retention;

import fr.vengelis.propergol.core.communication.InstructionRequest;

public class RetainedInstruction<T> {

    private final Retention type;
    private final InstructionRequest<T> instructionRequest;

    public RetainedInstruction(Retention type, InstructionRequest<T> instructionRequest) {
        this.type = type;
        this.instructionRequest = instructionRequest;
    }

    public Retention getType() {
        return type;
    }

    public InstructionRequest<T> getInstructionRequest() {
        return instructionRequest;
    }
}
