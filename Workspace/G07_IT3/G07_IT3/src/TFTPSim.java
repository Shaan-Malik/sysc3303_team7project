
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class TFTPSim {

	// UDP datagram packets and sockets used to send / receive
	private static HashMap<String, Integer> errors;
	private static HashMap<String, Integer> errorData;
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

		ClientThread = new ReceivingAndSendingThread(SendToReceiveFromServerSocket, ReceiveFromClientSocket,
				SendToClientSocket, this, "Client", errors, errorData);
		ClientThread.start();
		ServerThread = new ReceivingAndSendingThread(SendToReceiveFromServerSocket, ReceiveFromClientSocket,
				SendToClientSocket, this, "Server", errors, errorData);
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
		errorData.clear();
		scan = new Scanner(System.in);
		int type, packetNumber, timeOrSpace;
		String byteNumber = null;
		String packetType;
		boolean scanning;
		System.out.println("\nError Simulator Iteration 3\n");
		while (true) {
			scanning = true;
			type = 0;
			packetNumber = 0;
			timeOrSpace = 0;
			while (scanning) {
				try {
					System.out.print(
							"Enter 0 if finished. Enter 1 for losing, delaying, or duplicating packets. Enter 2 for creating errors. Enter 3 to send a packet using the wrong port. Enter 4 to shutdown.\n");
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
			} else if (type == 1) {
				scanning = true;
				while (scanning) {
					try {
						System.out.print("Enter 1 for loss, 2 for delay, or 3 for duplicate. Enter 4 to exit menu.\n");
						type = Integer.parseInt(scan.nextLine());
						scanning = false;
					} catch (Exception InputMismatchException) {
						System.out.println("Invalid input");
						continue;
					}
				}
				if (type == 4) {
					continue;
				}
				System.out.print("Enter the packet you want to simulate an error for: RRQ, WRQ, ACK, DATA \n");
				packetType = scan.nextLine();
				if (packetType.toLowerCase().equals("ack") || packetType.toLowerCase().equals("data")) {
					scanning = true;
					while (scanning) {
						try {
							System.out.print("Enter the block number of the packet: \n");
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
				if (packetType.toLowerCase().equals("data") || packetType.toLowerCase().equals("ack")) {
					errorData.put(byteNumber, timeOrSpace);
				}
				switch (type) {
				case 1:
					System.out.println("Losing Packet Error Added Successfully\n");
					break;
				case 2:
					System.out.println("Delaying Packet Error Added Successfully\n");
					break;
				case 3:
					System.out.println("Duplicating Packet Error Added Successfully\n");
					break;
				}
			} else if (type == 2) {
				scanning = true;
				while (scanning) {
					try {
						System.out.print(
								"Enter 1 to change opcode, 2 to delete filename, 3 to delete mode, 4 to change formatting of request. Enter 5 to exit menu.\n");
						type = Integer.parseInt(scan.nextLine());
						scanning = false;
					} catch (Exception InputMismatchException) {
						System.out.println("Invalid input");
						continue;
					}
				}
				if (type == 5) {
					continue;
				} else if (type == 1) {
					int opCode = 0;
					System.out.print("Enter the packet you want to change the opcode for: RRQ, WRQ, ACK, DATA \n");
					packetType = scan.nextLine();
					if (packetType.toLowerCase().equals("ack") || packetType.toLowerCase().equals("data")) {
						scanning = true;
						while (scanning) {
							try {
								System.out.print("Enter the block number of the packet: \n");
								packetNumber = Integer.parseInt(scan.nextLine());
								scanning = false;
							} catch (Exception InputMismatchException) {
								System.out.println("Invalid input");
								continue;
							}
						}
					}
					scanning = true;
					while (scanning) {
						try {
							System.out.print("Enter the new opcode of the packet: \n");
							opCode = Integer.parseInt(scan.nextLine());
							scanning = false;
						} catch (Exception InputMismatchException) {
							System.out.println("Invalid input");
							continue;
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
					errors.put(byteNumber, 4);
					errorData.put(byteNumber, opCode);
					System.out.println("Op Code Successfully Changed\n");
				} else if (type == 2) {
					System.out.print("Enter the packet you want to delete the filename for for: RRQ or WRQ \n");
					packetType = scan.nextLine();
					if (packetType.toLowerCase().equals("rrq")) {
						byteNumber = "01";
					} else if (packetType.toLowerCase().equals("wrq")) {
						byteNumber = "02";
					} else {
						System.out.println("Invalid input");
						continue;
					}
					errors.put(byteNumber, 5);
					System.out.println("File Name Successfully Deleted\n");
				} else if (type == 3) {
					System.out.print("Enter the packet you want to delete the mode for: RRQ or WRQ \n");
					packetType = scan.nextLine();
					if (packetType.toLowerCase().equals("rrq")) {
						byteNumber = "01";
					} else if (packetType.toLowerCase().equals("wrq")) {
						byteNumber = "02";
					} else {
						System.out.println("Invalid input");
						continue;
					}
					errors.put(byteNumber, 6);
					System.out.println("Mode Successfully Deleted\n");
				} else if (type == 4) {
					System.out.print("Enter the packet you want to change the format for: RRQ or WRQ \n");
					packetType = scan.nextLine();
					if (packetType.toLowerCase().equals("rrq")) {
						byteNumber = "01";
					} else if (packetType.toLowerCase().equals("wrq")) {
						byteNumber = "02";
					} else {
						System.out.println("Invalid input");
						continue;
					}
					scanning = true;
					int errorType = 0;
					while (scanning) {
						try {
							System.out.print("Enter 1 to delete the first 0, enter 2 to delete the second. \n");
							errorType = Integer.parseInt(scan.nextLine());
							scanning = false;
						} catch (Exception InputMismatchException) {
							System.out.println("Invalid input");
							continue;
						}
					}
					if (errorType == 1) {
						errors.put(byteNumber, 7);
						System.out.println("First 0 Seperator Successfully Deleted\n");
					} else if (errorType == 2) {
						errors.put(byteNumber, 8);
						System.out.println("Second 0 Seperator Successfully Deleted\n");
					} else {
						System.out.println("Invalid input");
						continue;
					}
				}
			} else if (type == 3) {
				System.out.print("Enter the packet you want to send from the wrong port: RRQ, WRQ, ACK, DATA \n");
				packetType = scan.nextLine();
				if (packetType.toLowerCase().equals("ack") || packetType.toLowerCase().equals("data")) {
					scanning = true;
					while (scanning) {
						try {
							System.out.print("Enter the block number of the packet: \n");
							packetNumber = Integer.parseInt(scan.nextLine());
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
				errors.put(byteNumber, 9);
				System.out.println("Wrong port Successfully Enabled \n");
			} else if (type == 4) {
				try {
					scan.close();
					shutdown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (type != 0 && type != 1 && type != 2 && type != 3 && type != 4) {
				System.out.println("Invalid request type");
				continue;
			}
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
		errorData = new HashMap<>(); // stores either the delay time or space between
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
	private static HashMap<String, Integer> ErrorData;
	private String clientByteNumber = "";
	private String serverByteNumber = "";
	private String prevClientByteNumber = "";
	private String prevServerByteNumber = "";
	private TFTPSim Parent;
	private boolean threadStopped = false;

	ReceivingAndSendingThread(DatagramSocket sendToReceiveFromServerSocket, DatagramSocket receiveFromClientSocket,
			DatagramSocket sendToClientSocket, TFTPSim parent, String hostName, HashMap<String, Integer> errors,
			HashMap<String, Integer> errorData) {
		HostName = hostName;
		SendToReceiveFromServerSocket = sendToReceiveFromServerSocket;
		ReceiveFromClientSocket = receiveFromClientSocket;
		SendToClientSocket = sendToClientSocket;
		this.Parent = parent;
		Errors = errors;
		ErrorData = errorData;
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
					int index = 0;
					byte[] newData;
					switch (type) {
					case 1:
						// Losing Packet
						System.out.println("losing packet");
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
							Thread.sleep(ErrorData.get(serverByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("sending delayed packet");
						SendToClient(data);
						break;
					case 3:
						// duplicating Packet
						System.out.println("duplicating packet");
						SendToClient(data);
						try {
							Thread.sleep(ErrorData.get(serverByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("sending duplicated packet");
						SendToClient(data);
						break;
					case 4:
						// Change Opcode
						System.out.println("changing opcode");
						int change = ErrorData.get(serverByteNumber);
						data[0] = (byte) (change / 256);
						data[1] = (byte) (change % 256);
						SendToClient(data);
						break;
					case 5:
						// Delete Filename
						System.out.println("deleting filename");
						index = 0;
						for (int x = 2; x < receivePacket.getLength(); x++) {
							if (data[x] == (byte) 0) {
								index = x;
								break;
							}
						}
						newData = Arrays.copyOfRange(data, index - 2, receivePacket.getLength());
						newData[0] = data[0];
						newData[1] = data[1];
						SendToClient(newData);
						break;
					case 6:
						// Delete Mode
						System.out.println("deleting mode");
						index = 0;
						for (int x = 2; x < receivePacket.getLength(); x++) {
							if (data[x] == (byte) 0) {
								index = x;
								break;
							}
						}
						newData = Arrays.copyOfRange(data, 0, index + 2);
						newData[index + 1] = 0;
						SendToClient(newData);
						break;
					case 7:
						System.out.println("deleting first zero");
						for (int x = 2; x < receivePacket.getLength(); x++) {
							if (data[x] == (byte) 0) {
								index = x;
								break;
							}
						}
						byte[] newSmallerData = new byte[receivePacket.getLength() - 1];
						for (int i = 0; i < newSmallerData.length; i++) {
							if (i < index) {
								newSmallerData[i] = data[i];
							} else {
								newSmallerData[i] = data[i + 1];
							}
						}
						SendToClient(newSmallerData);
						break;
					case 8:
						System.out.println("deleting second zero");
						newData = Arrays.copyOfRange(data, 0, receivePacket.getLength() - 1);
						SendToClient(newData);
						break;
					case 9:
						System.out.println("sending from wrong socket");
						DatagramSocket wrongSocket = null;
						try {
							wrongSocket = new DatagramSocket();
						} catch (SocketException e1) {
							e1.printStackTrace();
						}
						try {
							wrongSocket.send(new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(),
									Parent.getClientPort()));
						} catch (IOException e2) {
							e2.printStackTrace();
							System.exit(1);
						}
						break;
					}
				} else {
					SendToClient(data);
				}
				/*
				 * if (data[1] == 3 && sendPacket.getLength() < 516) { lastAck = true; }
				 */
			}
		} else if (HostName.equals("Client")) {
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
					int index = 0;
					byte[] newData;
					switch (type) {
					case 1:
						// Losing Packet
						System.out.println("losing packet");
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
						System.out.println("delaying");
						try {
							Thread.sleep(ErrorData.get(clientByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("sending delayed packet");
						SendToServer(data);
						break;
					case 3:
						// duplicating Packet
						System.out.println("duplicating packet");
						SendToServer(data);
						try {
							Thread.sleep(ErrorData.get(clientByteNumber) * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("sending duplicated packet");
						SendToServer(data);
						break;
					case 4:
						// Change Opcode
						System.out.println("changing opcode");
						int change = ErrorData.get(clientByteNumber);
						data[0] = (byte) (change / 256);
						data[1] = (byte) (change % 256);
						SendToServer(data);
						break;
					case 5:
						// Delete Filename
						System.out.println("deleting filename");
						index = 0;
						for (int x = 2; x < receivePacket.getLength(); x++) {
							if (data[x] == (byte) 0) {
								index = x;
								break;
							}
						}
						newData = Arrays.copyOfRange(data, index - 2, receivePacket.getLength());
						newData[0] = data[0];
						newData[1] = data[1];
						SendToServer(newData);
						break;
					case 6:
						// Delete Mode
						System.out.println("deleting mode");
						index = 0;
						for (int x = 2; x < receivePacket.getLength(); x++) {
							if (data[x] == (byte) 0) {
								index = x;
								break;
							}
						}
						newData = Arrays.copyOfRange(data, 0, index + 2);
						newData[index + 1] = 0;
						SendToServer(newData);
						break;
					case 7:
						System.out.println("deleting first zero");
						for (int x = 2; x < receivePacket.getLength(); x++) {
							if (data[x] == (byte) 0) {
								index = x;
								break;
							}
						}
						byte[] newSmallerData = new byte[receivePacket.getLength() - 1];
						for (int i = 0; i < newSmallerData.length; i++) {
							if (i < index) {
								newSmallerData[i] = data[i];
							} else {
								newSmallerData[i] = data[i + 1];
							}
						}
						SendToServer(newSmallerData);
						break;
					case 8:
						System.out.println("deleting second zero");
						newData = Arrays.copyOfRange(data, 0, receivePacket.getLength() - 1);
						SendToServer(newData);
						break;
					case 9:
						System.out.println("sending from wrong socket");
						DatagramSocket wrongSocket = null;
						try {
							wrongSocket = new DatagramSocket();
						} catch (SocketException e1) {
							e1.printStackTrace();
						}
						try {
							wrongSocket.send(new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(),
									Parent.getServerPort()));
						} catch (IOException e2) {
							e2.printStackTrace();
							System.exit(1);
						}
						break;
					}
				} else {
					SendToServer(data);
				}
			}
		}
		// return
	}

	public byte[] ReceiveFromClient() {
		byte[] data = new byte[516];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Simulator: Waiting for packet from client");
		// Block until a datagram packet is received from receiveSocket.
		try {
			ReceiveFromClientSocket.receive(receivePacket);
		} catch (IOException e) {
			// e.printStackTrace();
			threadStopped = true;
			return data;
			// System.exit(1);
		}

		// Process the received datagram.
		System.out.print("Simulator: received from Client: ");
		if (data[1] == 3)
			System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 4)
			System.out.println("ACK " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 5)
			System.out.println("ERROR");
		System.out.println("From host: " + receivePacket.getAddress());
		Parent.setClientPort(receivePacket.getPort());
		System.out.println("Host port: " + Parent.getClientPort());

		return data;
	}

	public void SendToServer(byte[] data) {

		// Reset sending port on a new transfer
		if (data[1] == 1 || data[1] == 2) {
			try {
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(), 69);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		} else {
			try {
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(),
						Parent.getServerPort());
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}

		System.out.print("Simulator: sending to Server: ");
		if (data[1] == 3)
			System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 4)
			System.out.println("ACK " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 5)
			System.out.println("ERROR");
		System.out.println("From host: " + sendPacket.getAddress());
		System.out.println("Host port: " + sendPacket.getPort());
		System.out.println("Length: " + sendPacket.getLength() + "\n");

		try {
			SendToReceiveFromServerSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			// System.exit(1);
		}
	}

	public byte[] ReceiveFromServer() {
		byte[] data = new byte[516];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Simulator: Waiting for packet from server");
		try {
			SendToReceiveFromServerSocket.receive(receivePacket);
		} catch (IOException e) {
			// e.printStackTrace();
			threadStopped = true;
			return data;
			// System.exit(1);
		}

		// Switch sending port while in a transfer
		Parent.setServerPort(receivePacket.getPort());

		System.out.print("Simulator: received from Server: ");
		if (data[1] == 3)
			System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 4)
			System.out.println("ACK " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 5)
			System.out.println("ERROR");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		System.out.println("Length: " + receivePacket.getLength() + "\n");

		return data;
	}

	public void SendToClient(byte[] data) {
		try {
			sendPacket = new DatagramPacket(data, receivePacket.getLength(), InetAddress.getLocalHost(),
					Parent.getClientPort());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		System.out.print("Simulator: sending to Client: ");
		if (data[1] == 3)
			System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 4)
			System.out.println("ACK " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
		else if (data[1] == 5)
			System.out.println("ERROR");
		System.out.println("From host: " + sendPacket.getAddress());
		System.out.println("Host port: " + sendPacket.getPort());
		System.out.println("Length: " + sendPacket.getLength() + "\n");

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