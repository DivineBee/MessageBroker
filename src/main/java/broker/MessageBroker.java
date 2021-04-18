package broker;

import actor.model.*;
import tcp.TcpClient;
import tcp.TcpServer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * Class that is responsible for work of pub/subscriber connection principle.
 * Sub connects to the server, requesting for list of topics. This subscriber is registered conform topics that it requested.
 * When there is new message from publisher, broker checks topic of message and sends it to all subscribers that
 * requested for this topic. If there is not such topic, then subscriber waits until messages with such topic will appear.
 * Pub connects to the server, showing that it wants to publish messages. Broker opens stream for him and waits for
 * them. When broker receives message, he checks topic of the message and sends it to all subs for this topic.
 */
public class MessageBroker {
    //  numbering of current available actor for work
    private static int currentAvailableActor = 1;

    //  list of all subscribers with topics to which they are subscribed
    private static final HashMap<String, List<Integer>> topicsSubscribers = new HashMap<>();

    //Queue for each topic of durable queues
    Queue<Map<String, Object>> tweetQueue = new PriorityQueue<>();
    Queue<Map<String, Object>> userQueue = new PriorityQueue<>();

    // when receive message from publisher then put it into according queue
    // save the information from queue until it reaches 1000, once it reaches 1000 the rewrite the queue
    // if a subscriber connected then send him the information from the queue

    public static void main(String[] args) throws IOException, DeadException {
        //  establish server waiting for connections
        TcpServer tcpServer = new TcpServer(3000);
        Socket socket = null;

        //  append all new sockets to new actors and wait for data as subscriber
        subscribe(socket, tcpServer);
    }

    //  actor's behavior for receiving message
    private static final Behaviour<Socket> receivingMessageBehavior = new Behaviour<Socket>() {
        /**
         * receive message and process it
         * @param self Self reference
         * @param msg Message that is sent to the actor
         * @return successful or not operation
         * @throws Exception
         */
        @Override
        public boolean onReceive(Actor<Socket> self, Socket msg) throws Exception {
            try {
                while (true) {
                    //  set stream for input from client
                    ObjectInputStream inputStream = new ObjectInputStream(msg.getInputStream());

                    //  transform obtained data to processable object
                    Map<String, Object> incomingData = (Map<String, Object>) inputStream.readObject();

                    if (incomingData == null || incomingData.isEmpty())
                        System.err.println("empty message was received");
                    else {
                        //  if client ends messaging, kill his actor
                        if (incomingData.get("topic").equals(CustomStringTopic.END_OF_MESSAGING)) {
                            self.die();
                            return false;
                        }

                        //  if this is new subscriber, then add actor for him and set to which topics he is subscribing
                        if (incomingData.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.SUBSCRIBING)) {
                            List<String> topicList = getListFromObj(incomingData.get(CustomStringTopic.TOPICS_TO_SUB));

                            //  if there is no such topic yet, create one for subscriber and wait for info to come
                            for (String topic : topicList) {
                                if (!topicsSubscribers.containsKey(topic))
                                    topicsSubscribers.put(topic, new ArrayList<>());
                                topicsSubscribers.get(topic).add((Integer) incomingData.get("port"));
                            }

                            //  establish connection for sending data to subscriber
                            TcpClient tcpClient = new TcpClient((String) incomingData.get(CustomStringTopic.IP), (Integer) incomingData.get("port"));
                            ActorFactory.createActor("TcpClient_" + incomingData.get("port"), tcpClient.getClientBehaviour());

                            return false;
                        }

                        if (incomingData.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.PUBLISHING))
                            for (String topic : getListFromObj(incomingData.get(CustomStringTopic.TOPIC_TO_PUBLISH)))
                                if (!topicsSubscribers.containsKey(topic))
                                    topicsSubscribers.put(topic, new ArrayList<>());

                        //  publish message to all submitted for this topic subscribers
                        if (incomingData.get(CustomStringTopic.TOPIC) != null) {
                            List<Integer> subscribersList = topicsSubscribers.get(incomingData.get(CustomStringTopic.TOPIC));
                            publish(subscribersList, incomingData);
                        } else {
                            System.err.println("Message has no topic attached");
                        }
                    }
                }
            } catch (SocketException e) {
                System.err.println("Disconnected");
            }
            return true;
        }

        //  restart actor if error was thrown
        @Override
        public void onException(Actor<Socket> self, Exception e) {
            System.out.println("Listening error");
            self.die();
        }
    };

    /**
     * open connection for each incoming request and set actor responsible for it
     * @param socket stream of connections
     * @param tcpServer server info
     * @throws DeadException if actor died or there is inner actor system failure
     */
    private static void subscribe(Socket socket, TcpServer tcpServer) throws DeadException {
        while (true) {
            //  wait for new connections
            try {
                socket = tcpServer.getServerSocket().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //  create actor if there is new connection to server
            ActorFactory.createActor("TcpServer_" + currentAvailableActor, receivingMessageBehavior);
            Supervisor.sendMessage("TcpServer_" + currentAvailableActor++, socket);
        }
    }

    /**
     * send data to all subscribers of this topic
     * @param listOfSubscribers list of subscribers
     * @param dataToPublish data that must be sent to the subscribers
     * @throws DeadException
     */
    private static void publish(List<Integer> listOfSubscribers, Map<String, Object> dataToPublish) throws DeadException {
        if (listOfSubscribers != null && listOfSubscribers.size() > 0)
            for (Integer subscriber : listOfSubscribers)
                Supervisor.sendMessage("TcpClient_" + subscriber, dataToPublish);
    }

    /**
     * gets list of Strings from incoming objects
     * @param object object that can be transformed to the list
     * @return list of Integers from object
     */
    private static List<String> getListFromObj(Object object) {
        return (List<String>) object;
    }
}
