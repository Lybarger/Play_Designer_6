package uw.playdesigner6;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by Metta on 3/2/2015.
 */
public class TCPConnection extends Thread {

    public static final int SERVERPORT = 4445;
    private boolean running = false;
    private PrintWriter mOut;
    private List<PrintWriter> outStreams;
    /**
     * Constructor of the class
     */
    public TCPConnection(List<PrintWriter> outStreams) {
        this.outStreams = outStreams;
    }

    @Override
    public void run() {
        super.run();

        running = true;
        try {
            System.out.println("S: Connecting...");

            //create a server socket. A server socket waits for requests to come in over the network.
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
            Socket client = serverSocket.accept();
            System.out.println("S: Receiving...");
            mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            outStreams.add(mOut);
        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }

    }


}

