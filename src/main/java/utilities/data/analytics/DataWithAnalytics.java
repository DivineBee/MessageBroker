package utilities.data.analytics;

import java.util.UUID;

/**
 * @author Beatrice V.
 * @created 12.03.2021 - 11:41
 * @project ActorProg2
 */

// This class contains all necessary data which can be passed to aggregator
// but also we admit that not full-fledged ones may come from different actors (objects which
// don't contain whole information but so called "fragments of data")
public class DataWithAnalytics {
    private UUID id;
    private String tweet;
    private String user;
    private Double emotionRatio;
    private Double userRatio;
    private Integer emotionScore;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public Double getEmotionRatio() {
        return emotionRatio;
    }

    public void setEmotionRatio(Double emotionRatio) {
        this.emotionRatio = emotionRatio;
    }

    public Integer getEmotionScore() {
        return emotionScore;
    }

    public void setEmotionScore(Integer emotionScore) {
        this.emotionScore = emotionScore;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Double getUserRatio() {
        return userRatio;
    }

    public void setUserRatio(Double userRatio) {
        this.userRatio = userRatio;
    }

    @Override
    public String toString() {
        return "DataWithAnalytics{" +
                "id=" + id +
                ", tweet='" + tweet + '\'' +
                ", user='" + user + '\'' +
                ", emotionRatio=" + emotionRatio +
                ", userRatio=" + userRatio +
                ", emotionScore=" + emotionScore +
                '}';
    }

    //  check if record is full and if it can be transmitted further
    public boolean checkForIntegrity() {
        return this.tweet != null && this.emotionRatio != null && this.emotionScore != null && this.user != null && this.userRatio != null;
    }
}