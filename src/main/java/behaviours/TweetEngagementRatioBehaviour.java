package behaviours;

import actor.model.Actor;
import actor.model.Behaviour;
import actor.model.Supervisor;
import utilities.data.analytics.DataWithAnalytics;
import utilities.data.analytics.DataWithId;

/**
 * @author Beatrice V.
 * @created 25.02.2021 - 21:16
 * @project ActorProg2
 */
public class TweetEngagementRatioBehaviour implements Behaviour<DataWithId> {
    /**
     * This method calculates the engagement ratio for tweet by getting in the message
     * the data it needs to process(favourites, retweets and followers count) and then calculates
     * the engagement ratio which will be sent to aggregator
     * @param self
     * @param dataWithId
     * @throws Exception
     */
    @Override
    public boolean onReceive(Actor<DataWithId> self, DataWithId dataWithId) throws Exception {
        // initialize variable which will store the ratio
        double tweetEngagementRatio = 0;
        try {
            // calculate the engagement ratio by formula favorites+retweets/followers
            tweetEngagementRatio = (double) (dataWithId.getFavouritesCount() + dataWithId.getRetweetsCount()) / dataWithId.getRetweetFollowersCount();
        } catch (ArithmeticException|NullPointerException e) {
            // in case he is some cringe guy with retweets and favourites and nobody want to follow him.
            System.err.println("Can't calculate ratio -> 0 followers");
        }
        // Initialize new fragment and insert the engagement ratio 
        DataWithAnalytics transmittableFragment = new DataWithAnalytics();
        // get the incoming data id
        transmittableFragment.setId(dataWithId.getId());
        // append calculated ratio
        transmittableFragment.setEmotionRatio(tweetEngagementRatio);
        // send the fragment with tweet's ratio to aggregator
        Supervisor.sendMessage("aggregator", transmittableFragment);
        return true;
    }

    @Override
    public void onException(Actor<DataWithId> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}
