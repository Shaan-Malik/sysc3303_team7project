// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.util.Scanner;
import java.net.*;

public class TFTPClient {

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket sendReceiveSocket;

   public TFTPClient()
   {
      try {
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void sendAndReceive(int type, String filename, String dataType, String outputMode, int testMode)
   {
      byte[] msg = new byte[100], // message we send
             fn, // filename as an array of bytes
             md, // mode as an array of bytes
             data; // reply as array of bytes
      			   // dataType as Strings
      int j, len, sendPort;
      
      // In the assignment, students are told to send to 23, so just:
      // sendPort = 23; 
      // is needed.
      // However, in the project, the following will be useful, except
      // that test vs. normal will be entered by the user.
      // change to NORMAL to send directly to server
      
      if (testMode == 0) //Mode 0 is Normal, 1 is Testing
         sendPort = 69;
      else
         sendPort = 23;

      if (outputMode.equals("verbose")) {
    	  System.out.println("Client: creating packet:");
      }
         
        // Prepare a DatagramPacket and send it via sendReceiveSocket
        // to sendPort on the destination host (also on this machine).

       msg[0] = 0;
       msg[1] = (byte) type;

       // next we have a file name -- let's just pick one
       filename = "test.txt";
       // convert to bytes
       fn = filename.getBytes();
        
       // and copy into the msg
       System.arraycopy(fn,0,msg,2,fn.length);
       // format is: source array, source index, dest array,
       // dest index, # array elements to copy
       // i.e. copy fn from 0 to fn.length to msg, starting at
       // index 2
        
       // now add a 0 byte
       msg[fn.length+2] = 0;

       // now add "octet" (or "netascii")
       dataType = "octet";
       // convert to bytes
       md = dataType.getBytes();
        
       // and copy into the msg
       System.arraycopy(md,0,msg,fn.length+3,md.length);
        
       len = fn.length+md.length+4; // length of the message
       // length of filename + length of dataType + opcode (2) + two 0s (2)
       // second 0 to be added next:

       // end with another 0 byte 
       msg[len-1] = 0;

       // Construct a datagram packet that is to be sent to a specified port
       // on a specified host.
       // The arguments are:
       //  msg - the message contained in the packet (the byte array)
       //  the length we care about - k+1
       //  InetAddress.getLocalHost() - the Internet address of the
       //     destination host.
       //     In this example, we want the destination to be the same as
       //     the source (i.e., we want to run the client and server on the
       //     same computer). InetAddress.getLocalHost() returns the Internet
       //     address of the local host.
       //  69 - the destination port number on the destination host.
        
       try {
          sendPacket = new DatagramPacket(msg, len,
                              InetAddress.getLocalHost(), sendPort);
       } catch (UnknownHostException e) {
          e.printStackTrace();
          System.exit(1);
       }
       if (outputMode.equals("verbose")) {
    	   System.out.println("Client: sending packet ");
           System.out.println("To host: " + sendPacket.getAddress());
           System.out.println("Destination host port: " + sendPacket.getPort());
           len = sendPacket.getLength();
           System.out.println("Length: " + len);
           System.out.println("Containing: ");
           for (j=0;j<len;j++) {
               System.out.println("byte " + j + " " + msg[j]);
           }
       }
       
       
       // Form a String from the byte array, and print the string.
       String sending = new String(msg,0,len);
       System.out.println(sending);

        // Send the datagram packet to the server via the send/receive socket.
       try {
          sendReceiveSocket.send(sendPacket);
       } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
       if (outputMode.equals("verbose")) {
    	   System.out.println("Client: Packet sent.");
       }
       // Construct a DatagramPacket for receiving packets up
       // to 100 bytes long (the length of the byte array).
       data = new byte[100];
       receivePacket = new DatagramPacket(data, data.length);
       if (outputMode.equals("verbose")) {
    	   System.out.println("Client: Waiting for packet.");
       }
       try {
          // Block until a datagram is received via sendReceiveSocket.
          sendReceiveSocket.receive(receivePacket);
       } catch(IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
       // Process the received datagram.
       
       if (outputMode.equals("verbose")) {
    	   System.out.println("Client: Packet received:");
           System.out.println("From host: " + receivePacket.getAddress());
           System.out.println("Host port: " + receivePacket.getPort());
           len = receivePacket.getLength();
           System.out.println("Length: " + len);
           System.out.println("Containing: ");
           
           for (j=0;j<len;j++) {
               System.out.println("byte " + j + " " + data[j]);
           }
           System.out.println();
       }
       
      // We're finished, so close the socket.
      sendReceiveSocket.close();
   }

   public static void main(String args[]) // TURN THIS AREA INTO THE UI
   {
	   TFTPClient c = new TFTPClient();
	   Scanner scan = new Scanner(System.in);
	   while (true) {
		   System.out.println("Enter a command: ");
		   String s = scan.next();
		   if (s.equals("start")) {
			   System.out.println("Read or Write request?");
			   String request = scan.next();
			   int type;
			   if (request.equals("read")) {
				   type = 1;
			   }
			   else if (request.equals("write")) {
				   type = 2;
			   }
			   else {
				   System.out.println("Invalid request type");
				   continue;
			   }
			   System.out.println("Enter Filename: ");
			   String filename = scan.next();
			   System.out.println("Enter data type (octet or netascii): ");
			   String datatype = scan.next();
			   if (!((datatype.equals("octet")) || (datatype.equals("netascii")))) {
				   System.out.println("Invalid data type");
				   continue;
			   }
			   System.out.println("Enter quiet or verbose mode: ");
			   String outputMode = scan.next();
			   if (!((outputMode.equals("quiet")) || (outputMode.equals("verbose")))){
				   System.out.println("Invalid mode");
				   continue;
			   }
			   System.out.println("Enter normal or test mode: ");
			   String testModeString = scan.next();
			   int testModeInt;
			   if (testModeString.equals("normal")) {
				   testModeInt = 0;
			   }
			   else if (testModeString.equals("test")) {
				   testModeInt = 1;
			   }
			   else {
				   System.out.println("Invalid request type");
				   continue;
			   }
			   c.sendAndReceive(type, filename, datatype, outputMode, testModeInt);
		   }
		   if (s.equals("shutdown")) {
			   scan.close();
			   System.exit(1);
		   }
	   }
	  	//send 10 alternating test read/write requests
//	  	for(int i = 0; i < 5; i++) {
//	  		c.sendAndReceive(1, "test.txt", "netascii", 0);
//	  		
//	  		c.sendAndReceive(2, "test2.txt", "ocTEt", 0);
//	  	}
//	  	
//	  	//send an invalid request
//	  	c.sendAndReceive(7, "test.txt", "netascii", 0);
//	  	
//	  	//Close the client
//	  	//c.close();
   }
}


