package fr.vengelis.propergol.repository;


import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.application.ApplicationType;

public class Repository {

    public static void main(String[] args) {
        Core core = new Core(ApplicationType.REPOSITORY);
        core.boot();

    }

}
