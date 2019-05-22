import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;


public class TFTPServerThread extends Thread {
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	//public static enum Request { READ, WRITE, ERROR};
	public static final byte[] readResp = {0, 3, 0, 1};
	public static final byte[] writeResp = {0, 4, 0, 0};
	byte[] data, response = new byte[4];
	String req; // READ, WRITE or ERROR
	int len;
	String filename,mode;
	int j,k;
	int block; //the number of blocks that have been read.
	FileInputStream input;
	String serverDirectory = "TFTPServer";

	TFTPServerThread(byte[] _data, DatagramPacket _receivePacket, String _req, int _len, String _threadName, ThreadGroup _threadGroup, String _filename) {
		super(_threadGroup, _threadName); 
		receivePacket = _receivePacket;
		data = _data;
		req = _req;
		if (req == "read") block = 1;
		if (req == "write") block = 0;
		len = _len;
		filename = _filename;
	}
	public void run() {
		// If it's a read, send back DATA (03) block 1
        // If it's a write, send back ACK (04) block 0
        // Otherwise, ignore it
		
//        if (data[0]!=0) req = "error"; // bad
//        else if (data[1]==1) req = "read"; // could be read
//        else if (data[1]==2) req = "write"; // could be write
//        else req = "error"; // bad
//
//        if (req!="error") { // check for filename
//            // search for next all 0 byte
//        	
//            for(j=2;j<len;j++) {
//                if (data[j] == 0) break;
//           }
//           if (j==len) req="error"; // didn't find a 0 byte
//           if (j==2) req="error"; // filename is 0 bytes long
//           // otherwise, extract filename
//           filename = new String(data,2,j-2);
//        }
//
//        if(req!="error") { // check for mode
//            // search for next all 0 byte
//            for(k=j+1;k<len;k++) { 
//                if (data[k] == 0) break;
//           }
//           if (k==len) req="error"; // didn't find a 0 byte
//           if (k==j+1) req="error"; // mode is 0 bytes long
//           mode = new String(data,j,k-j-1);
//        }
//        
//        if(k!=len-1) req="error"; // other stuff at end of packet   
        
		File destinationFile = new File("M:/"+serverDirectory+"/"+filename);
        if (req == "read") {
        	try {
        
			input = new FileInputStream(destinationFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        }
        
        // Create a response.
        if (req=="read") { // for Read it's 0301
        	int blockLength = (512 > destinationFile.length()) ? ((int) destinationFile.length()) : (512);
           byte[] bytes = new byte[blockLength + 4];
        	bytes[0] = 0;
        	bytes[1] = 3;
        	bytes[2] = 0;
        	bytes[3] = 1;
        	//bytes from index 2 to index (length - 3)
        	try {
				input.read(bytes, 4, blockLength);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	sendPacket = new DatagramPacket(bytes, bytes.length,
                    receivePacket.getAddress(), receivePacket.getPort());
        	
        	
        	
        } else if (req=="write") { // for Write it's 0400
           //response = writeResp;
           response = createWriteResponse(block);
           sendPacket = new DatagramPacket(response, response.length,
                   receivePacket.getAddress(), receivePacket.getPort());
        } else { // it was invalid, close socket on port 69 (so things work properly next time) and quit
           sendReceiveSocket.close();
           try {
			throw new Exception("Not yet implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }

        // Construct a datagram packet that is to be sent to a specified port
        // on a specified host.
        // The arguments are:
        //  data - the packet data (a byte array). This is the response.
        //  receivePacket.getLength() - the length of the packet data.
        //     This is the length of the msg we just created.
        //  receivePacket.getAddress() - the Internet address of the
        //     destination host. Since we want to send a packet back to the
        //     client, we extract the address of the machine where the
        //     client is running from the datagram that was sent to us by
        //     the client.
        //  receivePacket.getPort() - the destination port number on the
        //     destination host where the client is running. The client
        //     sends and receives datagrams through the same socket/port,
        //     so we extract the port that the client used to send us the
        //     datagram, and use that as the destination port for the TFTP
        //     packet.

        

        System.out.println("Server: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.println("Containing: ");
   //     for (j=0;j<len;j++) {
     //      System.out.println("byte " + j + " " + response[j]);
     //   }

        // Send the datagram packet to the client via a new socket.

        try {
           // Construct a new datagram socket and bind it to any port
           // on the local host machine. This socket will be used to
           // send UDP Datagram packets.
           sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
           se.printStackTrace();
           System.exit(1);
        }

        try {
           sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
        }

        System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
        System.out.println();
        FileOutputStream output = null;
        if (req.equals("write")) {
             try {
     			output = new FileOutputStream(destinationFile);
     		} catch (FileNotFoundException e1) {
     			e1.printStackTrace();
     		}
        }
       
        System.out.println("REACHED WHILE LOOP");
        
        while (true) {
        	//wait for new packet
        	try {
                // Block until a datagram is received via sendReceiveSocket.
                sendReceiveSocket.receive(receivePacket);
             } catch(IOException e) {
                e.printStackTrace();
                System.exit(1);
             }
        	System.out.println("PACKET RECEIVED");
        	//check if it's read or write
        	byte[] data = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
        	if (data[1] == 3) {
        		//DATA
        		try {
					output.write(data,4, data.length - 4);
				} catch (IOException e) {
					e.printStackTrace();
				}
        		
        		byte[] bytes = {0, 4, data[2], data[3]};
        		sendPacket = new DatagramPacket(bytes, bytes.length,
                        receivePacket.getAddress(), receivePacket.getPort());
        		
        		 try {
        	           sendReceiveSocket.send(sendPacket);
        	     } catch (IOException e) {
        	           e.printStackTrace();
        	           System.exit(1);
        	     }
        		 System.out.println("FLAG");
        		 if (data.length < 516) {
        			//Closes the stream, file is complete
        			try {
        				output.close();
        				System.out.println("output closed");
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        			
        			break;
        		 }
        		
        	} else if (data[1] == 4) {
        		//ACK
        		int blockNumber = data[2]*256 + data[3] + 1;
        		int sendingSize = (blockNumber*512 > destinationFile.length()) ? ((int) destinationFile.length() % 512) : (512);
        		byte[] bytes = new byte[sendingSize + 4];
        		bytes[0] = 0;
        		bytes[1] = 3;
        		bytes[2] = (byte) (blockNumber/256);
        		bytes[3] = (byte) (blockNumber%256);
        		
        		try {
    				input.read(bytes, 4, sendingSize);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
        		
        		sendPacket = new DatagramPacket(bytes, bytes.length,
                        receivePacket.getAddress(), receivePacket.getPort());
        		
        		 try {
        	           sendReceiveSocket.send(sendPacket);
        	     } catch (IOException e) {
        	           e.printStackTrace();
        	           System.exit(1);
        	     }
        		 
        		 if (sendingSize < 512) {
        			 try {
						input.close();
					 } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					 } 
        			 break;
        		 }
        	}
        	
        	//parse packet
        	
        	//create response
        	
        	//
        }
        
        
        
        
        
        
        
        // We're finished with this socket, so close it.
        sendReceiveSocket.close();
     } // end of loop
	
	 public byte[] createReadResponse(int block) {
		 int b = block;
		 byte byte1 = 0, byte2 = 0;
		 byte1 = (byte) (b/256);
		 byte2 = (byte) (b%256);
		 
		 byte[] bytes = {0, 3, byte1, byte2};
		 return bytes;
	 }
	 public byte[] createWriteResponse(int block) {
		 int b = block;
		 byte byte1 = 0, byte2 = 0;
		 byte1 = (byte) (b/256);
		 byte2 = (byte) (b%256);
		 
		 byte[] bytes = {0, 4, byte1, byte2};
		 return bytes;
	 }
}
	
