
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
	// public static enum Request { READ, WRITE, ERROR};
	// responses for valid requests

	// UDP datagram packets and sockets used to send / receive
	private DatagramPacket receivePacket;
	private DatagramSocket receiveSocket;
	private ThreadGroup Threads;

	public TFTPServer() {
		try {
			// Construct a datagram socket and bind it to port 69
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(69);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		Threads = new ThreadGroup("Parent Thread Group");
	}

	/**
	 * Receives and sends data transfer packets to a client
	 * 
	 * @throws Exception on invalid requests
	 */
	public void receiveAndSendTFTP(String serverDirectory) throws Exception {

		byte[] data;
		int len, j = 0, k = 0;
		String req = "error"; // request type
		String filename = "", mode;
		ServerShutdownThread shutThread = new ServerShutdownThread(this);
		shutThread.start();
		for (;;) { // loop forever
			// Construct a DatagramPacket for receiving packets up
			// to 516 bytes long (the length of the byte array).

			data = new byte[516];
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
			System.out.print("Server: ");
			if(data[1] == 1) System.out.println("RRQ received");
			else if(data[1] == 2) System.out.println("WRQ received");
			
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			// System.out.println("Containing: ");

			// print the bytes
			// for (j = 0; j < len; j++) {
			// System.out.println("byte " + j + " " + data[j]);
			// }

			if (data[0] != 0)
				sendErrorPacket(4, "Received Opcode is Invalid", receivePacket.getPort(), receivePacket.getAddress());
			else if (data[1] == 1)
				req = "read"; // could be read
			else if (data[1] == 2)
				req = "write"; // could be write
			else
				sendErrorPacket(4, "Received Opcode is Invalid", receivePacket.getPort(), receivePacket.getAddress());

				// check for filename
				// search for next all 0 byte

				for (j = 2; j < len; j++) {
					if (data[j] == 0)
						break;
				}
				if (j == len - 1) {
					sendErrorPacket(4, "Received Packet has no middle separator", receivePacket.getPort(), receivePacket.getAddress());
				}
				if (j == len)
					sendErrorPacket(4, "Received Packet has no separator", receivePacket.getPort(), receivePacket.getAddress()); // didn't find a 0 byte
				if (j == 2)
					sendErrorPacket(4, "Received Packet has no filename", receivePacket.getPort(), receivePacket.getAddress()); // filename is 0 bytes long
				// otherwise, extract filename
				filename = new String(data, 2, j - 2);

				//If file doesn't exist, send error code 1 to client
				if(req == "read" && !( new File(serverDirectory+"/"+filename) ).exists() ) {
					sendErrorPacket(1, "File "+serverDirectory+"/"+filename+" doesn't exist", receivePacket.getPort(), receivePacket.getAddress());
				}
				
				//If file already exists, send error 6 to client
				if(req == "write" && ( new File(serverDirectory+"/"+filename) ).exists() ) {
					sendErrorPacket(6, "File "+serverDirectory+"/"+filename+" already exists", receivePacket.getPort(), receivePacket.getAddress());
				}

				// check for mode
				// search for next all 0 byte
				for (k = j + 1; k < len; k++) {
					if (data[k] == 0)
						break;
				}
				if (k == len)
					sendErrorPacket(4, "Received Packet has no ending separator", receivePacket.getPort(), receivePacket.getAddress()); // didn't find a 0 byte
				if (k == j + 1)
					sendErrorPacket(4, "Received Packet has no mode", receivePacket.getPort(), receivePacket.getAddress()); // mode is 0 bytes long
				mode = new String(data, j, k - j - 1);
				if (! ( (mode != "octet") || (mode != "netascii") )) sendErrorPacket(4, "Received Packet has incorrect mode", receivePacket.getPort(), receivePacket.getAddress());


			if (k != len - 1)
				sendErrorPacket(4, "Received Packet has excess data", receivePacket.getPort(), receivePacket.getAddress()); // other stuff at end of packet
			
			
			// If a tread working on the filename isn't in the thread group start a separate
			// thread to handle it
			boolean fileIsFree = true;

			Thread[] threadArray = new Thread[Threads.activeCount()];
			Threads.enumerate(threadArray);
			for (Thread thread : threadArray) {
				if (thread.getName().equals(filename))
					fileIsFree = false;
			}

			if (fileIsFree) {
				TFTPServerThread t = new TFTPServerThread(serverDirectory, data, receivePacket, req, len, Threads, filename);
				t.start();
			}

		}
	}
	
	public void initialise() {
		Scanner scan = new Scanner(System.in);
		if (Threads != null)  {
			Thread[] threadArray = new Thread[Threads.activeCount()];
			Threads.enumerate(threadArray);
			for (Thread thread : threadArray) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("TFTP Server Iteration 5:\n");
		System.out.println("Enter the file directory:");
		String fileDirectory = scan.next();
		System.out.println("File Directory Saved\n");
		while (true) {
			System.out.println("Type done if finished, or type file to change the file directory");
			String command = scan.next();
			if (command.equals("done")) {
				try {
					receiveAndSendTFTP(fileDirectory);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			else if (command.equals("file")) {
				System.out.println("Enter the file directory:");
				fileDirectory = scan.next();
				System.out.println("File Directory Saved\n");
			}
		}
	}
	

	/**
	 * Shuts down the currently active server after all threads have stopped
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		Thread[] threadArray = new Thread[Threads.activeCount()];
		Threads.enumerate(threadArray);
		for (Thread thread : threadArray) {
			thread.join();
		}
		System.out.println("Server shutting down");
		System.exit(0);
	}

	public static void main(String args[]) throws Exception {
		TFTPServer s = new TFTPServer();
		s.initialise();
	}

	void sendErrorPacket(int errorCode, String msg, int port, InetAddress dest) {
		byte[] byteString = msg.getBytes();
		byte[] errorPacket = new byte[5 + byteString.length];
		errorPacket[0] = 0;
		errorPacket[1] = 5;
		errorPacket[2] = 0;
		errorPacket[3] = (byte) errorCode;
		for (int j = 0; j < byteString.length; j++) {
			errorPacket[j + 4] = byteString[j];
		}
		errorPacket[errorPacket.length - 1] = 0;
		System.out.println("Sending Error " + errorCode +": "+ msg ); 
		try {
			receiveSocket.send(new DatagramPacket(errorPacket, errorPacket.length, dest, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (errorCode != 5) {
			System.out.println("Shutting down server");
			System.exit(0);
		}
	}
}

class ServerShutdownThread extends Thread {
	Scanner scan;
	TFTPServer parent;

	ServerShutdownThread(TFTPServer _parent) {
		scan = new Scanner(System.in);
		parent = _parent;
	}

	/**
	 * On user typing in "shutdown" this shuts the server down
	 */
	public void run() {
		while (true) {
			String s = scan.nextLine();
			if (s.equals("r")) {
				parent.initialise();
				break;
			}
			if (s.equals("s")) {
				try {
					scan.close();
					parent.shutdown();
					break;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}