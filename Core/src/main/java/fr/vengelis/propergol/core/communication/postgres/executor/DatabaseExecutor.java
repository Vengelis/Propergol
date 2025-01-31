package fr.vengelis.propergol.core.communication.postgres.executor;

import fr.vengelis.propergol.core.communication.postgres.PostgreCommunicationSystem;

import java.sql.Connection;

/**
 * Class to execute SQL request
 */
public class DatabaseExecutor {

    /**
     * Execute a SQL query that return a value
     *
     * @param executor Executor
     * @param <T>      Type of value desired
     * @return Instance of type desired obtained by the request
     */
    public static <T> T executeQuery(PostgreCommunicationSystem connector, QueryExecutor<T> executor) throws Exception {
        T value = null;
        Connection connection = connector.getConnection();
        value = executor.perform(connection);
        return value;
    }

    public static void executeVoidQuery(PostgreCommunicationSystem connector, QueryVoidExecutor executor) throws Exception {
        Connection connection = connector.getConnection();
        executor.perform(connection);
    }

}

