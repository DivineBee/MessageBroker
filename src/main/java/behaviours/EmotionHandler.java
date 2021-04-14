package behaviours;
/**
 * @author Beatrice V.
 * @created 15.02.2021 - 10:32
 * @project ActorProg1
 */

import actor.model.Actor;
import actor.model.Behaviour;
import actor.model.DeadException;
import actor.model.Supervisor;
import utilities.data.analytics.DataWithAnalytics;
import utilities.data.analytics.DataWithId;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import static behaviours.JSONBehaviour.user;

public class EmotionHandler implements Behaviour<DataWithId> {
    // key-value pairs for emotions with their points
    public static final HashMap<String, Integer> emotionsMap = new HashMap<String, Integer>();

    public static String[] emotionWordsArray = null;

    public EmotionHandler(String pathToEmotionDbFile) throws IOException {
        //
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(pathToEmotionDbFile));

        // while we have something to read
        while ((line = reader.readLine()) != null) {
            // remove whitespaces from beginning and end, replace multiple whitespaces occurences with only one
            line = line.trim().replaceAll("\\s+", " ");

            // decompose words from line into inner array
            String[] parts = line.split(" ");

            // if there are only 2 items then first is key and second is value
            // and attach them to map
            if (parts.length == 2)
                emotionsMap.put(parts[0], Integer.parseInt(parts[1]));

            // if more than 2 items then concatenate all the items except last one
            // and add whitespace back
            else if (parts.length >= 3) {
                String key = "";
                for (int i = 0; i < parts.length - 1; i++)
                    if (key.isEmpty())
                        key = key.concat(parts[i]);
                    else
                        key = key.concat(" " + parts[i]);

                Integer value = Integer.parseInt(parts[parts.length - 1]);
                emotionsMap.put(key, value);
            } else
                System.out.println("ignoring line: " + line);
        }

        reader.close();

        Set setOfKeys = emotionsMap.keySet();
        emotionWordsArray = new String[setOfKeys.size()];
        setOfKeys.toArray(emotionWordsArray);
    }

    // method which calculates the score adding up occurrences of words in the tweet
    // from the emotionsMap
    public static int getEmotionScore(String tweet) {
        //  emotion score
        int score = 0;

        // transform tweet to lower case for analysis
        String lowerCaseTweet = tweet.toLowerCase();

        for (String word : emotionWordsArray)
            if (lowerCaseTweet.contains(word))
                score += amountOfEmotionWordAppearancesInTweet(tweet, word) * emotionsMap.get(word);

        if (score < 0)
            if (score > -3)
                System.out.println("\n" + user + ": IS SLIGHTLY SAD");
            else if (score > -7)
                System.out.println("\n" + user + ": IS SAD");
            else
                System.out.println("\n" + user + ": IS VERY SAD");
        else if (score > 0)
            if (score < 3)
                System.out.println("\n" + user + ": IS SLIGHTLY HAPPY");
            else if (score < 7)
                System.out.println("\n" + user + ": IS HAPPY");
            else
                System.out.println("\n" + user + ": IS VERY HAPPY");

        return score;
    }

    // finds how many time emotion word has appeared in text
    public static int amountOfEmotionWordAppearancesInTweet(String tweet, String emotionWord) {
        // init fragment for taking emotion words and count their appearances in text
        String reviewableFragment = "";
        int counter = 0;

        // find word appearances by array-like detection through String
        for (int startIndex = 0; startIndex < tweet.length() - emotionWord.length(); startIndex++) {
            int endingIndex = startIndex + emotionWord.length();
            if (startIndex != 0 && endingIndex != tweet.length())
                if (verifyWordBounds(tweet.charAt(startIndex - 1), tweet.charAt(endingIndex))) {
                    reviewableFragment = tweet.substring(startIndex, endingIndex);

                    //System.out.println("HERE " + reviewableFragment + " | " + emotionWord);
                    if (reviewableFragment.equalsIgnoreCase(emotionWord))
                        counter++;
                }
            else if (endingIndex != tweet.length() - 1)
                if (verifyWordOneWayBound(tweet.charAt(endingIndex + 1))) {
                    reviewableFragment = tweet.substring(startIndex, endingIndex);
                    if (reviewableFragment.equalsIgnoreCase(emotionWord))
                        counter++;
                }
        }
        return counter;
    }

    // true if word is limited by shown starting and ending characters, false if not
    public static boolean verifyWordBounds(char start, char end) {
        if (start == ' ' || start == '.' || start == ',' || start == '!' || start == '?' || start == ';' || start == '#')
            return end == ' ' || end == '.' || end == ',' || end == '!' || end == '?' || end == ';';

        return false;
    }

    // true if bound has one of the shown characters, false if not
    public static boolean verifyWordOneWayBound(char bound) {
        return bound == ' ' || bound == '.' || bound == ',' || bound == '!' || bound == '?' || bound == ';';
    }

    // Mapping of emotions to keys and their points as values.
    // Here is taken into consideration that some emotions are composed from more
    // than one word (e.g dont like)

    /**
     * In onReceive() method, a chunk of data with unique id is received and
     * a transmittable fragment is formed which will get the id of the data
     * and will calculate and pass tweets' emotion score to the aggregator.
     * @param self Actor
     * @param dataWithId message which contains unique id per chunk
     * @throws DeadException
     */
    @Override
    public boolean onReceive(Actor<DataWithId> self, DataWithId dataWithId) throws DeadException {
        // A new transmittable fragment is initialized
        DataWithAnalytics transmittableFragment = new DataWithAnalytics();
        // Then it gets the unique id of the fragment
        transmittableFragment.setId(dataWithId.getId());
        // and then sets the emotion score for that fragment
        transmittableFragment.setEmotionScore(getEmotionScore(dataWithId.getTweet()));
        // which later is transmitted to aggregator
        Supervisor.sendMessage("aggregator", transmittableFragment);

        return true;
    }

    @Override
    public void onException(Actor<DataWithId> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}