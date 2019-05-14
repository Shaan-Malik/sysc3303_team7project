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
   //public static enum Request { READ, WRITE, ERROR};
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
      int len, j=0,k=0;
      String req; //request type
      String filename,mode;

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
         
         if (data[0]!=0) req = "error"; // bad
         else if (data[1]==1) req = "read"; // could be read
         else if (data[1]==2) req = "write"; // could be write
         else req = "error"; // bad

         if (req!="error") { // check for filename
             // search for next all 0 byte
         	
             for(j=2;j<len;j++) {
                 if (data[j] == 0) break;
            }
            if (j==len) req="error"; // didn't find a 0 byte
            if (j==2) req="error"; // filename is 0 bytes long
            // otherwise, extract filename
            filename = new String(data,2,j-2);
         }

         if(req!="error") { // check for mode
             // search for next all 0 byte
             for(k=j+1;k<len;k++) { 
                 if (data[k] == 0) break;
            }
            if (k==len) req="error"; // didn't find a 0 byte
            if (k==j+1) req="error"; // mode is 0 bytes long
            mode = new String(data,j,k-j-1);
         }
         
         if(k!=len-1) req="error"; // other stuff at end of packet   
         
         TFTPServerThread t = new TFTPServerThread(data, req, len);
      }

         
         
         

   }

   public static void main( String args[] ) throws Exception
   {
      TFTPServer s = new TFTPServer();
      s.receiveAndSendTFTP();
   }
}


