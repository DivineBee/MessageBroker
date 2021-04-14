package actor.model;

import java.util.HashMap;

/**
 * @author Beatrice V.
 * @created 16.02.2021 - 15:51
 * @project ActorProg1
 */

public class ActorFactory {
    public static final HashMap<String, Actor> actorPool = new HashMap<>(); // pool of actors(inspired by ThreadPool or DeadPool?)

    public static HashMap<String, Actor> getActorPool() {
        return actorPool;
    } // getter

    // setter
    public static void setMaxMailboxSize(String nameOfActor, short MAX_MAILBOX_SIZE) {
        actorPool.get(nameOfActor).setMaxMailboxSize(MAX_MAILBOX_SIZE);
    }

    // creates actor and save it in system with its name and behaviour
    public static void createActor(String idActor, Behaviour behavior) {
        actorPool.put(idActor, new Actor(idActor, behavior));
    }
}
