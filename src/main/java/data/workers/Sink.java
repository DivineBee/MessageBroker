package data.workers;

import actor.model.Actor;
import actor.model.Behaviour;
import utilities.MongoUtility;
import utilities.data.analytics.DataWithAnalytics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Beatrice V.
 * @created 12.03.2021 - 10:20
 * @project ActorProg2
 */
public class Sink implements Behaviour<DataWithAnalytics> {
    // initialize custom class responsible for mongoDB data connection
    private MongoUtility mongoUtility;
    // initialize values which will be used to calculate time
    private long start = 0;
    private long end = 0;
    // flag for setting the state
    private boolean isSent = true;
    // list which holds records which will be sent to database
    private List<DataWithAnalytics> recordsToDB = new ArrayList<DataWithAnalytics>();
    // here is set the batch size(how many records can be sent at once to db)
    private static final int BATCH_SIZE = 128;

    // constructor which gets the required fields for establishing connection with db
    public Sink(String host, int port, String databaseName) {
        mongoUtility = new MongoUtility();
        mongoUtility.establishConnectionToDB(host, port, databaseName);
    }

    // Inside on receive method the backpressure strategy is implemented
    @Override
    public boolean onReceive(Actor<DataWithAnalytics> self, DataWithAnalytics msg) throws Exception {
        // inside this if the check of time is performed(if it didn't get over 200ms)
        if (isSent) {
            // get current time
            start = System.currentTimeMillis();
            // calculate 200 ms from starting of timer
            end = (long) (start + 0.2 * 1000);
            // set flag to false (the timer will finish)
            isSent = false;
        }
        // if the time is up or the maximum batch size is reached (whichever occurs first)
        if (System.currentTimeMillis() >= end || recordsToDB.size() >= BATCH_SIZE) {
            // insert records to DB
            mongoUtility.insertDataToDB(recordsToDB);
            // create a list to store other records
            recordsToDB = new ArrayList<>();
            // set flag to true
            isSent = true;
        }

        // add another incoming message with data to the general records
        recordsToDB.add(msg);
        System.out.println(msg);

        return true;
    }

    @Override
    public void onException(Actor<DataWithAnalytics> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}
