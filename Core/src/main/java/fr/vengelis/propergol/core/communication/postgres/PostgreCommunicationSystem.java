package fr.vengelis.propergol.core.communication.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.vengelis.propergol.core.communication.CommunicationSystem;
import fr.vengelis.propergol.core.communication.InstructionRequest;
import fr.vengelis.propergol.core.communication.InstructionResponse;
import fr.vengelis.propergol.core.communication.postgres.executor.DatabaseExecutor;
import fr.vengelis.propergol.core.communication.postgres.executor.QueryExecutor;
import fr.vengelis.propergol.core.communication.postgres.executor.QueryVoidExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class PostgreCommunicationSystem extends CommunicationSystem {

    private static PostgreCommunicationSystem INSTANCE;
    private HikariDataSource dataSource;
    private String host, user, password, database;
    private int port;

    private PostgreCommunicationSystem(String host, String user, String password, String database, int port) {
        super(System.POSTGRE);
        INSTANCE = this;
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
        initializeDataSource();
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
        config.setUsername(user);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setDriverClassName("org.postgresql.Driver");

        for (String s : Arrays.asList(
                "useServerPrepStmts", "useLocalSessionState", "rewriteBatchedStatements", "cacheResultSetMetadata",
                "cacheServerConfiguration", "elideSetAutoCommits", "maintainTimeStats")) {
            config.addDataSourceProperty(s, true);
        }
        config.addDataSourceProperty("maintainTimeStats", false);

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public static PostgreCommunicationSystem create(String host, String user, String password, String database, int port) throws Exception {
        if(INSTANCE != null)
            throw new Exception("HikariConnector already exist");
        if(host == null || host.length() == 0) throw new IllegalArgumentException("Database host cannot be null/empty");
        if(user == null || user.length() == 0) throw new IllegalArgumentException("Database host cannot be null/empty");
        if(password == null || password.length() == 0) throw new IllegalArgumentException("Database host cannot be null/empty");
        if(database == null || database.length() == 0) throw new IllegalArgumentException("Database host cannot be null/empty");

        return new PostgreCommunicationSystem(host, user, password, database, port);
    }

    public static PostgreCommunicationSystem get() {
        return INSTANCE;
    }

    public void close() {
        if(!this.dataSource.isClosed())
            this.dataSource.close();
    }

    @Override
    public InstructionResponse<?> send(InstructionRequest<?> request) {
        if (getServiceStatus().equals(Status.RETAINED)) {
            return sendToRetainedService(request);
        }

        try {
            if (request.getService() instanceof PostgreService service) {
                switch (service) {

                    // TODO : Faire la sauvegarde de la requete vers le service de retention si ça fail !

                    case QUERY -> {
                        Object result = DatabaseExecutor.executeQuery(this, (QueryExecutor<?>) request.getData());
                        return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, result);
                    }
                    case VOID_QUERY -> {
                        DatabaseExecutor.executeVoidQuery(this, (QueryVoidExecutor) request.getData());
                        return new InstructionResponse<>(InstructionResponse.SystemResult.SENDED, "Executed successfully");
                    }
                    default -> throw new IllegalArgumentException("Unsupported PostgreSQL service");
                }
            } else {
                throw new IllegalArgumentException("Invalid PostgreSQL service type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return sendToRetainedService(request);
        }
    }

    @Override
    protected void reconnect() {
        close();
        initializeDataSource();
    }

    @Override
    protected boolean isReconnected() {
        return tryHelloWorldSuccess();
    }

    @Override
    protected boolean tryHelloWorldSuccess() {
        try (Connection connection = getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected void boot() {

    }
}
