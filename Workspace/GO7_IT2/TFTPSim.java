
// TFTPSim.java
// This class is the beginnings of an error simulator for a simple TFTP server 
// based on UDP/IP. The simulator receives a read or write packet from a client and
// passes it on to the server.  Upon receiving a response, it passes it on to the 
// client.
// One socket (23) is used to receive from the client, and another to send/receive
// from the server.  A new socket is used for each communication back to the client.   

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

public class TFTPSim {

	// UDP datagram packets and sockets used to send / receive
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket;
	private static HashMap<String, Integer> errors;
	private static HashMap<String, Integer> delayAndSpace;
	private static TFTPSim sim;
	private static Scanner scan;

	public TFTPSim() {
		try {
			// Construct a datagram socket and bind it to port 23
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets from clients.
			receiveSocket = new DatagramSocket(23);
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets from the server.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void passOnTFTP() {

		byte[] data;

		int clientPort, j = 0, len, serverPort = 69;

		SimShutdownThread shutThread = new SimShutdownThread(this, scan);
		shutThread.start();

		for (;;) { // loop forever
			// Construct a DatagramPacket for receiving packets up
			// to 516 bytes long (the length of the byte array).

			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Simulator: Waiting for packet.");
			// Block until a datagram packet is received from receiveSocket.
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("Simulator: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			clientPort = receivePacket.getPort();
			System.out.println("Host port: " + clientPort);
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");

			// print the bytes
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + data[j]);
			}

			// Form a String from the byte array, and print the string.
			String received = new String(data, 0, len);
			System.out.println(received);

			// Now pass it on to the server (to port 69)
			// Construct a datagram packet that is to be sent to a specified port
			// on a specified host.
			// The arguments are:
			// msg - the message contained in the packet (the byte array)
			// the length we care about - k+1
			// InetAddress.getLocalHost() - the Internet address of the
			// destination host.
			// In this example, we want the destination to be the same as
			// the source (i.e., we want to run the client and server on the
			// same computer). InetAddress.getLocalHost() returns the Internet
			// address of the local host.
			// 69 - the destination port number on the destination host.

			// Reset sending port on a new transfer
			if (data[1] == 1 || data[1] == 2)
				serverPort = 69;

			sendPacket = new DatagramPacket(data, len, receivePacket.getAddress(), serverPort);

			System.out.println("Simulator: sending packet.");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + data[j]);
			}

			// Send the datagram packet to the server via the send/receive socket.

			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Construct a DatagramPacket for receiving packets up
			// to 516 bytes long (the length of the byte array).

			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Simulator: Waiting for packet.");
			try {
				// Block until a datagram is received via sendReceiveSocket.
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// Switch sending port while in a transfer
			serverPort = receivePacket.getPort();

			// Process the received datagram.
			System.out.println("Simulator: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + data[j]);
			}

			// Construct a datagram packet that is to be sent to a specified port
			// on a specified host.
			// The arguments are:
			// data - the packet data (a byte array). This is the response.
			// receivePacket.getLength() - the length of the packet data.
			// This is the length of the msg we just created.
			// receivePacket.getAddress() - the Internet address of the
			// destination host. Since we want to send a packet back to the
			// client, we extract the address of the machine where the
			// client is running from the datagram that was sent to us by
			// the client.
			// receivePacket.getPort() - the destination port number on the
			// destination host where the client is running. The client
			// sends and receives datagrams through the same socket/port,
			// so we extract the port that the client used to send us the
			// datagram, and use that as the destination port for the TFTP
			// packet.

			sendPacket = new DatagramPacket(data, receivePacket.getLength(), receivePacket.getAddress(), clientPort);

			System.out.println("Simulator: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + data[j]);
			}

			// Send the datagram packet to the client via a new socket.

			try {
				// Construct a new datagram socket and bind it to any port
				// on the local host machine. This socket will be used to
				// send UDP Datagram packets.
				sendSocket = new DatagramSocket();
			} catch (SocketException se) {
				se.printStackTrace();
				System.exit(1);
			}

			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Simulator: packet sent using port " + sendSocket.getLocalPort());
			System.out.println();

			// We're finished with this socket, so close it.
			sendSocket.close();
		} // end of loop

	}

