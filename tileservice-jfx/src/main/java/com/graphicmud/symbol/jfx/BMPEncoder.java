package com.graphicmud.symbol.jfx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 *
 */
public class BMPEncoder {

	//-------------------------------------------------------------------
	public static byte[] convertToBMP(WritableImage image) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int bytesPerPixel = 3; // BMP speichert RGB, aber kein Alpha

        int padding = (4 - (width * bytesPerPixel) % 4) % 4;
        int imageSize = (width * bytesPerPixel + padding) * height;
        int fileSize = 14 + 40 + imageSize;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // BMP-Datei-Header
        outputStream.write(new byte[]{'B', 'M'});
        writeInt(outputStream, fileSize);
        writeShort(outputStream, (short) 0);
        writeShort(outputStream, (short) 0);
        writeInt(outputStream, 54); // Offset zu den Bilddaten

        // DIB-Header (BITMAPINFOHEADER)
        writeInt(outputStream, 40); // Größe des DIB-Headers
        writeInt(outputStream, width);
        writeInt(outputStream, height);
        writeShort(outputStream, (short) 1); // Farbebenen
        writeShort(outputStream, (short) (bytesPerPixel * 8)); // Bits pro Pixel
        writeInt(outputStream, 0); // Keine Kompression
        writeInt(outputStream, imageSize);
        writeInt(outputStream, 0); // Horizontale Auflösung (Pixel pro Meter)
        writeInt(outputStream, 0); // Vertikale Auflösung (Pixel pro Meter)
        writeInt(outputStream, 0); // Farben in der Palette
        writeInt(outputStream, 0); // Wichtige Farben

        // Bilddaten
        PixelReader pixelReader = image.getPixelReader();
        for (int y = height - 1; y >= 0; y--) { // BMP speichert Bilddaten von unten nach oben
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                outputStream.write((byte) (argb & 0xFF));        // Blau
                outputStream.write((byte) ((argb >> 8) & 0xFF)); // Grün
                outputStream.write((byte) ((argb >> 16) & 0xFF));// Rot
            }
            for (int p = 0; p < padding; p++) {
                outputStream.write(0); // Padding-Bytes
            }
        }

        return outputStream.toByteArray();
    }
    private static void writeInt(ByteArrayOutputStream stream, int value) throws IOException {
        stream.write((value >> 0) & 0xFF);
        stream.write((value >> 8) & 0xFF);
        stream.write((value >> 16) & 0xFF);
        stream.write((value >> 24) & 0xFF);
    }

    private static void writeShort(ByteArrayOutputStream stream, short value) throws IOException {
        stream.write((value >> 0) & 0xFF);
        stream.write((value >> 8) & 0xFF);
    }

}
