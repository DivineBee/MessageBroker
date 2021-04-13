package tcp;

import actor.model.Actor;
import actor.model.Behaviour;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

// client-side connection module (sends requests to the server)
public class TcpClient {
    //  socket for connection
    private final Socket socket;
    // behaviour of the client for actor model
    private Behaviour<Object> clientBehaviour;

    /**
     * constructor that will define IP and port where to send requests, designed to be launched as an actor
     * @param destinationIP IP where to send requests
     * @param destinationPort Port where to send requests
     * @throws IOException
     */
    public TcpClient(String destinationIP, int destinationPort) throws IOException {
        System.out.println(destinationPort);

        //  establish socket for connection
        socket = new Socket(destinationIP, destinationPort);

        //  define behaviour
        clientBehaviour = new Behaviour<Object>() {

            /**
             * sends each received message as a request to the defined IP and port
             * @param self Self reference
             * @param msg Message that is sent to the actor and must be sent as request to IP and port
             * @return true if must be continued, false if not
             * @throws Exception
             */
            @Override
            public boolean onReceive(Actor<Object> self, Object msg) throws Exception {
                //  set output stream and send message as java-object
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(msg);

                return true;
            }

            @Override
            public void onException(Actor<Object> self, Exception e) {
                System.out.println("Error sending message from " + self.getName());
            }
        };
    }

    /**
     * set destination where to send request
     * @param destinationIp IP where to send
     * @param destinationPort port where to send
     * @throws IOException
     */
    public void setDestination(String destinationIp, int destinationPort) throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(destinationIp, destinationPort);
        socket.bind(socketAddress);
        socket.connect(socketAddress);
    }

    //  getters and setters
    public Behaviour<Object> getClientBehaviour() { return clientBehaviour; }
    public Socket getSocket() { return socket; }

    public void setClientBehaviour(Behaviour behaviour) { this.clientBehaviour = behaviour; }
}
