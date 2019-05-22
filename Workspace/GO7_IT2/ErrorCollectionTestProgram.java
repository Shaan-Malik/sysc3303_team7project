import java.util.*;
public class ErrorCollectionTestProgram {

	public static void main(String[] args) {
		HashMap<String, Integer> errors = new HashMap<>(); // stores a key-value pair: the unique code based on block number, and the type of error
		HashMap<String, Integer> delayAndSpace = new HashMap<>(); // stores either the delay time or space between duplicates.
		
		Scanner scan = new Scanner(System.in);
		boolean flag = true;
		while (flag == true) {
			int type, packetNumber = 0, timeOrSpace = 0;
			String byteNumber = null;
			String packetType;
			System.out.println("Error Collection Test Program\n");
			   while (true) {
				   System.out.print("Enter 0 for normal operation, 1 for loss, 2 for delay, or 3 for duplicate: \n");
				   String request = scan.next();
				   if (request.equals("q")) {
					   System.out.println("Quitting");
					   flag = false;
					   scan.close();
					   break;
				   }
				   if (request.equals("0")) {
					   type = 0;
				   }
				   else if (request.equals("1")) {
					   type = 1;
				   } else if (request.equals("2")) {
					   type = 2;
				   } else if (request.equals("3")) {
					   type = 3;
				   }
				   else {
					   System.out.println("Invalid request type");
					   continue;
				   }
				   System.out.print("Enter the request you want to simulate: RRQ, WRQ, ACK, DATA \n");
				   packetType = scan.next();
				   if (packetType.toLowerCase().equals("ack") || packetType.toLowerCase().equals("data")) {
					   System.out.print("Enter the number of the packet: \n");
					   packetNumber = Integer.parseInt(scan.next());
				   }
				   if (type > 1) {
				   if (type == 2) {
					   System.out.print("Enter the delay time in seconds: \n");
				   } else if (type == 3) {
					   System.out.print("Enter the space between duplicates: \n");
				   }
				   timeOrSpace = Integer.parseInt(scan.next());
				   }
				   if (packetType.toLowerCase().equals("rrq")) {
					   byteNumber = "0100";
				   } else if (packetType.toLowerCase().equals("wrq")) {
					   byteNumber = "0200";
				   } else if (packetType.toLowerCase().equals("ack")) {
					   int b1 = packetNumber/256;
					   int b2 = packetNumber%256;
					   byteNumber = "04" + "." + Integer.toString(b1) + "." + Integer.toString(b2);
				   } else if (packetType.toLowerCase().equals("data")) {
					   int b1 = packetNumber/256;
					   int b2 = packetNumber%256;
					   byteNumber = "03" + "." + Integer.toString(b1) + "." + Integer.toString(b2);
				   }
				   errors.put(byteNumber, type);
				   delayAndSpace.put(byteNumber, timeOrSpace);
				   
				   System.out.println("errors[" + byteNumber + "] = " + type);
				   System.out.println("delayAndSpace[" + byteNumber + "] = " + timeOrSpace);
				   System.out.println("\nRequest initialisation successful\n");
				   
				   break;
			   }
		}

	}

}
