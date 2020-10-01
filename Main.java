package correcter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static Random random = new Random();

    enum Mode {
        ENCODE, SEND, DECODE
    }

    public static byte[] encoding(byte[] input) {
        StringBuilder bin_str = new StringBuilder();
        StringBuilder bin_str_encoded = new StringBuilder();
        for (byte b : input) {
            bin_str.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ", "0"));
        }
        System.out.println(bin_str);

        for (int i = 0, parityBit; i < bin_str.length(); i += 4) {
            parityBit = Character.getNumericValue(bin_str.charAt(i)) ^
                    Character.getNumericValue(bin_str.charAt(i + 1)) ^
                    Character.getNumericValue(bin_str.charAt(i + 3));
            bin_str_encoded.append(parityBit);
            parityBit = Character.getNumericValue(bin_str.charAt(i)) ^
                    Character.getNumericValue(bin_str.charAt(i + 2)) ^
                    Character.getNumericValue(bin_str.charAt(i + 3));
            bin_str_encoded.append(parityBit);
            bin_str_encoded.append(bin_str.charAt(i));
            parityBit = Character.getNumericValue(bin_str.charAt(i + 1)) ^
                    Character.getNumericValue(bin_str.charAt(i + 2)) ^
                    Character.getNumericValue(bin_str.charAt(i + 3));
            bin_str_encoded.append(parityBit);
            bin_str_encoded.append(bin_str.charAt(i + 1));
            bin_str_encoded.append(bin_str.charAt(i + 2));
            bin_str_encoded.append(bin_str.charAt(i + 3));
            bin_str_encoded.append(0);
        }
        return getBytes(bin_str_encoded);
    }

    public static byte[] erroring(byte[] input) {
        byte[] errored = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            errored[i] = input[i];
            errored[i] ^= 1 << random.nextInt(7) + 1;
        }
        return errored;
    }

    public static byte[] decoding(byte[] input) {
        StringBuilder bin_str = new StringBuilder();
        StringBuilder bin_str_decoded = new StringBuilder();
        for (byte b : input) {
            bin_str.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(" ", "0"));
        }

        for (int i = 0, j, bits_check1, bits_check2, bits_check4; i < bin_str.length(); i += 8) {
            j = -1;
            System.out.println("\"" + (i >> 3) + "\"\n" + bin_str.substring(i, i + 8));
            bits_check1 = Character.getNumericValue(bin_str.charAt(i + 2)) ^
                    Character.getNumericValue(bin_str.charAt(i + 4)) ^
                    Character.getNumericValue(bin_str.charAt(i + 6));

            bits_check2 = Character.getNumericValue(bin_str.charAt(i + 2)) ^
                    Character.getNumericValue(bin_str.charAt(i + 5)) ^
                    Character.getNumericValue(bin_str.charAt(i + 6));
            bits_check4 = Character.getNumericValue(bin_str.charAt(i + 4)) ^
                    Character.getNumericValue(bin_str.charAt(i + 5)) ^
                    Character.getNumericValue(bin_str.charAt(i + 6));
            if (bits_check1 != Character.getNumericValue(bin_str.charAt(i))) {
                j += 1;
            }
            if (bits_check2 != Character.getNumericValue(bin_str.charAt(i + 1))) {
                j += 2;
            }
            if (bits_check4 != Character.getNumericValue(bin_str.charAt(i + 3))) {
                j += 4;
            }
            if ((Character.getNumericValue(bin_str.charAt(i + j)) ^ 1) == 1)
                bin_str.setCharAt(i + j, '1');
            else
                bin_str.setCharAt(i + j, '0');
            bin_str_decoded.append(Character.getNumericValue(bin_str.charAt(i + 2)));
            bin_str_decoded.append(Character.getNumericValue(bin_str.charAt(i + 4)));
            bin_str_decoded.append(Character.getNumericValue(bin_str.charAt(i + 5)));
            bin_str_decoded.append(Character.getNumericValue(bin_str.charAt(i + 6)));
            System.out.println(bin_str_decoded.substring(i >> 1, (i >> 1) + 4));

        }
        return getBytes(bin_str_decoded);
    }

    private static byte[] getBytes(StringBuilder bin_str) {
        int i;
        int length;
        int remainder = bin_str.length() % 8;
        System.out.println(bin_str.length());
        if (bin_str.charAt(bin_str.length() - 1) == '0' && remainder == 1) {
            bin_str.deleteCharAt(bin_str.length() - 1);
            remainder = bin_str.length() % 8;
            System.out.println(bin_str.length());
        }
        if (remainder != 0) {
            while (remainder != 0) {
                System.out.println("da");
                bin_str.append("0");
                remainder = bin_str.length() % 8;
            }
        }
        length = bin_str.length() >> 3;
        byte[] decoded = new byte[length];
        for (i = 0; i < length; i++) {
            decoded[i] = (byte) Integer.parseInt(bin_str.substring(i * 8, (i + 1) * 8), 2);
        }
        return decoded;
    }


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Write a mode: ");
        String inputMode = scanner.nextLine();
        Mode mode;
        if ("encode".equals(inputMode)) mode = Mode.ENCODE;
        else if ("send".equals(inputMode)) mode = Mode.SEND;
        else if ("decode".equals(inputMode)) mode = Mode.DECODE;
        else {
            System.out.println("Wrong mode input, closing.");
            return;
        }
        switch (mode) {
            case ENCODE:
                try (FileInputStream fileInputStream = new FileInputStream("send.txt");
                     FileOutputStream fileOutputStream = new FileOutputStream("encoded.txt")) {
                    byte[] input = fileInputStream.readAllBytes();
                    byte[] encoded = encoding(input);
                    fileOutputStream.write(encoded);
                    return;
                }
            case SEND:
                try (FileInputStream fileInputStream = new FileInputStream("encoded.txt");
                     FileOutputStream fileOutputStream = new FileOutputStream("received.txt")) {
                    byte[] input = fileInputStream.readAllBytes();
                    byte[] errored = erroring(input);
                    fileOutputStream.write(errored);
                    return;
                }
            case DECODE:
                try (FileInputStream fileInputStream = new FileInputStream("received.txt");
                     FileOutputStream fileOutputStream = new FileOutputStream("decoded.txt")) {
                    byte[] input = fileInputStream.readAllBytes();
                    byte[] decoded = decoding(input);
                    fileOutputStream.write(decoded);
                }
        }
    }
}
