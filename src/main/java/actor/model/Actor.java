package actor.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Beatrice V.
 * @created 04.02.2021 - 11:14
 * @project ActorProg1
 */
public class Actor<Message> implements Runnable {

    private volatile boolean isThreadAlive; // is current actor alive
    private boolean isHelperCreated = false; // does actor created a helper
    boolean isMaster = true; // is actor the master

    private short MAX_MAILBOX_SIZE = 10000; // if actor's mailbox will be full it will create helper to handle upcoming messages
    private String idActor; // actor identifier or name

    private volatile BlockingQueue<Message> mailbox; // mailbox of incoming messages
    private final Behaviour<Message> behavior; // interface which contains how the actor should process the message

    // creates actor with actor's id and behaviour
    public Actor(String idActor, Behaviour<Message> behavior) {
        this.behavior = behavior;
        this.mailbox = new LinkedBlockingQueue<>();
        this.idActor = idActor;
        isThreadAlive = true;
        new Thread(this).start();
    }

    // Getters & Setters
    public String getName() {
        return this.idActor;
    }

    Behaviour<Message> getBehavior() {
        return this.behavior;
    }

    void setMaxMailboxSize(short MAX_MAILBOX_SIZE) {
        this.MAX_MAILBOX_SIZE = MAX_MAILBOX_SIZE;
    }

    // Actor die/kill method which also informs the Supervisor upon his death
    public void die() {
        // make thread dead(false)
        isThreadAlive = false;
        // free up space and clear mailbox
        this.mailbox.clear();
        try {
            // masters can't die so they will respawn
            Supervisor.actorDie(this.idActor, this.isMaster);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // receive message
    public boolean takeMessage(Message message) throws DeadException {
        if (!isThreadAlive)
            throw new DeadException("Thread is dead");

        short currentMailboxSize = (short) mailbox.size();

        // check if a helper wasn't already created and if mailbox size is over capacity, then request for helper
        if (!isHelperCreated && (currentMailboxSize > MAX_MAILBOX_SIZE))
            // though actor scaling a helper is created with same attributes as parent to handle the messages
            if (ActorScale.createHelper(this.idActor, this.behavior, this.MAX_MAILBOX_SIZE))
                isHelperCreated = true;

        // if there is a helper and mailbox is full then retransmit message to helper
        if (isHelperCreated && currentMailboxSize > MAX_MAILBOX_SIZE)
            // master tries to send the message to his helper
            if (!Supervisor.sendMessage(this.idActor + ActorScale.HELPER_NAME, message))
                isHelperCreated = false;

        // add message to the mailbox
        return mailbox.offer(message);
    }

    // process mailbox and check if killing is required
    @Override
    public void run() {
        try {
            // take message from queue
            while (behavior.onReceive(this, mailbox.take()) && isThreadAlive)
                // destroy and stop helper if it's done with the task
                if (!isMaster && mailbox.size() == 0) {
                    die();
                    break;
                }
        } catch (InterruptedException exception) {
            behavior.onException(this, exception);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method for random actor sleep from 50 to 500 ms
    public static void sleepActor() {
        try {
            short max = 500;
            short min = 50;
            int range = max - min + 1;
            Thread.sleep((long) (Math.random() * range) + min);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}