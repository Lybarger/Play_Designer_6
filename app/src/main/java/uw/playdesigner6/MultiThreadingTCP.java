package uw.playdesigner6;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadingTCP {

    private List<PrintWriter> outStreams;
    private TCPConnection tcp;

    public MultiThreadingTCP(){
        this.outStreams = new ArrayList<PrintWriter>();
        this.tcp = new TCPConnection(this.outStreams);
        this.tcp.start();
    }

    /**
     * Method to send the messages from server to client
     * @param message the message sent by the server
     */
    public void sendMessage(String message){
        System.out.println("TCP connection sending message : " + message);
        for (PrintWriter outStream : outStreams){
            if(outStream != null && !outStream.checkError()){
                outStream.println(message);
                outStream.flush();
            }
        }
    }
}
