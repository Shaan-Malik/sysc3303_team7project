
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
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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
	private int clientPort;
	private int serverPort = 69;

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

		SimShutdownThread shutThread = new SimShutdownThread(this, scan);
		shutThread.start();

		for (;;) { // loop forever
			data = ReceiveFromClient();
			SendToHost(data);
			data = ReceiveFromHost();
			SendToClient(data);
		}
	}

	public byte[] ReceiveFromClient() {
		byte[] data = new byte[516];
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
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Containing: ");

		// print the bytes
		for (int j = 0; j < len; j++) {
			System.out.println("byte " + j + " " + data[j]);
		}

		// Form a String from the byte array, and print the string.
		String received = new String(data, 0, len);
		System.out.println(received);
		return data;
	}

	public void SendToHost(byte[] data) {

		// Reset sending port on a new transfer
		if (data[1] == 1 || data[1] == 2)
			serverPort = 69;

		try {
			sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), serverPort);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		System.out.println("Simulator: sending packet.");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Containing: ");
		for (int j = 0; j < len; j++) {
			System.out.println("byte " + j + " " + data[j]);
		}

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public byte[] ReceiveFromHost() {
		byte[] data = new byte[516];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Simulator: Waiting for packet.");
		try {
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Switch sending port while in a transfer
		serverPort = receivePacket.getPort();

		System.out.println("Simulator: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Containing: ");
		for (int j = 0; j < len; j++) {
			System.out.println("byte " + j + " " + data[j]);
		}

		return data;
	}

	public void SendToClient(byte[] data) {
		try {
			sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), clientPort);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		System.out.println("Simulator: Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Containing: ");
		for (int j = 0; j < len; j++) {
			System.out.println("byte " + j + " " + data[j]);
		}

		try {
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
		sendSocket.close();
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
		int type, packetNumber, timeOrSpace;
		String byteNumber = null;
		String packetType;
		boolean scanning;
		System.out.println("Error Collection Test Program\n");
		while (true) {
			scanning = true;
			type = 0;
			packetNumber = 0;
			timeOrSpace = 0;
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
						timeOrSpace = Integer.parseInt(scan.nextLine());
						scanning = false;
					} catch (Exception InputMismatchException) {
						System.out.println("Invalid input");
						continue;
					}
				}
			}
			if (packetType.toLowerCase().equals("rrq")) {
				byteNumber = "01";
			} else if (packetType.toLowerCase().equals("wrq")) {
				byteNumber = "02";
			} else if (packetType.toLowerCase().equals("ack")) {
				int b1 = packetNumber / 256;
				int b2 = packetNumber % 256;
				byteNumber = "04" + "." + Integer.toString(b1) + "." + Integer.toString(b2);
			} else if (packetType.toLowerCase().equals("data")) {
				int b1 = packetNumber / 256;
				int b2 = packetNumber % 256;
				byteNumber = "03" + "." + Integer.toString(b1) + "." + Integer.toString(b2);
			} else {
				System.out.println("Invalid input");
				continue;
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