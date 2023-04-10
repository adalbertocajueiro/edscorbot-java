package es.us.edscorbot.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NumpyIO {

    private static final byte[] MAGIC_NUMBER = new byte[] { (byte) 0x93, 0x4e, 0x55, 0x4d, 0x50, 0x59 };
    private static final int DEFAULT_VERSION_MAJOR = 1;
    private static final int DEFAULT_VERSION_MINOR = 0;

    private static final Pattern PATTERN_DICT = Pattern.compile(
            "\\{\\s*'(.*)'\\s*:\\s*('(.*)'|(True)|(False)|\\((.*)\\))\\s*,\\s*" +
                    "'(.*)'\\s*:\\s*('(.*)'|(True)|(False)|\\((.*)\\))\\s*,\\s*" +
                    "'(.*)'\\s*:\\s*('(.*)'|(True)|(False)|\\((.*)\\))\\s*,?\\s*}");

    /* Common helper functions for the headers */
    static class Header {
        final int versionMajor;
        final int versionMinor;
        final String descr;
        final boolean fortranOrder;
        final int[] shape;

        public Header(int versionMajor, int versionMinor, String descr, boolean fortranOrder, int[] shape) {
            this.versionMajor = versionMajor;
            this.versionMinor = versionMinor;
            this.descr = descr;
            this.fortranOrder = fortranOrder;
            this.shape = shape;
        }
    }

    private static void writeHeader(DataOutputStream out, int versionMajor, int versionMinor, String descr,
            boolean fortranOrder, int[] shape) throws IOException {
        writeHeader(out, new Header(versionMajor, versionMinor, descr, fortranOrder, shape));
    }

    private static void writeHeader(DataOutputStream out, Header header) throws IOException {
        checkCompatibility(header);
        out.write(MAGIC_NUMBER);
        out.writeByte(header.versionMajor);
        out.writeByte(header.versionMinor);

        String shapeString;
        if (header.shape.length != 1) {
            shapeString = Arrays.toString(header.shape).replace('[', '(').replace(']', ')');
        } else {
            shapeString = "(" + header.shape[0] + ",)";
        }
        StringBuilder headerString = new StringBuilder("{'descr': '" + header.descr + "', " +
                "'fortran_order': " + (header.fortranOrder ? "True" : "False") + ", " +
                "'shape': " + shapeString + ", }");

        int hangover = (MAGIC_NUMBER.length + 2 + 2 + headerString.length() + 1) % 64;
        if (hangover > 0) {
            for (int i = hangover; i < 64; i++) {
                headerString.append(" ");
            }
        }
        headerString.append("\n");

        byte[] headerBytes = headerString.toString().getBytes(StandardCharsets.US_ASCII);
        writeIntLe(out, 2, false, headerBytes.length);
        out.write(headerBytes);
    }

    private static Header readHeader(DataInputStream in) throws IOException {
        byte[] readMagicNumber = new byte[6];
        if (in.read(readMagicNumber) != 6) {
            throw new EOFException();
        }
        if (!Arrays.equals(readMagicNumber, MAGIC_NUMBER)) {
            throw new IOException("Invalid magic number!");
        }
        int versionMajor = in.readUnsignedByte();
        int versionMinor = in.readUnsignedByte();
        if (versionMajor != DEFAULT_VERSION_MAJOR || versionMinor != DEFAULT_VERSION_MINOR) {
            throw new IllegalArgumentException("Unsupported version!");
        }
        int headerBytesLength = readIntLe(in, 2, false);
        byte[] headerBytes = new byte[headerBytesLength];
        if (in.read(headerBytes) != headerBytesLength) {
            throw new EOFException();
        }
        String headerString = new String(headerBytes, StandardCharsets.US_ASCII).trim(); // remove padding
        Matcher matcher = PATTERN_DICT.matcher(headerString);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Malformed header string!");
        }
        String descr = null;
        Boolean fortranOrder = null;
        int[] shape = null;
        for (int i = 0; i < 3 * 6; i += 6) {
            String key = matcher.group(i + 1);
            if (key.equals("descr")) {
                descr = matcher.group(i + 3);
            } else if (key.equals("fortran_order")) {
                if ("True".equals(matcher.group(i + 4))) {
                    fortranOrder = true;
                } else if ("False".equals(matcher.group(i + 5))) {
                    fortranOrder = false;
                } else {
                    throw new IllegalArgumentException("Unknown value in fortran_order header!");
                }
            } else if (key.equals("shape")) {
                String value = matcher.group(i + 6);
                if (value.equals("")) {
                    shape = new int[0];
                } else {
                    shape = Arrays.stream(value.split("\\s*,\\s*"))
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::valueOf)
                            .toArray();
                }
            } else {
                throw new IllegalArgumentException("Unknown header key!");
            }
        }
        if (descr == null || fortranOrder == null || shape == null) {
            throw new IllegalArgumentException("Not all three necessary headers specified!");
        }
        Header header = new Header(versionMajor, versionMinor, descr, fortranOrder, shape);
        checkCompatibility(header);
        return header;
    }

    private static void checkCompatibility(Header header) {
        if (header.versionMajor != DEFAULT_VERSION_MAJOR || header.versionMinor != DEFAULT_VERSION_MINOR) {
            throw new IllegalArgumentException("Unsupported version!");
        }
        if (header.fortranOrder) {
            throw new IllegalArgumentException("Fortran order not supported!");
        }
        if (!header.descr.matches("[<|][uif][1248]")) {
            throw new IllegalArgumentException("Unsupported dtype!");
        }
        if (header.shape.length > 3) {
            throw new IllegalArgumentException("Unsupported shape!");
        }
    }

    /* Low-endian writer helper functions */
    private static void writeIntLe(DataOutputStream out, int bytes, boolean signed, int value) throws IOException {
        if (signed) {
            switch (bytes) {
                case 1:
                    out.writeByte(value);
                    return;
                case 2:
                    out.writeShort(Short.reverseBytes((short) value));
                    return;
                case 4:
                    out.writeInt(Integer.reverseBytes(value));
                    return;
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Integer has only 4 bytes!");
            }
        } else {
            switch (bytes) {
                case 1:
                    out.writeByte(value);
                    return;
                case 2:
                    out.writeByte(value & 0xFF);
                    out.writeByte((value >> 8) & 0xFF);
                    return;
                case 4:
                    out.writeInt(Integer.reverseBytes(value));
                    return;
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Integer has only 4 bytes!");
            }
        }
    }

    private static void writeLongLe(DataOutputStream out, int bytes, boolean signed, long value) throws IOException {
        if (signed) {
            switch (bytes) {
                case 1:
                    out.writeByte((int) value);
                    return;
                case 2:
                    out.writeShort(Short.reverseBytes((short) value));
                    return;
                case 4:
                    out.writeInt(Integer.reverseBytes((int) value));
                    return;
                case 8:
                    out.writeLong(Long.reverseBytes(value));
                    return;
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Long has only 8 bytes!");
            }
        } else {
            switch (bytes) {
                case 1:
                    out.writeByte((int) value);
                    return;
                case 2:
                    out.writeByte((int) (value & 0xFF));
                    out.writeByte((int) ((value >> 8) & 0xFF));
                    return;
                case 4:
                    out.writeByte((int) (value & 0xFF));
                    out.writeByte((int) ((value >> 8) & 0xFF));
                    out.writeByte((int) ((value >> 16) & 0xFF));
                    out.writeByte((int) ((value >> 24) & 0xFF));
                    return;
                case 8:
                    out.writeLong(Long.reverseBytes(value));
                    return;
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Long has only 8 bytes!");
            }
        }

    }

    private static void writeFloatLe(DataOutputStream out, float value) throws IOException {
        writeIntLe(out, 4, true, Float.floatToIntBits(value));
    }

    private static void writeDoubleLe(DataOutputStream out, double value) throws IOException {
        writeLongLe(out, 8, true, Double.doubleToLongBits(value));
    }

    private static void writeBooleanNumpy(DataOutputStream out, boolean value) throws IOException {
        out.writeBoolean(value);
    }

    /* Int writer functions */
    public static void write(DataOutputStream out, int value, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[0]);
        writeIntLe(out, bytes, signed, value);
    }

    public static void write(DataOutputStream out, int[] values, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[] { values.length });
        for (int i = 0; i < values.length; i++) {
            writeIntLe(out, bytes, signed, values[i]);
        }
    }

    public static void write(DataOutputStream out, int[][] values, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                writeIntLe(out, bytes, signed, values[i][j]);
            }
        }
    }

    public static void write(DataOutputStream out, int[][][] values, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length, values[0][0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                for (int k = 0; k < values[0][0].length; k++) {
                    writeIntLe(out, bytes, signed, values[i][j][k]);
                }
            }
        }
    }

    /* Long writer functions */
    public static void write(DataOutputStream out, long value, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[0]);
        writeLongLe(out, bytes, signed, value);
    }

    public static void write(DataOutputStream out, long[] values, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[] { values.length });
        for (int i = 0; i < values.length; i++) {
            writeLongLe(out, bytes, signed, values[i]);
        }
    }

    public static void write(DataOutputStream out, long[][] values, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                writeLongLe(out, bytes, signed, values[i][j]);
            }
        }
    }

    public static void write(DataOutputStream out, long[][][] values, int bytes, boolean signed) throws IOException {
        String descr = (bytes == 1 ? "|" : "<") + (signed ? "i" : "u") + bytes;
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length, values[0][0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                for (int k = 0; k < values[0][0].length; k++) {
                    writeLongLe(out, bytes, signed, values[i][j][k]);
                }
            }
        }
    }

    /* Float writer functions */
    public static void write(DataOutputStream out, float value) throws IOException {
        String descr = "<f4";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[0]);
        writeFloatLe(out, value);
    }

    public static void write(DataOutputStream out, float[] values) throws IOException {
        String descr = "<f4";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[] { values.length });
        for (int i = 0; i < values.length; i++) {
            writeFloatLe(out, values[i]);
        }
    }

    public static void write(DataOutputStream out, float[][] values) throws IOException {
        String descr = "<f4";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                writeFloatLe(out, values[i][j]);
            }
        }
    }

    public static void write(DataOutputStream out, float[][][] values) throws IOException {
        String descr = "<f4";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length, values[0][0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                for (int k = 0; k < values[0][0].length; k++) {
                    writeFloatLe(out, values[i][j][k]);
                }
            }
        }
    }

    /* Double writer functions */
    public static void write(DataOutputStream out, double value) throws IOException {
        String descr = "<f8";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[0]);
        writeDoubleLe(out, value);
    }

    public static void write(DataOutputStream out, double[] values) throws IOException {
        String descr = "<f8";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[] { values.length });
        for (int i = 0; i < values.length; i++) {
            writeDoubleLe(out, values[i]);
        }
    }

    public static void write(DataOutputStream out, double[][] values) throws IOException {
        String descr = "<f8";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                writeDoubleLe(out, values[i][j]);
            }
        }
    }

    public static void write(DataOutputStream out, double[][][] values) throws IOException {
        String descr = "<f8";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length, values[0][0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                for (int k = 0; k < values[0][0].length; k++) {
                    writeDoubleLe(out, values[i][j][k]);
                }
            }
        }
    }

    /* Boolean writer functions */
    public static void write(DataOutputStream out, boolean value) throws IOException {
        String descr = "|b1";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[0]);
        writeBooleanNumpy(out, value);
    }

    public static void write(DataOutputStream out, boolean[] values) throws IOException {
        String descr = "|b1";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false, new int[] { values.length });
        for (int i = 0; i < values.length; i++) {
            writeBooleanNumpy(out, values[i]);
        }
    }

    public static void write(DataOutputStream out, boolean[][] values) throws IOException {
        String descr = "|b1";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                writeBooleanNumpy(out, values[i][j]);
            }
        }
    }

    public static void write(DataOutputStream out, boolean[][][] values) throws IOException {
        String descr = "|b1";
        writeHeader(out, DEFAULT_VERSION_MAJOR, DEFAULT_VERSION_MINOR, descr, false,
                new int[] { values.length, values[0].length, values[0][0].length });
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                for (int k = 0; k < values[0][0].length; k++) {
                    writeBooleanNumpy(out, values[i][j][k]);
                }
            }
        }
    }

    /* Low-endian reader helper-functions */
    private static int readIntLe(DataInputStream in, int bytes, boolean signed) throws IOException {
        if (signed) {
            switch (bytes) {
                case 1:
                    return in.readByte();
                case 2:
                    return Short.reverseBytes(in.readShort());
                case 4:
                    return Integer.reverseBytes(in.readInt());
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Integer has only 4 bytes!");
            }
        } else {
            switch (bytes) {
                case 1:
                    return in.readUnsignedByte();
                case 2:
                    int ch1 = in.read();
                    int ch2 = in.read();
                    if ((ch1 | ch2) < 0) {
                        throw new EOFException();
                    }
                    return (ch2 << 8) + ch1;
                case 4:
                    return Integer.reverseBytes(in.readInt());
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Integer has only 4 bytes!");

            }
        }
    }

    private static long readLongLe(DataInputStream in, int bytes, boolean signed) throws IOException {
        if (signed) {
            switch (bytes) {
                case 1:
                    return in.readByte();
                case 2:
                    return Short.reverseBytes(in.readShort());
                case 4:
                    return Integer.reverseBytes(in.readInt());
                case 8:
                    return Long.reverseBytes(in.readLong());
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Long has only 8 bytes!");
            }
        } else {
            switch (bytes) {
                case 1:
                    return in.readUnsignedByte();
                case 2: {
                    int ch1 = in.read();
                    int ch2 = in.read();
                    if ((ch1 | ch2) < 0) {
                        throw new EOFException();
                    }
                    return (ch2 << 8) + ch1;
                }
                case 4: {
                    long ch1 = in.read();
                    long ch2 = in.read();
                    long ch3 = in.read();
                    long ch4 = in.read();
                    if ((ch1 | ch2 | ch3 | ch4) < 0) {
                        throw new EOFException();
                    }
                    return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1;
                }
                case 8:
                    return Long.reverseBytes(in.readLong());
                default:
                    throw new IllegalArgumentException(
                            "Only supporting powers of 2 for bytes argument! Long has only 8 bytes!");

            }
        }
    }

    private static float readFloatLe(DataInputStream in) throws IOException {
        return Float.intBitsToFloat(readIntLe(in, 4, true));
    }

    private static double readDoubleLe(DataInputStream in) throws IOException {
        return Double.longBitsToDouble(readLongLe(in, 8, true));
    }

    private static boolean readBooleanNumpy(DataInputStream in) throws IOException {
        return in.readBoolean();
    }

    /* Int reader functions */
    public static int readInt0d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 0) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        return readIntLe(in, bytes, signed);
    }

    public static int[] readInt1d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 1) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        int[] value = new int[header.shape[0]];
        for (int i = 0; i < value.length; i++) {
            value[i] = readIntLe(in, bytes, signed);
        }
        return value;
    }

    public static int[][] readInt2d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 2) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        int[][] value = new int[header.shape[0]][header.shape[1]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = readIntLe(in, bytes, signed);
            }
        }
        return value;
    }

    public static int[][][] readInt3d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 3) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        int[][][] value = new int[header.shape[0]][header.shape[1]][header.shape[2]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                for (int k = 0; k < value[0][0].length; k++) {
                    value[i][j][k] = readIntLe(in, bytes, signed);
                }
            }
        }
        return value;
    }

    /* Long reader functions */
    public static long readLong0d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 0) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        return readLongLe(in, bytes, signed);
    }

    public static long[] readLong1d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 1) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        long[] value = new long[header.shape[0]];
        for (int i = 0; i < value.length; i++) {
            value[i] = readLongLe(in, bytes, signed);
        }
        return value;
    }

    public static long[][] readLong2d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 2) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        long[][] value = new long[header.shape[0]][header.shape[1]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = readLongLe(in, bytes, signed);
            }
        }
        return value;
    }

    public static long[][][] readLong3d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        boolean signed;
        if (header.descr.charAt(1) == 'i') {
            signed = true;
        } else if (header.descr.charAt(1) == 'u') {
            signed = false;
        } else {
            throw new IllegalArgumentException("Unknown dtype!");
        }
        int bytes = Integer.valueOf(header.descr.substring(2));
        if (header.shape.length != 3) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        long[][][] value = new long[header.shape[0]][header.shape[1]][header.shape[2]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                for (int k = 0; k < value[0][0].length; k++) {
                    value[i][j][k] = readLongLe(in, bytes, signed);
                }
            }
        }
        return value;
    }

    /* Float reader functions */
    public static float readFloat0d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f4")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 0) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        return readFloatLe(in);
    }

    public static float[] readFloat1d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f4")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 1) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        float[] value = new float[header.shape[0]];
        for (int i = 0; i < value.length; i++) {
            value[i] = readFloatLe(in);
        }
        return value;
    }

    public static float[][] readFloat2d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f4")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 2) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        float[][] value = new float[header.shape[0]][header.shape[1]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = readFloatLe(in);
            }
        }
        return value;
    }

    public static float[][][] readFloat3d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f4")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 3) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        float[][][] value = new float[header.shape[0]][header.shape[1]][header.shape[2]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                for (int k = 0; k < value[0][0].length; k++) {
                    value[i][j][k] = readFloatLe(in);
                }
            }
        }
        return value;
    }

    /* Double reader functions */
    public static double readDouble0d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f8")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 0) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        return readDoubleLe(in);
    }

    public static double[] readDouble1d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f8")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 1) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        double[] value = new double[header.shape[0]];
        for (int i = 0; i < value.length; i++) {
            value[i] = readDoubleLe(in);
        }
        return value;
    }

    public static double[][] readDouble2d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f8")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 2) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        double[][] value = new double[header.shape[0]][header.shape[1]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = readDoubleLe(in);
            }
        }
        return value;
    }

    public static double[][][] readDouble3d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("<f8")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 3) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        double[][][] value = new double[header.shape[0]][header.shape[1]][header.shape[2]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                for (int k = 0; k < value[0][0].length; k++) {
                    value[i][j][k] = readDoubleLe(in);
                }
            }
        }
        return value;
    }

    /* Boolean reader functions */
    public static boolean readBoolean0d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("|b1")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 0) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        return readBooleanNumpy(in);
    }

    public static boolean[] readBoolean1d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("|b1")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 1) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        boolean[] value = new boolean[header.shape[0]];
        for (int i = 0; i < value.length; i++) {
            value[i] = readBooleanNumpy(in);
        }
        return value;
    }

    public static boolean[][] readBoolean2d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("|b1")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 2) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        boolean[][] value = new boolean[header.shape[0]][header.shape[1]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = readBooleanNumpy(in);
            }
        }
        return value;
    }

    public static boolean[][][] readBoolean3d(DataInputStream in) throws IOException {
        Header header = readHeader(in);
        if (!header.descr.equals("|b1")) {
            throw new IllegalArgumentException("Invalid dtype!");
        }
        if (header.shape.length != 3) {
            throw new IllegalArgumentException("Invalid shape!");
        }
        boolean[][][] value = new boolean[header.shape[0]][header.shape[1]][header.shape[2]];
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                for (int k = 0; k < value[0][0].length; k++) {
                    value[i][j][k] = readBooleanNumpy(in);
                }
            }
        }
        return value;
    }

    /* Fake write unit tests, platform dependent */
    public static void main(String[] args) throws IOException, InterruptedException {
        int[][][] value = new int[][][] { { { 0, 1 }, { 2, 3 } }, { { 4, 5 }, { 6, 7 } } };
        int bytes = 4;
        int[][][] read;
        DataOutputStream out = new DataOutputStream(new FileOutputStream("test.npy"));
        write(out, value, bytes, false);
        cmp(Arrays.deepToString(value), "uint" + bytes * 8);
        out.close();
        DataInputStream in = new DataInputStream(new FileInputStream("ref.npy"));
        read = readInt3d(in);
        in.close();
        assert Arrays.deepEquals(value, read);
    }

    private static void cmp(String array, String dtype) throws IOException, InterruptedException {
        if (array.equals("NaN")) {
            array = "float('nan')";
        } else if (array.equals("Infinity")) {
            array = "float('infinity')";
        } else if (array.equals("-Infinity")) {
            array = "-float('infinity')";
        }
        String command = "import numpy as np\n" +
                "np.save(\"ref\", np.asarray(" + array + ", dtype=np." + dtype + "))";
        File temp = File.createTempFile("script", ".py");
        Files.write(temp.toPath(), Collections.singleton(command));
        // System.out.println(command);
        Process process = Runtime.getRuntime().exec("python " + temp.getAbsolutePath());
        System.out.println(IOUtils.toString(process.getErrorStream()));
        assert Runtime.getRuntime().exec("cmp test.npy ref.npy").waitFor() == 0;
    }

    private static int exp2(int i) {
        return (int) Math.pow(2, i);
    }

}