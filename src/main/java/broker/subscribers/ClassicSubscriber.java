package broker.subscribers;

import actor.model.Actor;
import actor.model.Behaviour;
import java.util.HashMap;

/**
 * @author Beatrice V.
 * @created 11.04.2021 - 17:35
 * @project MessageBroker
 */
public class ClassicSubscriber {

    private static Behaviour<HashMap<String, Object>> messageExtracterBehaviour = new Behaviour<HashMap<String, Object>>() {
        @Override
        public boolean onReceive(Actor<HashMap<String, Object>> self, HashMap<String, Object> msg) throws Exception {
            return true;
        }

        @Override
        public void onException(Actor<HashMap<String, Object>> self, Exception e) {
            e.printStackTrace();
            self.die();
        }
    };

}
