package sorter;

import java.io.*;
import java.nio.file.*;

/**
 * BinaryIO — Binary file I/O for the sort dataset
 * ─────────────────────────────────────────────────
 * Binary layout (big-endian, Java's native DataOutputStream order):
 *   [4 bytes : int  — element count n]
 *   [n × 4 bytes : int — each element]
 *
 * Methods:
 *   writeBinary(path, data)   — serialize int[] to .bin file
 *   readBinary(path)          — deserialize .bin file to int[]
 *   textToBinary(txt, bin)    — parse space-separated .txt → .bin
 *   loadDataset(txt, bin)     — load bin if present, else convert txt first
 */
public class BinaryIO {

    private BinaryIO() {}   // utility class — no instances

    // ── Write ──────────────────────────────────────────────────────────────
    public static void writeBinary(String path, int[] data) throws IOException {
        try (DataOutputStream dos =
                 new DataOutputStream(
                     new BufferedOutputStream(
                         new FileOutputStream(path)))) {
            dos.writeInt(data.length);
            for (int v : data) dos.writeInt(v);
        }
    }

    // ── Read ───────────────────────────────────────────────────────────────
    public static int[] readBinary(String path) throws IOException {
        try (DataInputStream dis =
                 new DataInputStream(
                     new BufferedInputStream(
                         new FileInputStream(path)))) {
            int n    = dis.readInt();
            int[] out = new int[n];
            for (int i = 0; i < n; i++) out[i] = dis.readInt();
            return out;
        }
    }

    // ── Text → Binary conversion ────────────────────────────────────────────
    public static int[] textToBinary(String txtPath, String binPath)
            throws IOException {
        String text  = Files.readString(Path.of(txtPath)).trim();
        String[] tok = text.split("\\s+");
        int[] data   = new int[tok.length];
        for (int i = 0; i < tok.length; i++) data[i] = Integer.parseInt(tok[i]);
        writeBinary(binPath, data);
        return data;
    }

    // ── Convenience loader ──────────────────────────────────────────────────
    public static int[] loadDataset(String txtPath, String binPath)
            throws IOException {
        if (!new File(binPath).exists()) {
            System.out.println("  Converting " + txtPath
                               + " → " + binPath + " (binary) …");
            return textToBinary(txtPath, binPath);
        }
        return readBinary(binPath);
    }
}
