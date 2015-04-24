package uw.playdesigner6;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by Metta on 3/2/2015.
 */
public class TCPConnection extends Thread {

    public boolean running;
    private List<PrintWriter> outStreams;
    private int port;
    /**
     * Constructor of the class
     */
    public TCPConnection(List<PrintWriter> outStreams, int port) {
        this.port = port;
        this.outStreams = outStreams;
        this.running = true;
    }


    @Override
    public void run() {
        super.run();
            try {
                System.out.println("S: Connecting...");

                //create a server socket. A server socket waits for requests to come in over the network.
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(port));
                serverSocket.setReuseAddress(true);
                System.out.println("Finish connecting " + port);
                while(running) {
                    //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
                    Socket client = serverSocket.accept();
                    System.out.println("S: Receiving...");
                    PrintWriter mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                    outStreams.add(mOut);
                }
            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            }
        }

    }



