package tcp;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * tcp connection module that will behave as a server - listen for incoming requests
 */
public class TcpServer {
    //  port that will listen for requests
    private int serverPort;

    //  connection socket
    private ServerSocket serverSocket;

    //  constructor defining port of listening incoming requests
    public TcpServer(int whichPortToOpen) throws IOException {
        serverSocket = new ServerSocket(whichPortToOpen);
        this.serverPort = whichPortToOpen;
    }

    //  getters

    public int getServerPort() { return serverPort; }
    public ServerSocket getServerSocket() { return serverSocket; }
}
