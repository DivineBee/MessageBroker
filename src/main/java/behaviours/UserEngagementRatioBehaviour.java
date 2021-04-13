package behaviours;

import actor.model.Actor;
import actor.model.Behaviour;
import actor.model.Supervisor;
import utilities.data.analytics.DataWithAnalytics;
import utilities.data.analytics.DataWithId;

/**
 * @author Beatrice V.
 * @created 14.03.2021 - 11:25
 * @project ActorProg2
 */
public class UserEngagementRatioBehaviour implements Behaviour<DataWithId> {
    /**
     * This method calculates the engagement ratio for user by getting in the message
     * the data it needs to process(followers, friends and statuses count) and then calculates
     * the engagement ratio which will be sent to aggregator
     * @param self
     * @param dataWithId
     * @throws Exception
     */
    @Override
    public boolean onReceive(Actor<DataWithId> self, DataWithId dataWithId) throws Exception {
        // initialize variable which will store the ratio
        double userEngagementRatio = 0;
        try {
            // calculate the engagement ratio by formula followers-friends/statuses
            userEngagementRatio = (double) (dataWithId.getUserFollowersCount() - dataWithId.getFriendsCount()) / dataWithId.getStatusesCount();
        } catch (ArithmeticException|NullPointerException e) {
            // in case he has followers but don't have any statuses. suspicious account
            System.err.println("Can't calculate ratio -> 0 statuses");
        }

        // Initialize new fragment and insert the engagement ratio
        DataWithAnalytics transmittableFragment = new DataWithAnalytics();
        // get the incoming data id
        transmittableFragment.setId(dataWithId.getId());
        // append calculated user ratio
        transmittableFragment.setUserRatio(userEngagementRatio);
        // send the fragment with user's ratio to aggregator
        Supervisor.sendMessage("aggregator", transmittableFragment);
        return true;
    }

    @Override
    public void onException(Actor<DataWithId> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}