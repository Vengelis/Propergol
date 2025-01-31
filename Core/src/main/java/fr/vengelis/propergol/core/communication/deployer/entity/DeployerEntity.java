package fr.vengelis.propergol.core.communication.deployer.entity;

public class DeployerEntity {

    public enum EntityType {
        SLAVE,
        REPOSITORY,
        WEBCONNECT,
        ;
    }

    public enum IntegrationStep {
        WAITING,
        ARRIVED,
        AUTHENTICATING,
        INTEGRATED,
        REFUSED,
        ;
    }

    private final EntityType entityType;
    private IntegrationStep integrationStep = IntegrationStep.WAITING;

    public DeployerEntity(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public IntegrationStep getIntegrationStep() {
        return integrationStep;
    }

    public void setIntegrationStep(IntegrationStep integrationStep) {
        this.integrationStep = integrationStep;
    }
}
