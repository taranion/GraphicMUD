package com.graphicmud.symbol.jfx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class PngIndexedEncoderFX {


	   public static byte[] convertToPNG(WritableImage image) throws IOException {
	        int width = (int) image.getWidth();
	        int height = (int) image.getHeight();

	        // Erstelle Farbpalette (in diesem Beispiel bis zu 256 Farben)
	        int[] palette = new int[256];
	        byte[] alphaValues = new byte[256]; // Für Transparenzwerte
	        int paletteSize = 0;

	        PixelReader pixelReader = image.getPixelReader();
	        byte[] pixelData = new byte[width * height];

	        // Fülle Palette und weise Farbindices und Transparenzwerte zu
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                int argb = pixelReader.getArgb(x, y);
	                int rgb = argb & 0xFFFFFF;
	                int alpha = (argb >> 24) & 0xFF;

	                int paletteIndex = findInPalette(palette, paletteSize, rgb);
	                if (paletteIndex == -1 && paletteSize < 256) {
	                    palette[paletteSize] = rgb;
	                    alphaValues[paletteSize] = (byte) alpha; // Transparenzwert speichern
	                    paletteIndex = paletteSize++;
	                }
	                pixelData[y * width + x] = (byte) paletteIndex;
	            }
	    }

	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	        // PNG-Signatur
	        outputStream.write(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});

	        // IHDR-Chunks
	        writeIHDR(outputStream, width, height);

	        // PLTE-Chunks (Palette)
	        writePLTE(outputStream, palette, paletteSize);

	        // tRNS-Chunks (Transparenz)
	        writeTRNS(outputStream, alphaValues, paletteSize);

	        // IDAT-Chunks (komprimierte Bilddaten)
	        writeIDAT(outputStream, pixelData, width, height);

	        writeTEXT(outputStream, "Author", "Stefan Prelle");

	        // IEND-Chunks
	        writeIEND(outputStream);

	        return outputStream.toByteArray();
	    }

	    private static int findInPalette(int[] palette, int size, int color) {
	        for (int i = 0; i < size; i++) {
	            if (palette[i] == color) {
	                return i;
	            }
	        }
	        return -1;
	    }

	    private static void writeIHDR(ByteArrayOutputStream stream, int width, int height) throws IOException {
	        ByteArrayOutputStream chunkStream = new ByteArrayOutputStream();
	        writeInt(chunkStream, width);
	        writeInt(chunkStream, height);
	        chunkStream.write(8); // Bit-Tiefe
	        chunkStream.write(3); // Farbart: Indexed color (Palette)
	        chunkStream.write(0); // Kompression: Deflate
	        chunkStream.write(0); // Filtermethode: 0
	        chunkStream.write(0); // Interlace: Keine
	        writeChunk(stream, "IHDR", chunkStream.toByteArray());
	    }

	    private static void writePLTE(ByteArrayOutputStream stream, int[] palette, int size) throws IOException {
	        ByteArrayOutputStream chunkStream = new ByteArrayOutputStream();
	        for (int i = 0; i < size; i++) {
	            int color = palette[i];
	            chunkStream.write((color >> 16) & 0xFF); // Rot
	            chunkStream.write((color >> 8) & 0xFF);  // Grün
	            chunkStream.write(color & 0xFF);         // Blau
	        }
	        writeChunk(stream, "PLTE", chunkStream.toByteArray());
	    }

	    private static void writeTRNS(ByteArrayOutputStream stream, byte[] alphaValues, int size) throws IOException {
	        ByteArrayOutputStream chunkStream = new ByteArrayOutputStream();
	        for (int i = 0; i < size; i++) {
	            chunkStream.write(alphaValues[i]);
	        }
	        writeChunk(stream, "tRNS", chunkStream.toByteArray());
	    }

	    private static void writeIDAT(ByteArrayOutputStream stream, byte[] pixelData, int width, int height) throws IOException {
	        ByteArrayOutputStream deflateStream = new ByteArrayOutputStream();
	        DeflaterOutputStream deflater = new DeflaterOutputStream(deflateStream);

	        for (int y = 0; y < height; y++) {
	            deflater.write(0); // Keine Filtermethode
	            deflater.write(pixelData, y * width, width); // Zeile schreiben
	        }

	        deflater.close();
	        writeChunk(stream, "IDAT", deflateStream.toByteArray());
	    }

	    private static void writeIEND(ByteArrayOutputStream stream) throws IOException {
	        writeChunk(stream, "IEND", new byte[0]);
	    }

	    private static void writeChunk(ByteArrayOutputStream stream, String type, byte[] data) throws IOException {
	        writeInt(stream, data.length);
	        stream.write(type.getBytes(StandardCharsets.US_ASCII));
	        stream.write(data);
	        writeInt(stream, calculateCRC(type.getBytes(StandardCharsets.US_ASCII), data));
	    }

	    private static void writeInt(ByteArrayOutputStream stream, int value) throws IOException {
	        stream.write((value >> 24) & 0xFF);
	        stream.write((value >> 16) & 0xFF);
	        stream.write((value >> 8) & 0xFF);
	        stream.write(value & 0xFF);
	    }

	    private static int calculateCRC(byte[] type, byte[] data) {
	        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
	        crc.update(type);
	        crc.update(data);
	        return (int) crc.getValue();
	    }

	    private static void writeTEXT(ByteArrayOutputStream stream, String keyword, String text) throws IOException {
	        ByteArrayOutputStream chunkStream = new ByteArrayOutputStream();
	        chunkStream.write(keyword.getBytes(StandardCharsets.ISO_8859_1)); // Schlüsselwort
	        chunkStream.write(0); // Null-Byte als Trennzeichen
	        chunkStream.write(text.getBytes(StandardCharsets.ISO_8859_1)); // Textinhalt
	        writeChunk(stream, "tEXt", chunkStream.toByteArray());
	    }
}
