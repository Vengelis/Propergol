package fr.vengelis.propergol.monitor;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("home")
public class RepoConfig {

    private int port = 8080;
    private String apiKey;

    // Getters et Setters
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
