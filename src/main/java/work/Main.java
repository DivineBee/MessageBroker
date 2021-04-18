package work;

import actor.model.ActorFactory;
import actor.model.DeadException;
import actor.model.Supervisor;
import behaviours.EmotionHandler;
import behaviours.TweetEngagementRatioBehaviour;
import behaviours.SSEClientBehaviour;
import behaviours.UserEngagementRatioBehaviour;
import broker.CustomStringTopic;
import data.workers.Aggregator;
import data.workers.Sink;
import tcp.TcpClient;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Beatrice V.
 * @created 07.02.2021 - 19:36
 * @project ActorProg1
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // Section of initialization
        SSEClientBehaviour sseClientBehaviour = new SSEClientBehaviour();
        EmotionHandler emotionHandler = new EmotionHandler("D:\\downloads\\MessageBroker\\src\\main\\resources\\emotions.txt");
        TweetEngagementRatioBehaviour ratioBehaviour = new TweetEngagementRatioBehaviour();
        UserEngagementRatioBehaviour userRatioBehaviour = new UserEngagementRatioBehaviour();
        Aggregator aggregator = new Aggregator();
        Sink sink = new Sink("localhost", 27017, "Tweets");

        // Section of creating the actors
        ActorFactory.createActor("firstSSEClient", sseClientBehaviour);
        ActorFactory.createActor("secondSSEClient", sseClientBehaviour);

        TcpClient client = new TcpClient("127.0.0.1", 3000);
        ActorFactory.createActor("TcpClient", client.getClientBehaviour());
        handShake();

        ActorFactory.createActor("tweetEngagementRatio", ratioBehaviour);
        ActorFactory.createActor("userEngagementRatio", userRatioBehaviour);
        ActorFactory.createActor("emotionScoreCalculator", emotionHandler);
        ActorFactory.createActor("emotionHandler", emotionHandler);

        ActorFactory.createActor("aggregator", aggregator);
        ActorFactory.createActor("sink", sink);

        // Section of sending messages for reading the 2 streams
        Supervisor.sendMessage("firstSSEClient", "http://localhost:4000/tweets/1");
        Supervisor.sendMessage("secondSSEClient", "http://localhost:4000/tweets/2");
    }

    /**
     * handshake with message broker, setting topics to which client wants to subscriber
     */
    private static void handShake() throws DeadException {
        HashMap<String, Object> messageToSend = new HashMap<>();
        messageToSend.put(CustomStringTopic.TOPIC, CustomStringTopic.PUBLISHING);

        ArrayList<String> topicsList = new ArrayList<>();
        topicsList.add(CustomStringTopic.TWEET);
        topicsList.add(CustomStringTopic.USER);
        messageToSend.put(CustomStringTopic.TOPIC_TO_PUBLISH, topicsList);

        Supervisor.sendMessage("TcpClient", messageToSend);
    }
}
