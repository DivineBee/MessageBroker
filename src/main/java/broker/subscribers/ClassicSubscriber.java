package broker.subscribers;

import actor.model.*;
import broker.CustomStringTopic;
import broker.CustomSubtopic;
import tcp.TcpClient;
import tcp.TcpServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.Socket;
import java.util.Map;

/**
 * @author Beatrice V.
 * @created 11.04.2021 - 17:35
 * @project MessageBroker
 */
public class ClassicSubscriber {
    private static int subPort = 3002;
    private static String subIP = "127.0.0.1";

    //  Behaviour of joining messages and recording incomplete ones for their further finishing
    private static Behaviour<Socket> messageExtractorBehaviour = new Behaviour<Socket>() {
        @Override
        public boolean onReceive(Actor<Socket> self, Socket msg) throws Exception {
            HashMap<String, Object> currentRecord = new HashMap<>();
            try {
                while (true) {
                    // receive input stream coming from broker
                    ObjectInputStream objectInputStream = new ObjectInputStream(msg.getInputStream());

                    // read incoming data as message (object) of specific type
                    Map<String, Object> incomingMessage = (Map<String, Object>) objectInputStream.readObject();

                    // get record with info about tweet
                    if (incomingMessage.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.TWEET)) {
                        currentRecord.put(CustomSubtopic.ID, incomingMessage.get(CustomSubtopic.ID));
                        currentRecord.put(CustomSubtopic.TWEET_TEXT, incomingMessage.get(CustomSubtopic.TWEET_TEXT));
                        currentRecord.put(CustomSubtopic.EMOTION_RATIO, incomingMessage.get(CustomSubtopic.EMOTION_RATIO));
                        currentRecord.put(CustomSubtopic.EMOTION_SCORE, incomingMessage.get(CustomSubtopic.EMOTION_SCORE));
                        // get record with info about user
                    } else if (incomingMessage.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.USER)) {
                        currentRecord.put(CustomSubtopic.ID, incomingMessage.get(CustomSubtopic.ID));
                        currentRecord.put(CustomSubtopic.USERNAME, incomingMessage.get(CustomSubtopic.USERNAME));
                        currentRecord.put(CustomSubtopic.USER_RATIO, incomingMessage.get(CustomSubtopic.USER_RATIO));
                    }

                    //  show record if there is value
                    if (!currentRecord.isEmpty())
                        System.out.println(currentRecord);

                    currentRecord.clear();
                }
            } catch (IOException e) {
                System.err.println("Message Broker not responding...");
            }
            return true;
        }

        @Override
        public void onException(Actor<Socket> self, Exception e) {
            e.printStackTrace();
            self.die();
        }
    };

    public static void main(String[] args) throws IOException, DeadException {
        // init client that will send request for connection
        TcpClient tcpClient = new TcpClient("127.0.0.1", 3000);
        ActorFactory.createActor("TcpClient", tcpClient.getClientBehaviour());

        // inform broker where to send data
        handShake();

        // define port that will listen for incoming requests and messages from broker
        TcpServer tcpServer = new TcpServer(3002);
        Socket socket = null;

        // perform subscription
        subscribe(socket, tcpServer);
    }

    /**
     * subscriber to the server
     * @param socket connection to server for receiving data
     * @param tcpServer protocol for receiving data
     */
    private static void subscribe(Socket socket, TcpServer tcpServer) throws DeadException {
        while(true) {
            // catch new connection
            try {
                socket = tcpServer.getServerSocket().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // append received connection to the listener
            ActorFactory.createActor("TcpServer", messageExtractorBehaviour);
            Supervisor.sendMessage("TcpServer", socket);
        }
    }

    /**
     * handshake with message broker, setting topics to which client wants to sub
     */
    private static void handShake() throws DeadException {
        // initialize handshake message that will inform about to which topics is required subscription
        HashMap<String, Object> messageToSend = new HashMap<>();
        messageToSend.put(CustomStringTopic.TOPIC, CustomStringTopic.SUBSCRIBING);

        // here sub wants to get tweet and user data
        ArrayList<String> topicsList = new ArrayList<>();
        topicsList.add(CustomStringTopic.TWEET);
        topicsList.add(CustomStringTopic.USER);
        messageToSend.put(CustomStringTopic.TOPICS_TO_SUB, topicsList);

        // inform broker about port where to send data
        messageToSend.put(CustomStringTopic.PORT, subPort);
        messageToSend.put(CustomStringTopic.IP, subIP);
        Supervisor.sendMessage("TcpClient", messageToSend);
    }

}
