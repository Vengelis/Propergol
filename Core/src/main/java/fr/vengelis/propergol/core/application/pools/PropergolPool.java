package fr.vengelis.propergol.core.application.pools;

public class PropergolPool {

    private final int id;
    private String name;

    public PropergolPool(int id) {
        this.id = id;
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
