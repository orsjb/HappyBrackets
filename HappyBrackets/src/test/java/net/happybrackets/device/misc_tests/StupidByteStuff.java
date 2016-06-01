package net.happybrackets.device.misc_tests;

public class StupidByteStuff {

	public static void main(String[] args) {
		
		
		
	}
	
	public static void test2() {
		
		// System.out.println(String.format("%08b", (0x80 | 0x28)));
		// System.out.println(Integer.toBinaryString((byte)2 << 8));

		byte a = 1;
		byte b = 2;

		System.out.print("a: ");
		boolean[] abits = getBits(a);
		for(boolean v : abits) {
			System.out.print(v?1:0);
		}
		System.out.println();
	
		System.out.print("b: ");
		boolean[] bbits = getBits(b);
		for(boolean v : bbits) {
			System.out.print(v?1:0);
		}
		System.out.println();
		
		//getInstance 2s complement short
		boolean[] myShortBits = new boolean[16];
		for(int i = 0; i < 8; i++) {
			myShortBits[i] = bbits[i];
			myShortBits[i+8] = abits[i];
		}
		
		
		
	}

	private static boolean[] getBits(byte inByte) {
		boolean[] bits = new boolean[8];
		for (int j = 0; j < 8; j++) {
			// Shift each bit by 1 starting at zero shift
			byte tmp = (byte) (inByte >> j);
			// Check byte with mask 00000001 for LSB
			bits[7-j] = (tmp & 0x01) == 1;
		}
		return bits;
	}

}
