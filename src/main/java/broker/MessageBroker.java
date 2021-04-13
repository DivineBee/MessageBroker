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

public class MessageBroker {
    //  numbering of current available actor for work
    private static int currentAvailableActor = 1;

    //  list of all subscribers with topics to which they are subscribed
    private static final HashMap<String, List<Integer>> subscribersToTopics = new HashMap<>();

    //  actor's behavior for receiving message
    private static final Behaviour<Socket> receivingMessageBehavior = new Behaviour<Socket>() {

        @Override
        public boolean onReceive(Actor<Socket> self, Socket msg) throws Exception {
            return true;
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
