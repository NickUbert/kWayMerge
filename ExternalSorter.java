import java.io.*;
import java.util.*;
import java.nio.file.*;

public class ExternalSorter {
	// implements K-Way External Merge Sort

	int[][] buffers;
	int numBuffers;
	int pageSize;
	RandomAccessFile inFile;
	RandomAccessFile outFile;
	static long writePointer;
	static long readPointer;

	public ExternalSorter(String in, String out, int nBuffers, int pSize) throws IOException {
		// in is the name of an unsorted binary file of ints
		// out is the name of the output binary file (the destination of the sorted
		// ints)
		// nBuffers is the number of in memory page buffers available for sorting
		// pSize is the number of ints in a page

		// Initialize global variables
		inFile = new RandomAccessFile(in, "rw");
		outFile = new RandomAccessFile(out, "rw");
		outFile.setLength(0);
		pageSize = pSize;
		numBuffers = nBuffers;

		writePointer = 0;
		readPointer = 0;

		buffers = new int[numBuffers][pageSize];
		// Begin by partial sorting the input into (inputSize/pageSize) sorted sequences
		partialSort();

		while (!outputSorted()) {
			// Repeat merge phase until the number of sorted sequences == 1
			readPointer = 0;
			writePointer = 0;
			for (int i = 0; i < numBuffers - 1; i++) {
				buffers[i] = readNextPage(outFile);
			}
			mergePhase();
		}
	}

	private void partialSort() {
		// Quicksort input and write to output using buffers
		try {
			while (inFile.getFilePointer() < inFile.length()) {
				for (int i = 0; i < numBuffers; i++) {
					buffers[i] = readNextPage(inFile);
					quickSort(buffers[i], 0, pageSize - 1);
					writeOutput(buffers[i]);
				}

			}
		} catch (IOException e) {
			System.out.println("#Error during partial sorting phase");
			e.printStackTrace();
		}

	}

	public void mergePhase() {
		// Pre: Assume buffers has been populated
		// Assume partial sort has been completed and the buffers contain sorted
		// sequences.
		// Assume that input has been padded with INTEGER.MIN_VALUEs

		// Initialize values
		int k = numBuffers - 1;
		int[] index = new int[k];
		int[] output = new int[pageSize];
		int outIndex = 0;

		while (!indexEmpty(index)) {
			// Reset comparison variables
			int low = Integer.MAX_VALUE;
			int bIndex = -1;

			// Make comparison pass
			for (int i = 0; i < k; i++) {
				if (index[i] != Integer.MAX_VALUE) {
					int cur = buffers[i][index[i]];
					if (cur <= low) {
						bIndex = i;
						low = cur;
					}

				}
			}
			// Add current lowest key to output and advance corresponding index
			if (low == Integer.MIN_VALUE) {
				// This signals that the end of file was reached and a page wasn't fully filled
				// update the boolean so that no more pages will be read in.
				// Update the index value to avoid reading from this column while the rest
				// finish.
				index[bIndex] = Integer.MAX_VALUE;
			} else {
				index[bIndex]++;
				// Page Exhausted but there is more to be read
				if (index[bIndex] >= pageSize) {
					// reset the index counter
					index[bIndex] = 0;
					buffers[bIndex] = readNextPage(outFile);
					// close out access to buffer elements since end of array was reached.
				}
			}

			output[outIndex] = low;
			outIndex++;

			// If output buffer is full
			if (outIndex == pageSize) {
				// Write to file
				writeOutput(output);
				// Reset output variables
				outIndex = 0;
				output = new int[pageSize];
			}
		}
	}

	private boolean outputSorted() {
		// Iterate through output file and compare neighboring values to make sure
		// everything's sorted
		int n2;
		try {
			if (outFile.length() < 4) {
				return false;
			}
			outFile.seek(0);
			int n1 = outFile.readInt();
			while (outFile.getFilePointer() < outFile.length()) {
				n2 = outFile.readInt();
				if (n2 < n1) {
					return false;
				}
				n1 = n2;
			}
		} catch (IOException e) {
			System.out.println("#Error while verifying sorted order in output file");
			e.printStackTrace();
		}
		return true;
	}

	private int[] readNextPage(RandomAccessFile file) {
		int[] nextRow = new int[pageSize];
		// Start by padding array in case EOF is reached

		for (int i = 0; i < pageSize; i++) {
			nextRow[i] = Integer.MIN_VALUE;
		}

		try {
			// Until either EOF or array is filled, read ints from file
			for (int i = 0; i < pageSize; i++) {
				if (readPointer > file.length() - 4) {
					break;
				}
				file.seek(readPointer);
				nextRow[i] = file.readInt();
				readPointer = file.getFilePointer();

			}
		} catch (IOException e) {
			System.out.println("#Error while reading input");
			e.printStackTrace();
		}

		return nextRow;
	}

	private void writeOutput(int[] output) {
		// Since we always read from input
		// We will only ever write sorted outputs to output file
		// THerefore the pointer will always be accurate, no need to seek?
		for (int i = 0; i < pageSize; i++) {
			if (output[i] != Integer.MIN_VALUE) {
				try {
					outFile.seek(writePointer);
					outFile.writeInt(output[i]);
					writePointer = outFile.getFilePointer();
				} catch (IOException e) {
					System.out.println("#Error while writing output");
					e.printStackTrace();
				}
			}
		}
	}

	private boolean indexEmpty(int[] cur) {
		// Check to see if any input array hasn't been exhausted
		for (int i = 0; i < cur.length; i++) {
			if (cur[i] != Integer.MAX_VALUE)
				// If an index exists that still contains input keys
				return false;
		}
		// All input arrays have been exhausted
		return true;
	}

	/*
	 * 
	 * All code below this point was taken from a quicksort implementation found on
	 * GeeksForGeeks.com, no need to reinvent the wheel.
	 * 
	 */
	static void swap(int[] arr, int i, int j) {
		int temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}

	static int partition(int[] arr, int low, int high) {

		int pivot = arr[high];

		int i = (low - 1);

		for (int j = low; j <= high - 1; j++) {

			if (arr[j] < pivot) {

				i++;
				swap(arr, i, j);
			}
		}
		swap(arr, i + 1, high);
		return (i + 1);
	}

	static void quickSort(int[] arr, int low, int high) {

		if (low < high) {
			int pi = partition(arr, low, high);

			quickSort(arr, low, pi - 1);
			quickSort(arr, pi + 1, high);
		}
	}
}