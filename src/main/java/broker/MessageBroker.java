package broker;

import actor.model.*;
import tcp.TcpClient;
import tcp.TcpServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**\
 *  Class that is responsible for work of pub/sub connection principle.
 *  Sub connects to the server, requesting for list of topics. This sub is registered conform topics that it requested.
 * When there is new message from publisher, broker checks topic of message and sends it to all subscribers that
 * requested for this topic. If there is not such topic, then sub waits until messages with such topic will appear.
 *  Pub connects to the server, showing that it wants to publish messages. Broker opens stream for him and waits for
 * them. When broker receives message, he checks topic of the message and sends it to all subs for this topic.
 */
public class MessageBroker {
    //  numbering of current available actor for work
    private static int currentAvailableActor = 1;

    //  list of all subscribers with topics to which they are subscribed
    private static final HashMap<String, List<Integer>> subscribersToTopics = new HashMap<>();

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
            while(true) {
                //  set stream for input from client
                InputStream inputStream = msg.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                //  transform obtained data to processable object
                Map<String, Object> incomingData = (Map<String, Object>) objectInputStream.readObject();
                System.out.println(incomingData.get("topic") + incomingData.toString());

                //  if client ends messaging, kill his actor
                if(incomingData.get("topic").equals(CustomStringTopic.END_OF_MESSAGING)) {
                    self.die();
                    return false;
                }

                //  if this is new subscriber, then add actor for him and set to which topics he is subscribing
                if(incomingData.get("topic").equals(CustomStringTopic.SUBSCRIBING)) {
                    List<String> themesList = getListFromObj(incomingData.get("topics_to_sub"));

                    //  if there is no such theme yet, create one for sub and wait for info to come
                    for (String theme : themesList) {
                        if (!subscribersToTopics.containsKey(theme))
                            subscribersToTopics.put(theme, new ArrayList<>());
                        subscribersToTopics.get(theme).add((Integer) incomingData.get("port"));
                    }

                    //  establish connection for sending data to sub
                    TcpClient tcpClient = new TcpClient("127.0.0.1", (Integer) incomingData.get("port"));
                    ActorFactory.createActor("TcpClient_" + incomingData.get("port"), tcpClient.getClientBehaviour());

                    return false;
                }

                //  check if this topic is in list of topics, else add this one to topic list
                if(!subscribersToTopics.containsKey(incomingData.get("topic")))
                    subscribersToTopics.put((String) incomingData.get("topic"), new ArrayList<>());

                //  publish message to all submitted for this topic subscribers
                List<Integer> listOfSubs = subscribersToTopics.get(incomingData.get("topic"));
                publish(listOfSubs, incomingData);
            }
        }

        //  restart actor if error was thrown
        @Override
        public void onException(Actor<Socket> self, Exception e) {
            System.out.println("Listening error");
            self.die();
        }
    };

    public static void main(String[] args) throws IOException, DeadException {
        //  establish server waiting for connections
        TcpServer tcpServer = new TcpServer(3000);
        Socket socket = null;

        //  append all new sockets to new actors and wait for data as sub
        subscribe(socket, tcpServer);
    }

    /**
     * open connection for each incoming request and set actor responsible for it
     * @param socket stream of connections
     * @param tcpServer server info
     * @throws DeadException if actor died or there is inner actor system failure
     */
    private static void subscribe(Socket socket, TcpServer tcpServer) throws DeadException {
        while(true) {
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
     * gets list of Strings from incoming objects
     * @param object object that can be transformed to the list
     * @return list of Integers from object
     */
    private static List<String> getListFromObj (Object object) {
        return (List<String>) object;
    }

    /**
     * send data to all subscribers of this topic
     * @param listOfSubscribers list of subscribers
     * @param dataToPublish data that must be sent to the subscribers
     * @throws DeadException
     */
    private static void publish(List<Integer> listOfSubscribers, Map<String, Object> dataToPublish) throws DeadException {
        if(listOfSubscribers != null)
            if(listOfSubscribers.size() > 0)
                for (Integer subscriber : listOfSubscribers)
                    Supervisor.sendMessage("TcpClient_" + subscriber, dataToPublish);
    }
}
