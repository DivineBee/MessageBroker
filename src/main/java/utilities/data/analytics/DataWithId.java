package utilities.data.analytics;

import java.util.UUID;

/**
 * @author Beatrice V.
 * @created 08.03.2021 - 12:02
 * @project ActorProg2
 */

// Contains primary information for starting processing. Since this type of information is sufficiently typed
// and its used by multiple actors then we use it for primary transmission. For example to emotion score and ratio.
// Because emotion score and ratio receive only one type of data but aggregator receive ambiguous type of data(of
// different kind)
public class DataWithId {
    private UUID id;
    private String tweet;
    private String user;

    private int favouritesCount;
    private int retweetsCount;
    private int retweetFollowersCount;

    private int friendsCount;
    private int userFollowersCount;
    private int statusesCount;

    public DataWithId(String tweet, String user, int favouritesCount, int retweetsCount, int retweetFollowersCount,
                      int friendsCount, int userFollowersCount, int statusesCount) throws Exception {
        if (!tweet.isEmpty()) {
            this.tweet = tweet;
            this.id = UUID.randomUUID();
            this.favouritesCount = favouritesCount;
            this.retweetsCount = retweetsCount;
            this.retweetFollowersCount = retweetFollowersCount;
            this.user = user;
            this.friendsCount = friendsCount;
            this.userFollowersCount = userFollowersCount;
            this.statusesCount = statusesCount;
        } else
            throw new Exception("Tweet is empty or no tweet attached");
    }

    public DataWithId(String user, int friendsCount, int userFollowersCount, int statusesCount) throws Exception {
        if (!user.isEmpty()) {
            this.user = user;
            this.id = UUID.randomUUID();
            this.friendsCount = friendsCount;
            this.userFollowersCount = userFollowersCount;
            this.statusesCount = statusesCount;
        } else
            throw new Exception("Tweet is empty or no tweet attached");
    }

    public DataWithId() {
    }

    public UUID getId() {
        return id;
    }

    public String getTweet() {
        return tweet;
    }

    public String getUser() {
        return user;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public int getRetweetsCount() {
        return retweetsCount;
    }

    public int getRetweetFollowersCount() {
        return retweetFollowersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public int getUserFollowersCount() {
        return userFollowersCount;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    @Override
    public String toString() {
        return "DataWithId{" +
                "id=" + id +
                ", tweet='" + tweet + '\'' +
                ", user='" + user + '\'' +
                ", favouritesCount=" + favouritesCount +
                ", retweetsCount=" + retweetsCount +
                ", retweetFollowersCount=" + retweetFollowersCount +
                ", friendsCount=" + friendsCount +
                ", userFollowersCount=" + userFollowersCount +
                ", statusesCount=" + statusesCount +
                '}';
    }
}
