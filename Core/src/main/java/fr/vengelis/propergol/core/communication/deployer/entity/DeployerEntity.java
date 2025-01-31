package fr.vengelis.propergol.core.communication.deployer.entity;

import com.google.gson.Gson;
import fr.vengelis.propergol.core.application.Entity;

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
        INTEGRATED,
        REFUSED,
        ;
    }

    private final EntityType entityType;
    private final Entity entity;
    private IntegrationStep integrationStep = IntegrationStep.WAITING;

    public DeployerEntity(EntityType entityType, Entity entity) {
        this.entityType = entityType;
        this.entity = entity;
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

    public Entity getEntity() {
        return entity;
    }

    public static String serialize(DeployerEntity entity) {
        Gson gson = new Gson();
        return gson.toJson(entity);
    }

    public static DeployerEntity deserialize(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, DeployerEntity.class);
    }

}
