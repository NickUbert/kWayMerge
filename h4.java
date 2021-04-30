import java.io.*;
import java.util.*;

public class h4 {
    public static void main(String args[]) throws IOException {
        int numBuffers = Integer.parseInt(args[2]);
        int pageSize = Integer.parseInt(args[3]);
        new ExternalSorter(args[0], args[1], numBuffers, pageSize);
        //check that the output file in sorted
        RandomAccessFile f = new RandomAccessFile(args[1], "r");
        f.seek(0);
        int n1 = f.readInt();
        while (f.getFilePointer() < f.length()) {
            int n2 = f.readInt();
            if (n2 < n1) {
                System.out.println("Sort Failed");
                System.exit(1);
            }
            n1 = n2;
        }
        System.out.println("Sort Succeeded");
        f.close();
    }
}
