// TFTPServer.java
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.io.*;
import java.net.*;
import java.util.*;

public class TFTPServer {

   // types of requests we can receive
   public static enum Request { READ, WRITE, ERROR};
   // responses for valid requests
  
   
   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket receivePacket;
   private DatagramSocket receiveSocket;
   
   public TFTPServer()
   {
      try {
         // Construct a datagram socket and bind it to port 69
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets.
         receiveSocket = new DatagramSocket(69);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void receiveAndSendTFTP() throws Exception
   {

      byte[] data;
      int len, j=0;

      for(;;) { // loop forever
         // Construct a DatagramPacket for receiving packets up
         // to 100 bytes long (the length of the byte array).
         
         data = new byte[100];
         receivePacket = new DatagramPacket(data, data.length);

         System.out.println("Server: Waiting for packet.");
         // Block until a datagram packet is received from receiveSocket.
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         // Process the received datagram.
         System.out.println("Server: Packet received:");
         System.out.println("From host: " + receivePacket.getAddress());
         System.out.println("Host port: " + receivePacket.getPort());
         len = receivePacket.getLength();
         System.out.println("Length: " + len);
         System.out.println("Containing: " );
         
         // print the bytes
         for (j=0;j<len;j++) {
            System.out.println("byte " + j + " " + data[j]);
         }

         // Form a String from the byte array.
         String received = new String(data,0,len);
         System.out.println(received);
      }

         
         
         

   }

   public static void main( String args[] ) throws Exception
   {
      TFTPServer s = new TFTPServer();
      s.receiveAndSendTFTP();
   }
}


