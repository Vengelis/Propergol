package fr.vengelis.propergol.core.application.groups;

import java.util.HashMap;

public class GroupManager {

    private final HashMap<Integer, PropergolGroup> groups = new HashMap<>();

    public void register(PropergolGroup group) {
        groups.put(group.getId(), group);
    }

    public HashMap<Integer, PropergolGroup> get() {
        return groups;
    }
}
