import java.util.*;
public class ErrorCollectionTestProgram {

	public static void main(String[] args) {
		HashMap<Integer, Integer> errors = new HashMap<>();
		HashMap<Integer, Integer> delayAndSpace = new HashMap<>();
		
		Scanner scan = new Scanner(System.in);
		int type, testModeInt;
		String filename, datatype, outputMode;
		System.out.println("Error Collection Test Program\n");
		   while (true) {
			   System.out.print("Enter 0 for normal operation, 1 for loss, 2 for delay, or 3 for duplicate: \n");
			   String request = scan.next();
			   if (request.equals("0")) {
				   type = 0;
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

	}

}
