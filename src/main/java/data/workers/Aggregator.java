package data.workers;

import actor.model.Actor;
import actor.model.Behaviour;
import actor.model.Supervisor;
import utilities.data.analytics.DataWithAnalytics;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Beatrice V.
 * @created 12.03.2021 - 10:20
 * @project ActorProg2
 */
public class Aggregator implements Behaviour<DataWithAnalytics> {
    // local storage for incoming fragments
    HashMap<UUID, DataWithAnalytics> localHashMap;

    // constructor
    public Aggregator() {
        localHashMap = new HashMap<>();
    }

    // Aggregator receives and then combines all the fragments which arrive together and merge them in one chunk
    // which will be sent to the sink.
    @Override
    public boolean onReceive(Actor<DataWithAnalytics> self, DataWithAnalytics dataAnalyticsFragment) throws Exception {
        // check if there is such an entry in the local hashmap. If so, then the execution of the
        // code inside the if starts
        if (localHashMap.get(dataAnalyticsFragment.getId()) != null) {
            // since we have already checked and found such an entry with such an id in the hashmap, we pull it out
            // to perform operations on it
            DataWithAnalytics record = localHashMap.get(dataAnalyticsFragment.getId());
            // we check what data is in the transmitted fragment and transfer it to this record
            checkData(dataAnalyticsFragment, record);
            // then check the data for integrity, if it passes the check then it can be sent
            // to the sink and removed from local map
            if (record.checkForIntegrity()) {
                Supervisor.sendMessage("sink", record);
                localHashMap.remove(record);
            }
        } else {
            // else just create new record and place new incoming data
            DataWithAnalytics newRecord = new DataWithAnalytics();
            newRecord.setId(dataAnalyticsFragment.getId());
            checkData(dataAnalyticsFragment, newRecord);
            localHashMap.put(dataAnalyticsFragment.getId(), newRecord);
        }
        return true;
    }

    // Check what data is in the transmitted fragment and transfer it to this record
    public void checkData(DataWithAnalytics dataAnalyticsFragment, DataWithAnalytics record) {
        if (dataAnalyticsFragment.getTweet() != null)
            record.setTweet(dataAnalyticsFragment.getTweet());

        if (dataAnalyticsFragment.getEmotionScore() != null)
            record.setEmotionScore(dataAnalyticsFragment.getEmotionScore());

        if (dataAnalyticsFragment.getEmotionRatio() != null)
            record.setEmotionRatio(dataAnalyticsFragment.getEmotionRatio());

        if (dataAnalyticsFragment.getUser() != null)
            record.setUser(dataAnalyticsFragment.getUser());

        if (dataAnalyticsFragment.getUserRatio() != null)
            record.setUserRatio(dataAnalyticsFragment.getUserRatio());
    }

    @Override
    public void onException(Actor<DataWithAnalytics> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}
