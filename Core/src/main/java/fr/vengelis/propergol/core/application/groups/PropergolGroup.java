package fr.vengelis.propergol.core.application.groups;

import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.communication.InstructionRequest;
import fr.vengelis.propergol.core.communication.InstructionResponse;
import fr.vengelis.propergol.core.communication.postgres.PostgreService;
import fr.vengelis.propergol.core.communication.postgres.executor.QueryExecutor;
import fr.vengelis.propergol.core.communication.postgres.executor.QueryVoidExecutor;
import fr.vengelis.propergol.core.communication.retention.Retention;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PropergolGroup {

    private final int id;
    private String name;

    public static PropergolGroup build(String name) {
        if(Core.get().getGroupManager().get().values().stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(name))) {
            return Core.get().getGroupManager().get().values().stream()
                    .filter(g -> g.getName().equalsIgnoreCase(name))
                    .findFirst().get();
        } else {

            // TODO : Faire un système de sauvegarde du dernier index utilisé pour éviter de refaire des requetes inutiles

            InstructionResponse<?> rep = Core.get().getPostgreCommunicationSystem().send(
                    new InstructionRequest<QueryVoidExecutor>(
                            Retention.FORGETTABLE,
                            InstructionRequest.Handler.POSTGRE,
                            PostgreService.VOID_QUERY,
                            connection -> {
                                PreparedStatement statement = connection.prepareStatement(
                                        "INSERT INTO test_table(\"data\"::text) VALUES (?)"
                                );
                                statement.setString(1, name);
                            })
            );

            if(rep.getSystemResult().equals(InstructionResponse.SystemResult.SENDED)) {
                InstructionResponse<Integer> rep2 = (InstructionResponse<Integer>) Core.get().getPostgreCommunicationSystem().send(new InstructionRequest<QueryExecutor<Integer>>(
                        Retention.FORGETTABLE,
                        InstructionRequest.Handler.POSTGRE,
                        PostgreService.QUERY,
                        connection -> {
                            PreparedStatement statement = connection.prepareStatement(
                                    "SELECT * FROM test_table WHERE data = ?"
                            );
                            statement.setString(1, name);
                            ResultSet rs = statement.executeQuery();
                            return rs.next() ? rs.getInt("id") : -1;
                        }
                ));
                if(rep2.getSystemResult().equals(InstructionResponse.SystemResult.SENDED) && rep2.getResult().isPresent()) {
                    PropergolGroup g = new PropergolGroup(rep2.getResult().get(), name);
                    Core.get().getGroupManager().register(g);
                    return g;
                }
            }
            return new PropergolGroup(-1, "Database not reachable");
        }
    }

    private PropergolGroup(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
