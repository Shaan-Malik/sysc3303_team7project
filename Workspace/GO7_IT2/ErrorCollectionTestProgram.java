import java.util.*;
public class ErrorCollectionTestProgram {

	public static void main(String[] args) {
		HashMap<Integer, Integer> errors = new HashMap<>();
		HashMap<Integer, Integer> delayAndSpace = new HashMap<>();
		
		Scanner scan = new Scanner(System.in);
		int type, packetNumber = 0, timeOrSpace = 0, byteNumber = 0;
		String packetType;
		System.out.println("Error Collection Test Program\n");
		   while (true) {
			   System.out.print("Enter 0 for normal operation, 1 for loss, 2 for delay, or 3 for duplicate: \n");
			   String request = scan.next();
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
			   if (packetType.equals("ACK") || packetType.equals("DATA")) {
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
			   if (packetType.equals("RRQ")) {
				   byteNumber = Integer.parseInt("0100");
			   } else if (packetType.equals("WRQ")) {
				   byteNumber = Integer.parseInt("0200");
			   } else if (packetType.equals("ACK")) {
				   byte b1 = (byte)(packetNumber/256);
				   byte b2 = (byte)(packetNumber%256);
				   byteNumber = Integer.parseInt("04" + Byte.toString(b1) + Byte.toString(b2));
			   } else if (packetType.equals("DATA")) {
				   byte b1 = (byte)(packetNumber/256);
				   byte b2 = (byte)(packetNumber%256);
				   byteNumber = Integer.parseInt("03" + Byte.toString(b1) + Byte.toString(b2));
			   }
			   errors.put(byteNumber, type);
			   delayAndSpace.put(byteNumber, timeOrSpace);
			   
			   System.out.println(byteNumber);
			   System.out.println("\nRequest initialisation successful\n");
			   scan.close();
			   break;
		   }

	}

}
