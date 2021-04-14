package behaviours;

import actor.model.*;
import work.SSEClient;

import java.io.EOFException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Beatrice V.
 * @created 08.02.2021 - 18:39
 * @project ActorProg1
 */
public class SSEClientBehaviour implements Behaviour<String> {

    private JSONBehaviour jsonBehaviour;

    public SSEClientBehaviour() {
        this.jsonBehaviour = new JSONBehaviour();
    }

    // method responsible for sse stream handling and passing to next actor for processing
    @Override
    public boolean onReceive(Actor<String> self, String urlPath) throws Exception {
        try {
            // set url for the stream
            URL sseUrl = new URL(urlPath);
            // create instance of client class
            SSEClient client = new SSEClient(sseUrl, evt -> {
                String data = evt.data;

                // if data contains panic message then actor is self-killing
                if (data.contains("{\"message\": panic}")) {
                    System.out.println("Actor died x_x");
                    self.die();
                }
                // if the data is available and readable then create new actor which will
                // do next steps and will process the data from the stream
                if (!data.isEmpty()) {
                    ActorFactory.createActor("jsonHandler", jsonBehaviour);
                    Supervisor.sendMessage("jsonHandler", data);
                }
            });
            client.connect();
            self.sleepActor();
        } catch (EOFException | MalformedURLException e) {
            System.out.println("the stream ended!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // if exception encountered then actor will die
    @Override
    public void onException(Actor<String> self, Exception exc) {
        exc.printStackTrace();
        self.die();
    }
}
