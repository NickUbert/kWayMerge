import java.io.*;
import java.util.*;

public class BinaryToASCII {
	public static void makeInts(String out, int numInts, int maxInt, long seed) throws IOException {
		DataOutputStream nums = new DataOutputStream(new FileOutputStream(out));
		Random r = new Random(seed);
		for (int i = 0; i < numInts; i++) {
			nums.writeInt(r.nextInt(maxInt));
		}
		nums.close();
	}

	public static void printInts(String in) throws IOException {
		DataInputStream nums = new DataInputStream(new FileInputStream(in));
		Boolean EOF = false;
		while (!EOF) {
			try {
				System.out.println(nums.readInt());
			} catch (EOFException e) {
				EOF = true;
			}
		}
		nums.close();
	}

	public static void main(String args[]) throws IOException {
		if (args[0].equals("Make")) {
			int numInts = Integer.parseInt(args[2]);
			int maxInt = Integer.parseInt(args[3]);
			long seed = (long) Integer.parseInt(args[4]);
			makeInts(args[1], numInts, maxInt, seed);
		} else { // args[0] should be "Print"
			printInts(args[1]);
		}
	}
}