package actor.model;

/**
 * @author Beatrice V.
 * @created 05.02.2021 - 17:28
 * @project ActorProg1
 */

// General interface which will be used for creating new Behaviours
// all actor's behaviours must implement these methods and define their own logic
// This conforms to SOLID's second principle (Open closed principle) which is
// closed for modification but open for extension(you can see the behaviours package
// for more examples)
public interface Behaviour<Message> {

    // defines what the actor should do with the received message
    boolean onReceive(Actor<Message> self, Message message) throws Exception;

    // informs about death of the actor
    void onException(Actor<Message> self, Exception exc);
}
