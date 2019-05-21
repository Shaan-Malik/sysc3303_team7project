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
      
   }

   public void sendAndReceive(int type, String filename, String dataType, String outputMode, int testMode)
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
	   int type, testModeInt;
	   String filename, datatype, outputMode;
	   System.out.println("TFTP Client: Interation 1\n");
	   while (true) {
		   System.out.print("Enter read or write request: \n");
		   String request = scan.next();
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
		   System.out.print("Enter filename: \n");
		   filename = scan.next();
		   System.out.print("Enter octet or netascii data type: \n");
		   datatype = scan.next();
		   if (!((datatype.equals("octet")) || (datatype.equals("netascii")))) {
			   System.out.println("Invalid data type");
			   continue;
		   }
		   System.out.print("Enter quiet or verbose mode: \n");
		   outputMode = scan.next();
		   if (!((outputMode.equals("quiet")) || (outputMode.equals("verbose")))){
			   System.out.println("Invalid mode");
			   continue;
		   }
		   System.out.print("Enter normal or test mode: \n");
		   String testModeString = scan.next();
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
		   System.out.println("\nRequest initialisation successful\n");
		   break;
	   }
	   System.out.println("Tip: Run the help command if you are stuck\n");
	   while (true) {
		   System.out.println("Enter a command: ");
		   String s = scan.next();
		   if (s.equals("init")) {
			   System.out.println("Read or Write request?");
			   String request = scan.next();
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
			   filename = scan.next();
			   System.out.println("Enter data type (octet or netascii): ");
			   datatype = scan.next();
			   if (!((datatype.equals("octet")) || (datatype.equals("netascii")))) {
				   System.out.println("Invalid data type");
				   continue;
			   }
			   System.out.println("Enter quiet or verbose mode: ");
			   outputMode = scan.next();
			   if (!((outputMode.equals("quiet")) || (outputMode.equals("verbose")))){
				   System.out.println("Invalid mode");
				   continue;
			   }
			   System.out.println("Enter normal or test mode: ");
			   String testModeString = scan.next();
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
		   }
		   else if (s.equals("shutdown")) {
			   scan.close();
			   System.exit(0);
		   }
		   else if (s.equals("help")) {
			   System.out.println("help: The help command");
			   System.out.println("init: Can initialise a new request");
			   System.out.println("run: Run a request");
			   System.out.println("shutdown: Shutsdown client");
			   System.out.println("read: Sets to a read request");
			   System.out.println("write: Sets to a write request");
			   System.out.println("filename: Type in a new filename after the filename command.");
			   System.out.println("octet: Sets to a octet datatype");
			   System.out.println("netascii: Sets to a netascii datatype");
			   System.out.println("quiet: Sets to quiet mode");
			   System.out.println("verbose: Sets to verbose mode");
			   System.out.println("normal: Sets to normal mode");
			   System.out.println("test: Sets to test mode");
		   }
		   else if (s.equals("run")) {
			   c.sendAndReceive(type, filename, datatype, outputMode, testModeInt);
		   }
		   else if (s.equals("read")) {
			   type = 1;
			   System.out.println("Client is now set to do a read request");
		   }
		   else if (s.equals("write")) {
			   type = 2;
			   System.out.println("Client is now set to do a write request");
		   }
		   else if (s.length() >= 10 && s.substring(0,8).equals("filename")) {
			   filename = s.substring(9);
			   System.out.println("filename has been changed to: " + filename);
		   }
		   else if (s.equals("octet")) {
			   datatype = "octet";
			   System.out.println("Client is now set to octet datatype");
		   }
		   else if (s.equals("netascii")) {
			   datatype = "netascii";
			   System.out.println("Client is now set to netascii datatype");
		   }
		   else if (s.equals("quiet")) {
			   outputMode = "quiet";
			   System.out.println("Client is now set to quiet mode");
		   }
		   else if (s.equals("verbose")) {
			   outputMode = "verbose";
			   System.out.println("Client is now set to verbose mode");
		   }
		   else if (s.equals("normal")) {
			   testModeInt = 0;
			   System.out.println("Client is now set to normal mode");
		   }
		   else if (s.equals("test")) {
			   testModeInt = 1;
			   System.out.println("Client is now set to test mode");
		   }
	   }
   }
}


