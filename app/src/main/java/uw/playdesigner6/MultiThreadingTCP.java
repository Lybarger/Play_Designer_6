package uw.playdesigner6;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadingTCP extends Thread {

    private List<PrintWriter> outStreams;
    private TCPConnection tcp;

    public MultiThreadingTCP(int port){
        this.outStreams = new ArrayList<PrintWriter>();
        this.tcp = new TCPConnection(this.outStreams, port);
        this.tcp.start();
    }

    public void stopTCP(){
       this.tcp.interrupt();
    }

    /**
     * Method to send the messages from server to client
     * @param message the message sent by the server
     */
    public void sendMessage(String message){
        System.out.println("TCP connection sending message : " + message);
        System.out.println("Sending to : " + outStreams.size());
        for (PrintWriter outStream : outStreams){
            if(outStream != null && !outStream.checkError()){
                System.out.println("Sending message!");
                outStream.println(message);
                outStream.flush();
            }
        }
    }
}
