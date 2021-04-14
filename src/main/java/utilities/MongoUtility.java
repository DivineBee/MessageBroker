package utilities;

/**
 * @author Beatrice V.
 * @created 13.03.2021 - 18:14
 * @project ActorProg2
 */

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import utilities.data.analytics.DataWithAnalytics;

import java.util.List;

public class MongoUtility {
    // initialize up mongo client
    private MongoClient client;
    // initialize the database name
    private MongoDatabase database;
    // initialize the collection
    private MongoCollection<Document> collection;

    // Establish connection to database
    public void establishConnectionToDB(String host, int port, String databaseName) {
        client = new MongoClient(host, port);
        database = client.getDatabase(databaseName);
    }

    // Establish connection to collection of database
    public void establishConnectionToCollection(String collectionName) {
        collection = database.getCollection(collectionName);
    }

    // Update elements if are present or insert if there are no such records in db
    public void insertDataToDB(List<DataWithAnalytics> dataRecords) throws Exception {
        if (dataRecords.size() > 0) {
            // establish connection to the tweets collection which will contain
            // id, tweet, emotion ratio and score. Then insert it to collection
            establishConnectionToCollection("tweets");
            for (DataWithAnalytics currentRecord : dataRecords) {
                Document tweetDoc = new Document();
                tweetDoc.put("id", currentRecord.getId());
                tweetDoc.put("tweet", currentRecord.getTweet());
                tweetDoc.put("emotionRatio", currentRecord.getEmotionRatio());
                tweetDoc.put("score", currentRecord.getEmotionScore());
                collection.insertOne(tweetDoc);
            }
            // establish connection to the users collection which will contain
            // id, user, user ratio and then insert it to collection
            establishConnectionToCollection("users");
            for (DataWithAnalytics currentRecord : dataRecords) {
                Document userDoc = new Document();
                userDoc.put("id", currentRecord.getId());
                userDoc.put("user", currentRecord.getUser());
                userDoc.put("userRatio", currentRecord.getUserRatio());
                collection.insertOne(userDoc);
            }
        } else
            throw new Exception("empty list sent to collection");
    }
}
