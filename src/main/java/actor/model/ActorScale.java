package actor.model;

import static actor.model.Actor.sleepActor;
import static actor.model.ActorFactory.actorPool;

/**
 * @author Beatrice V.
 * @created 16.02.2021 - 15:51
 * @project ActorProg1
 */
public class ActorScale {
    public static String HELPER_NAME = "helper"; // prefix which will be added to helper actor's name
    static final short MAX_AMOUNT_OF_ACTORS = 100; // Maximal amount of actors considering current PC's specs

    // Creates helper for master actor or for another existing helper if it needs it.
    public static boolean createHelper(String idMasterActor, Behaviour masterBehaviour, short masterMaxMessages) {
        // check if the actor pool size is not full else deny the request and do not create helper
        if (actorPool.size() > ActorScale.MAX_AMOUNT_OF_ACTORS)
            return false;

        sleepActor();

        // else if everything is fine and actorPool is available then create new helper
        String idHelper = idMasterActor + HELPER_NAME;
        Actor actor = new Actor(idHelper, masterBehaviour);

        actor.setMaxMailboxSize(masterMaxMessages);
        // helper is killing itself once it's finishing the task, but if it's master, it can not do that
        // even if he had finished his job.
        actor.isMaster = false;
        actorPool.put(idHelper, actor);
        return true;
    }
}
