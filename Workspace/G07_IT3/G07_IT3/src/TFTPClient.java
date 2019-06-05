
// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class TFTPClient {

	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;

	// private InetAddress expectedAddress;
	// private SocketAddress expectedSocket;

	String clientDirectory = "TFTPClient";

	/**
	 * Sends and Receives packets of a file from a server
	 * 
	 * @param type       The operation to be performed by the process
	 * @param filename   THe file to be transferred
	 * @param dataType   The packet's encoding type
	 * @param outputMode Controls if print statements are used during the process
	 * @param testMode   Controls if an intermediate host is used
	 */
	public void sendAndReceive(int type, String filename, String dataType, String outputMode, int testMode,
			String directory) {

		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}

		byte[] msg = new byte[516], // message we send
				fn, // filename as an array of bytes
				md, // mode as an array of bytes
				data; // reply as array of bytes
						// dataType as Strings
		int len, sendPort, transactionPort = -1;

		// In the assignment, students are told to send to 23, so just:
		// sendPort = 23;
		// is needed.
		// However, in the project, the following will be useful, except
		// that test vs. normal will be entered by the user.
		// change to NORMAL to send directly to server

		if (testMode == 0) // Mode 0 is Normal, 1 is Testing
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
		System.arraycopy(fn, 0, msg, 2, fn.length);
		// format is: source array, source index, dest array,
		// dest index, # array elements to copy
		// i.e. copy fn from 0 to fn.length to msg, starting at
		// index 2

		// now add a 0 byte
		msg[fn.length + 2] = 0;

		// now add "octet" (or "netascii")
		dataType = "octet";
		// convert to bytes
		md = dataType.getBytes();

		// and copy into the msg
		System.arraycopy(md, 0, msg, fn.length + 3, md.length);

		len = fn.length + md.length + 4; // length of the message
		// length of filename + length of dataType + opcode (2) + two 0s (2)
		// second 0 to be added next:

		// end with another 0 byte
		msg[len - 1] = 0;

		// Construct a datagram packet that is to be sent to a specified port
		// on a specified host.

		try {
			sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (outputMode.equals("verbose")) {

			System.out.print("Client: sending ");
			if (type == 1)
				System.out.println("RRQ");
			else if (type == 2)
				System.out.println("WRQ");

			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			System.out.println("Length: " + sendPacket.getLength() + "\n");
		}

		FileInputStream input = null;
		FileOutputStream output = null;

		// Form a String from the byte array, and print the string.
		String sending = new String(msg, 0, len);
		if (outputMode.equals("verbose"))
			System.out.println(sending);

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

		// Timeout after 5 seconds if sending data. On timeout re-send last packet, up
		// to three times until quit

		try {
			sendReceiveSocket.setSoTimeout(5000);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (int i = 1; i > 0; i++) {

			try {
				// Block until a datagram is received via sendReceiveSocket.
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {

				if (i <= 3) {
					// re-send packet
					if (outputMode.equals("verbose"))
						System.out.println("Timeout: Re-sending last request packet");
					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e1) {
						e1.printStackTrace();
						System.exit(1);
					}
					continue;
				}

			}

			if (i > 3) {
				if (outputMode.equals("verbose"))
					System.out.println("Timeout: Shutting Down");
				System.exit(0);
			} else {
				int sourcePort = receivePacket.getPort();
				if (transactionPort == -1)
					transactionPort = sourcePort;
				if (transactionPort != sourcePort) {
					// ERROR CODE 5
					if (outputMode.equals("verbose"))
						System.out.println("Expected TID " + transactionPort + " but received TID: " + sourcePort);
					sendErrorPacket(5, "Incorrect TID (Wrong port)", sourcePort, receivePacket.getAddress());
				}
				break;
			}
		}

		try {
			sendReceiveSocket.setSoTimeout(0);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Initialize file I/O structures
		File destinationFile = new File(directory + "/" + filename);

		if (type == 2) {
			try {
				input = new FileInputStream(destinationFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		} else if (type == 1) {

			try {
				output = new FileOutputStream(destinationFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}

		if (outputMode.equals("verbose"))
			System.out.println("Begin Receiving / Sending Data\n");

		int expectedBlockNum;

		if (type == 2)
			expectedBlockNum = 0;
		else
			expectedBlockNum = 1;

		while (true) {

			// Validate received packet

			// Test Opcode
			if (!(data[0] == 0 && ((data[1] == 3) || (data[1] == 4) || (data[1] == 5))))
				sendErrorPacket(4, "Received Opcode is Invalid", receivePacket.getPort(), receivePacket.getAddress());

			// Test Error Messages
			if (data[1] == 5) {

				// Examine Formatting
				if ((data.length < 5) || (data[data.length - 1] != 0))
					sendErrorPacket(4, "Received Error Message has Invalid Formatting", receivePacket.getPort(),
							receivePacket.getAddress());

				// Expand in Iteration 4
				if (!(data[2] == 0 && ((Byte.toUnsignedInt(data[3]) >= 4) || (Byte.toUnsignedInt(data[3]) <= 5))))
					sendErrorPacket(4, "Received ErrorCode is Invalid", receivePacket.getPort(),
							receivePacket.getAddress());

			} else {
				// Test Data or Ack
				if (data.length < 4)
					sendErrorPacket(4, "Received Message has Invalid Formatting", receivePacket.getPort(),
							receivePacket.getAddress());

			}

			// wait for new packet

			// check if it's read or write
			data = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());

			// Process the received datagram.
			if (outputMode.equals("verbose")) {

				System.out.print("Client: received ");
				if (data[1] == 3)
					System.out.println("DATA " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
				else if (data[1] == 4)
					System.out.println("ACK " + (Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3])));
				else if (data[1] == 5)
					System.out.println("ERROR");

				System.out.println("From host: " + receivePacket.getAddress());
				System.out.println("Host port: " + receivePacket.getPort());
				System.out.println("Length: " + receivePacket.getLength());
				System.out.println();
			}

			if (data[1] == 3) {
				// Parsing DATA packet
				// READ
				int blockNumber = Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3]);

				if (blockNumber == expectedBlockNum) {
					// Output data to file
					try {
						output.write(data, 4, data.length - 4);
					} catch (IOException e) {
						e.printStackTrace();
					}
					expectedBlockNum = (expectedBlockNum + 1) % 65536;
				} else {
					if (outputMode.equals("verbose"))
						System.out.println("\nDuplicate/Out-of-order DATA packet " + blockNumber + ", expected: "
								+ expectedBlockNum + "\n");
				}

				// Creating and sending response
				byte[] bytes = { 0, 4, (byte) (blockNumber / 256), (byte) (blockNumber % 256) };

				if (testMode == 0) {
					sendPacket = new DatagramPacket(bytes, bytes.length, receivePacket.getAddress(),
							receivePacket.getPort());
				} else {
					sendPacket = new DatagramPacket(bytes, bytes.length, receivePacket.getAddress(), sendPort);
				}

				if (outputMode.equals("verbose")) {
					System.out.println("Client: sending ACK " + blockNumber);
					System.out.println("To host: " + sendPacket.getAddress());
					System.out.println("Destination host port: " + sendPacket.getPort());

					System.out.println("Length: " + sendPacket.getLength() + "\n");
				}

				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				if (data.length < 516) {
					// Closes the stream, file is complete
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					break;
				}

			} else if (data[1] == 4) {
				// Parsing ACK packet
				// WRITE

				// Prepare data with wrapper
				int blockNumber = Byte.toUnsignedInt(data[2]) * 256 + Byte.toUnsignedInt(data[3]);

				if (blockNumber == expectedBlockNum) {

					blockNumber = (blockNumber + 1) % 65536;

					int sendingSize = (blockNumber * 512 > destinationFile.length())
							? ((int) destinationFile.length() % 512)
							: (512);
					byte[] bytes = new byte[sendingSize + 4];
					bytes[0] = 0;
					bytes[1] = 3;
					bytes[2] = (byte) (blockNumber / 256);
					bytes[3] = (byte) (blockNumber % 256);

					// insert data from file
					try {
						input.read(bytes, 4, sendingSize);
					} catch (IOException e) {
						e.printStackTrace();
					}
					expectedBlockNum = (expectedBlockNum + 1) % 65536;
					if (testMode == 0) {
						sendPacket = new DatagramPacket(bytes, bytes.length, receivePacket.getAddress(),
								receivePacket.getPort());
					} else {
						sendPacket = new DatagramPacket(bytes, bytes.length, receivePacket.getAddress(), sendPort);
					}

					if (outputMode.equals("verbose")) {
						System.out.println("Client: sending DATA " + blockNumber);
						System.out.println("To host: " + sendPacket.getAddress());
						System.out.println("Destination host port: " + sendPacket.getPort());
						System.out.println("Length: " + sendPacket.getLength() + "\n");
					}

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
							e.printStackTrace();
						}
						if(outputMode.equals("verbose")) System.out.println("Transfer Complete");
						break;
					}
				} else {
					if (outputMode.equals("verbose"))
						System.out.println(
								"\nDuplicate/Out-of-order ACK packet" + blockNumber + " " + expectedBlockNum + "\n");
				}
			} else if (data[1] == 5) {
				// PRINT AND SHUTDOWN
				if (data[3] == 5) {
					if (outputMode.equals("verbose")) System.out.println("Received Error " + Byte.toUnsignedInt(data[3])+": "+ ( new String(Arrays.copyOfRange(data, 4, data.length-1))));
				}
				else {
					if (outputMode.equals("verbose")) System.out.println("Received Error " + Byte.toUnsignedInt(data[3])+": "+ ( new String(Arrays.copyOfRange(data, 4, data.length-1)) ) + " Shutting Down" ); 
					System.exit(0);
				}
			}

			// Timeout after 5 seconds if sending data. On timeout re-send last packet, up
			// to three times until quit

			if (type == 2) { // If sending data
				try {
					sendReceiveSocket.setSoTimeout(5000);
				} catch (SocketException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			for (int i = 1; i > 0; i++) {

				try {
					// Block until a datagram is received via sendReceiveSocket.
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {

					if (type == 2 && i <= 3) {
						// re-send packet
						if (outputMode.equals("verbose"))
							System.out.println("Timeout: Re-sending last data packet");
						try {
							sendReceiveSocket.send(sendPacket);
						} catch (IOException e1) {
							e1.printStackTrace();
							System.exit(1);
						}
						continue;
					}

				}

				if (i > 3) {
					if (outputMode.equals("verbose"))
						System.out.println("Timeout: Shutting Down");
					System.exit(0);
				} else {
					int sourcePort = receivePacket.getPort();
					if (transactionPort != sourcePort) {
						// ERROR CODE 5
						sendErrorPacket(5, "Incorrect TID (Wrong port)", sourcePort, receivePacket.getAddress());
						i--;
						continue;
					}
					break;
				}
			}

			if (type == 2) { // If sending data
				try {
					sendReceiveSocket.setSoTimeout(0);
				} catch (SocketException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

		}

		// We're finished, so close the socket.
		sendReceiveSocket.close();
	}

	public static void main(String args[]) // UI
	{
		TFTPClient c = new TFTPClient();
		Scanner scan = new Scanner(System.in);
		int type, testModeInt;
		String filename, datatype, outputMode, directory;
		System.out.println("TFTP Client: Interation 3\n");
		datatype = "octet";
		while (true) {
			System.out.print("Enter read or write request: \n");
			String request = scan.next();
			if (request.equals("read")) {
				type = 1;
			} else if (request.equals("write")) {
				type = 2;
			} else {
				System.out.println("Invalid request type");
				continue;
			}
			System.out.print("Enter filename: \n");
			filename = scan.next();
			System.out.print("Enter file directory: \n");
			directory = scan.next();
			System.out.print("Enter quiet or verbose mode: \n");
			outputMode = scan.next();
			if (!((outputMode.equals("quiet")) || (outputMode.equals("verbose")))) {
				System.out.println("Invalid mode");
				continue;
			}
			System.out.print("Enter normal or test mode: \n");
			String testModeString = scan.next();
			if (testModeString.equals("normal")) {
				testModeInt = 0;
			} else if (testModeString.equals("test")) {
				testModeInt = 1;
			} else {
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
				while (true) {
					System.out.print("Enter read or write request: \n");
					String request = scan.next();
					if (request.equals("read")) {
						type = 1;
					} else if (request.equals("write")) {
						type = 2;
					} else {
						System.out.println("Invalid request type");
						continue;
					}
					System.out.print("Enter filename: \n");
					filename = scan.next();
					System.out.print("Enter file directory: \n");
					directory = scan.next();
					System.out.print("Enter quiet or verbose mode: \n");
					outputMode = scan.next();
					if (!((outputMode.equals("quiet")) || (outputMode.equals("verbose")))) {
						System.out.println("Invalid mode");
						continue;
					}
					System.out.print("Enter normal or test mode: \n");
					String testModeString = scan.next();
					if (testModeString.equals("normal")) {
						testModeInt = 0;
					} else if (testModeString.equals("test")) {
						testModeInt = 1;
					} else {
						System.out.println("Invalid request type");
						continue;
					}
					System.out.println("\nRequest initialisation successful\n");
					break;
				}
			} else if (s.equals("shutdown")) {
				scan.close();
				System.exit(0);
			} else if (s.equals("help")) {
				System.out.println("help: The help command");
				System.out.println("init: Can initialise a new request");
				System.out.println("run: Run a request");
				System.out.println("shutdown: Shutsdown client");
				System.out.println("read: Sets to a read request");
				System.out.println("write: Sets to a write request");
				System.out.println("filename: Type in a new filename after the filename command.");
				System.out.println("directory: Type in a new directory after the directory command.");
				System.out.println("octet: Sets to a octet datatype");
				System.out.println("netascii: Sets to a netascii datatype");
				System.out.println("quiet: Sets to quiet mode");
				System.out.println("verbose: Sets to verbose mode");
				System.out.println("normal: Sets to normal mode");
				System.out.println("test: Sets to test mode");
			} else if (s.equals("run")) {
				c.sendAndReceive(type, filename, datatype, outputMode, testModeInt, directory);
			} else if (s.equals("read")) {
				type = 1;
				System.out.println("Client is now set to do a read request");
			} else if (s.equals("write")) {
				type = 2;
				System.out.println("Client is now set to do a write request");
			} else if (s.equals("filename")) {
				System.out.println("Enter Filename: ");
				filename = scan.next();
				System.out.println("filename has been changed to: " + filename);
			} else if (s.equals("directory")) {
				System.out.println("Enter directory: ");
				directory = scan.next();
				System.out.println("directory has been changed to: " + directory);
			} else if (s.equals("octet")) {
				datatype = "octet";
				System.out.println("Client is now set to octet datatype");
			} else if (s.equals("netascii")) {
				datatype = "netascii";
				System.out.println("Client is now set to netascii datatype");
			} else if (s.equals("quiet")) {
				outputMode = "quiet";
				System.out.println("Client is now set to quiet mode");
			} else if (s.equals("verbose")) {
				outputMode = "verbose";
				System.out.println("Client is now set to verbose mode");
			} else if (s.equals("normal")) {
				testModeInt = 0;
				System.out.println("Client is now set to normal mode");
			} else if (s.equals("test")) {
				testModeInt = 1;
				System.out.println("Client is now set to test mode");
			}
		}
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
		System.out.println("Sending Error to:" + port); 
		try {
			sendReceiveSocket.send(new DatagramPacket(errorPacket, errorPacket.length, dest, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (errorCode != 5) {
			System.out.println("Shutting down");
			System.exit(0);
		}
	}

}