	/**
	 * Shuts down the currently active error simulator
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		System.out.println("Sim shutting down");
		System.exit(0);
	}

	public void initialise() {

		scan = new Scanner(System.in);
		int type = 0, packetNumber = 0, timeOrSpace = 0;
		String byteNumber = null;
		String packetType;
		System.out.println("Error Collection Test Program\n");
		while (true) {
			boolean scanning = true;
			while (scanning) {
				try {
					System.out.print(
							"Enter 0 if finished. Enter 1 for loss, 2 for delay, or 3 for duplicate. Enter 4 to shutdown.\n");
					type = Integer.parseInt(scan.nextLine());
					scanning = false;
				} catch (Exception InputMismatchException) {
					System.out.println("Invalid input");
					continue;
				}
			}
			if (type == 0) {
				System.out.println("Finished Initialisation");
				passOnTFTP();
				break;
			}
			if (type == 4) {
				try {
					scan.close();
					shutdown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (type != 1 && type != 2 && type != 3) {
				System.out.println("Invalid request type");
				continue;
			}
			System.out.print("Enter the request you want to simulate: RRQ, WRQ, ACK, DATA \n");
			packetType = scan.nextLine();
			if (packetType.toLowerCase().equals("ack") || packetType.toLowerCase().equals("data")) {
				scanning = true;
				while (scanning) {
					try {
						System.out.print("Enter the number of the packet: \n");
						packetNumber = Integer.parseInt(scan.nextLine());
						scanning = false;
					} catch (Exception InputMismatchException) {
						System.out.println("Invalid input");
						continue;
					}
				}
			}
			if (type > 1) {
				scanning = true;
				while (scanning) {
					try {
						if (type == 2) {
							System.out.print("Enter the delay time in seconds: \n");
						} else if (type == 3) {
							System.out.print("Enter the space between duplicates: \n");
						}
						packetNumber = Integer.parseInt(scan.nextLine());
						scanning = false;
					} catch (Exception InputMismatchException) {
						System.out.println("Invalid input");
						continue;
					}
				}
			}
			if (packetType.toLowerCase().equals("rrq")) {
				byteNumber = "0100";
			} else if (packetType.toLowerCase().equals("wrq")) {
				byteNumber = "0200";
			} else if (packetType.toLowerCase().equals("ack")) {
				int b1 = packetNumber / 256;
				int b2 = packetNumber % 256;
				byteNumber = "04" + "." + Integer.toString(b1) + "." + Integer.toString(b2);
			} else if (packetType.toLowerCase().equals("data")) {
				int b1 = packetNumber / 256;
				int b2 = packetNumber % 256;
				byteNumber = "03" + "." + Integer.toString(b1) + "." + Integer.toString(b2);
			}
			errors.put(byteNumber, type);
			delayAndSpace.put(byteNumber, timeOrSpace);

			System.out.println("errors[" + byteNumber + "] = " + type);
			System.out.println("delayAndSpace[" + byteNumber + "] = " + timeOrSpace);
			System.out.println("\nRequest initialisation successful\n");
		}
	}

	public static void main(String args[]) {
		sim = new TFTPSim();
		errors = new HashMap<>(); // stores a key-value pair: the unique code based on block
									// number, and the type of error
		delayAndSpace = new HashMap<>(); // stores either the delay time or space between
											// duplicates.
		sim.initialise();
	}
}

class SimShutdownThread extends Thread {
	TFTPSim parent;
	Scanner scan;

	SimShutdownThread(TFTPSim _parent, Scanner _scan) {
		parent = _parent;
		scan = _scan;
	}

	/**
	 * On user typing in "shutdown" this shuts the error sim down
	 */
	public void run() {
		while (true) {
			String s = scan.nextLine();
			if (s.equals("reset")) {
				parent.initialise();
				break;
			}
			if (s.equals("shutdown")) {
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