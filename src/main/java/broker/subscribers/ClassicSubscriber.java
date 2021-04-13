package broker.subscribers;

import actor.model.Actor;
import actor.model.Behaviour;
import broker.CustomStringTopic;
import broker.CustomSubtopic;
import java.util.HashMap;

/**
 * @author Beatrice V.
 * @created 11.04.2021 - 17:35
 * @project MessageBroker
 */
public class ClassicSubscriber {

    //  Behaviour of joining messages and recording incomplete ones for their further finishing
    private static Behaviour<HashMap<String, Object>> messageExtracterBehaviour = new Behaviour<HashMap<String, Object>>() {
        @Override
        public boolean onReceive(Actor<HashMap<String, Object>> self, HashMap<String, Object> msg) throws Exception {
            HashMap<String, Object> currentRecord = new HashMap<>();

            if (msg.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.TWEET)) {
                currentRecord.put(CustomSubtopic.ID, msg.get(CustomSubtopic.ID));
                currentRecord.put(CustomSubtopic.TWEET_TEXT, msg.get(CustomSubtopic.TWEET_TEXT));
                currentRecord.put(CustomSubtopic.EMOTION_RATIO, msg.get(CustomSubtopic.EMOTION_RATIO));
                currentRecord.put(CustomSubtopic.EMOTION_SCORE, msg.get(CustomSubtopic.EMOTION_SCORE));
            } else if (msg.get(CustomStringTopic.TOPIC).equals(CustomStringTopic.USER)) {
                currentRecord.put(CustomSubtopic.ID, msg.get(CustomSubtopic.ID));
                currentRecord.put(CustomSubtopic.USERNAME, msg.get(CustomSubtopic.USERNAME));
                currentRecord.put(CustomSubtopic.USER_RATIO, msg.get(CustomSubtopic.USER_RATIO));
            }

            if (!currentRecord.isEmpty())
                System.out.println(currentRecord);

            return true;
        }

        @Override
        public void onException(Actor<HashMap<String, Object>> self, Exception e) {
            e.printStackTrace();
            self.die();
        }
    };

}
