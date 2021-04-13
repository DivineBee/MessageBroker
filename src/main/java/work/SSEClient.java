package work;

import actor.model.DeadException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Beatrice V.
 * @created 05.02.2021 - 17:42
 * @project ActorFinal
 */
public class SSEClient {
    URL target;
    EventListener listener;
    boolean readEvent = true;

    public static class SSEEvent {
        public String data;
        public String type;
    }

    public interface EventListener {
        void message(SSEEvent event) throws DeadException;
    }

    public SSEClient(URL destination, EventListener listener) {
        target = destination;
        this.listener = listener;
    }

    boolean isEol(StringBuilder sb) {
        return ((sb.length() > 0) && (((sb.length() < 2) && sb.codePointAt(0) == 10) ||
                (sb.codePointAt(sb.length() - 1) == 10 && sb.codePointAt(sb.length() - 2) == 13) ||
                (sb.codePointAt(sb.length() - 1) == 10))
        );
    }

    void readEvent(InputStream in) throws IOException, DeadException {
        String eventType = "message";
        StringBuilder payLoad = new StringBuilder();

        while (readEvent) {
            StringBuilder sb = new StringBuilder();
            while (!isEol(sb)) {
                int red = in.read();
                if (red == -1)
                    throw new EOFException("end of stream");
                char c = (char) red;
                sb.append(c);
            }

            String line = sb.toString();
            if (line.startsWith(":"))
                continue;

            if (line.startsWith("data: ")) {
                payLoad.append(line.substring(6));
                continue;
            }
            if (line.startsWith("event: ")) {
                eventType = line.substring(7);
                continue;
            }
            if ("\n".equals(line) || "\r\n".equals(line))
                break;

            System.out.println("line = " + line + "<");
        }

        SSEEvent evt = new SSEEvent();
        evt.type = eventType;
        evt.data = payLoad.toString();
        listener.message(evt);
    }

    public void connect() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) target.openConnection();
        InputStream in = connection.getInputStream();
        while (true)
            readEvent(in);
    }
}