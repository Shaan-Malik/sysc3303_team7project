
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
	private static HashMap<String, Integer> errors;
	private static HashMap<String, Integer> delayAndSpace;
	protected DatagramSocket SendToReceiveFromServerSocket, ReceiveFromClientSocket, SendToClientSocket;
	private static TFTPSim sim;
	private static Scanner scan;
	private static int clientPort;
	private static int serverPort;
	ReceivingAndSendingThread ClientThread;
	ReceivingAndSendingThread ServerThread;
	
	public TFTPSim() {
	}

	public void passOnTFTP() {
		if (SendToReceiveFromServerSocket != null) {
			SendToReceiveFromServerSocket.close();
		}
		if (ReceiveFromClientSocket != null) {
			ReceiveFromClientSocket.close();
		}
		if (SendToClientSocket != null) {
			SendToClientSocket.close();
		}
		SimShutdownThread shutThread = new SimShutdownThread(this, scan);
		shutThread.start();
		try {
			SendToReceiveFromServerSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		try {
			ReceiveFromClientSocket = new DatagramSocket(23);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		try {
			SendToClientSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		ClientThread = new ReceivingAndSendingThread(SendToReceiveFromServerSocket, ReceiveFromClientSocket, SendToClientSocket, this, "Client", errors, delayAndSpace);
		ClientThread.start();
		ServerThread = new ReceivingAndSendingThread(SendToReceiveFromServerSocket, ReceiveFromClientSocket, SendToClientSocket, this, "Server", errors, delayAndSpace);
		ServerThread.start();
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
		errors.clear();
		delayAndSpace.clear();
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

			System.out.println("\nRequest initialisation successful\n");
		}
	}
	
	public int getClientPort() {
		return clientPort;
	}
	public void setClientPort(int newClientPort) {
		clientPort = newClientPort;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int newServerPort) {
		serverPort = newServerPort;
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

class ReceivingAndSendingThread extends Thread {
	String HostName;
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket SendToReceiveFromServerSocket, ReceiveFromClientSocket, SendToClientSocket;
	private static HashMap<String, Integer> Errors;
	private static HashMap<String, Integer> DelayAndSpace;
	private String clientByteNumber = "";
	private String serverByteNumber = "";
	private String prevClientByteNumber = "";
	private String prevServerByteNumber = "";
	private TFTPSim Parent;
	private boolean threadStopped = false;
	
	ReceivingAndSendingThread(DatagramSocket sendToReceiveFromServerSocket, DatagramSocket receiveFromClientSocket, DatagramSocket sendToClientSocket, TFTPSim parent, String hostName, HashMap<String, Integer> errors, HashMap<String, Integer> delayAndSpace){
		HostName = hostName;
		SendToReceiveFromServerSocket = sendToReceiveFromServerSocket;
		ReceiveFromClientSocket = receiveFromClientSocket;
		SendToClientSocket = sendToClientSocket;
		this.Parent = parent;
		Errors = errors;
		DelayAndSpace = delayAndSpace;
	}
	
	public void run() {
		byte[] data;
		if (HostName.equals("Server")) {
			while (true) {
				data = ReceiveFromServer();
				if (threadStopped) {
					break;
				}
				prevServerByteNumber = serverByteNumber;

				switch (data[1]) {
				case 1:
					serverByteNumber = "01";
					break;
				case 2:
					serverByteNumber = "02";
					break;
				case 3:
					serverByteNumber = "03" + "." + Integer.toString(data[2]) + "." + Integer.toString(data[3]);
					break;
				case 4:
					serverByteNumber = "04" + "." + Integer.toString(data[2]) + "." + Integer.toString(data[3]);
					break;
				}

				if (Errors.containsKey(serverByteNumber) && !(prevServerByteNumber.equals(serverByteNumber))) {
					int type = Errors.get(serverByteNumber);
					switch (type) {
					case 1:
						// Losing Packet

						if (data[1] == 4) {
							data = ReceiveFromClient();
							SendToServer(data);
							data = ReceiveFromServer();
							SendToClient(data);
						} else {
							data = ReceiveFromServer();
							SendToClient(data);
						}
						break;
					case 2:
						// delaying Packet
						System.out.println("delaying");
						try {
							Thread.sleep(DelayAndSpace.get(serverByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("sending delayed packet");
						SendToClient(data);
						break;
					case 3:
						// duplicating Packet
						SendToClient(data);
						try {
							Thread.sleep(DelayAndSpace.get(serverByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						SendToClient(data);
						break;
					}
				} else {
					SendToClient(data);
				}
				/*
				if (data[1] == 3 && sendPacket.getLength() < 516) {
					lastAck = true;
				}
				*/
			}
		}
		else if (HostName.equals("Client")) {
			while (true) {
				data = ReceiveFromClient();
				if (threadStopped) {
					break;
				}

				prevClientByteNumber = clientByteNumber;
				System.out.println("prev: " + prevClientByteNumber);

				switch (data[1]) {
				case 1:
					clientByteNumber = "01";
					break;
				case 2:
					clientByteNumber = "02";
					break;
				case 3:
					clientByteNumber = "03" + "." + Integer.toString(data[2]) + "." + Integer.toString(data[3]);
					break;
				case 4:
					clientByteNumber = "04" + "." + Integer.toString(data[2]) + "." + Integer.toString(data[3]);
					break;
				}
				System.out.println("current: " + clientByteNumber);

				if (Errors.containsKey(clientByteNumber) && !(prevClientByteNumber.equals(clientByteNumber))) {
					int type = Errors.get(clientByteNumber);
					switch (type) {
					case 1:
						// Losing Packet
						if (data[1] == 4) { // We have to receive from the server if we intend to drop an ack packet
							data = ReceiveFromServer();
							SendToClient(data);
							data = ReceiveFromClient();
							SendToServer(data);
							data = ReceiveFromClient();
							SendToServer(data);
						} else {
							data = ReceiveFromClient();
							SendToServer(data);
						}
						break;
					case 2:
						// delaying Packet
						try {
							Thread.sleep(DelayAndSpace.get(clientByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						SendToServer(data);
						break;
					case 3:
						// duplicating Packet
						SendToServer(data);
						try {
							Thread.sleep(DelayAndSpace.get(clientByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						SendToServer(data);
						break;
					}
				} else {
					SendToServer(data);
				}
			}
		}
		//return
	}
	
	public byte[] ReceiveFromClient() {
		byte[] data = new byte[516];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Simulator: Waiting for packet from client");
		// Block until a datagram packet is received from receiveSocket.
		try {
			ReceiveFromClientSocket.receive(receivePacket);
		} catch (IOException e) {
			//e.printStackTrace();
			threadStopped = true;
			return data;
			//System.exit(1);
		}

		// Process the received datagram.
		System.out.print("Simulator: received ");
		if(data[1] == 3) System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 4) System.out.println("ACK "+ (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 5) System.out.println("ERROR");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		System.out.println("Length: " + receivePacket.getLength()+"\n");
		
		return data;
	}

	public void SendToServer(byte[] data) {

		// Reset sending port on a new transfer
		/*
		if (data[1] == 1 || data[1] == 2) {
			System.out.println("test");
			Parent.setServerPort(69);
		}
		*/
		/*
		if ((data[1] == 1 || data[1] == 2) && !(prevClientByteNumber.equals(clientByteNumber))) {
			System.out.println("test");
			Parent.setServerPort(69);
		}
		*/
		if (data[1] == 1 || data[1] == 2) {
			try {
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), 69);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}
		else {
			try {
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), Parent.getServerPort());
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}

		System.out.print("Simulator: sending ");
		if(data[1] == 3) System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 4) System.out.println("ACK "+ (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 5) System.out.println("ERROR");
		System.out.println("From host: " + sendPacket.getAddress());
		System.out.println("Host port: " + sendPacket.getPort());
		System.out.println("Length: " + sendPacket.getLength()+"\n");

		try {
			SendToReceiveFromServerSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			//System.exit(1);
		}
	}

	public byte[] ReceiveFromServer() {
		byte[] data = new byte[516];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Simulator: Waiting for packet from server");
		try {
			SendToReceiveFromServerSocket.receive(receivePacket);
		} catch (IOException e) {
			//e.printStackTrace();
			threadStopped = true;
			return data;
			//System.exit(1);
		}

		// Switch sending port while in a transfer
		Parent.setServerPort(receivePacket.getPort());

		System.out.print("Simulator: received ");
		if(data[1] == 3) System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 4) System.out.println("ACK "+ (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 5) System.out.println("ERROR");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		System.out.println("Length: " + receivePacket.getLength()+"\n");

		return data;
	}

	public void SendToClient(byte[] data) {
		try {
			sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), Parent.getClientPort());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		System.out.print("Simulator: sending ");
		if(data[1] == 3) System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 4) System.out.println("ACK "+ (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])) );
		else if(data[1] == 5) System.out.println("ERROR");
		System.out.println("From host: " + sendPacket.getAddress());
		System.out.println("Host port: " + sendPacket.getPort());
		System.out.println("Length: " + sendPacket.getLength()+"\n");

		try {
			SendToClientSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Simulator: packet sent using port " + SendToClientSocket.getLocalPort());
		System.out.println();
	}
}