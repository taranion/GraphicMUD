package com.graphicmud.symbol.jfx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class PngEncoderFX2 {


	   public static byte[] convertToPNG(WritableImage image) throws IOException {
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        writePNG(byteArrayOutputStream, image);
	        return byteArrayOutputStream.toByteArray();
	    }

	    private static void writePNG(ByteArrayOutputStream outputStream, WritableImage image) throws IOException {
	        int width = (int) image.getWidth();
	        int height = (int) image.getHeight();
	        PixelReader pixelReader = image.getPixelReader();

	        // PNG-Signatur
	        outputStream.write(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});

	        // IHDR-Chunk
	        ByteArrayOutputStream ihdrData = new ByteArrayOutputStream();
	        writeInt(ihdrData, width);
	        writeInt(ihdrData, height);
	        ihdrData.write(8); // Bit-Tiefe
	        ihdrData.write(6); // Farbe (truecolor mit Alpha)
	        ihdrData.write(0); // Kompressionsmethode
	        ihdrData.write(0); // Filtermethode
	        ihdrData.write(0); // Interlace-Methode
	        writeChunk(outputStream, "IHDR", ihdrData.toByteArray());

	        // IDAT-Chunk
	        ByteArrayOutputStream idatData = new ByteArrayOutputStream();
	        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(idatData);
	        for (int y = 0; y < height; y++) {
	            deflaterOutputStream.write(0); // Kein Filter
	            for (int x = 0; x < width; x++) {
	                int argb = pixelReader.getArgb(x, y);
	                deflaterOutputStream.write((argb >> 16) & 0xFF); // R
	                deflaterOutputStream.write((argb >> 8) & 0xFF);  // G
	                deflaterOutputStream.write(argb & 0xFF);         // B
	                deflaterOutputStream.write((argb >> 24) & 0xFF); // A
	            }
	        }
	        deflaterOutputStream.close();
	        writeChunk(outputStream, "IDAT", idatData.toByteArray());

	        // IEND-Chunk
	        writeChunk(outputStream, "IEND", new byte[0]);
	    }

	    private static void writeInt(ByteArrayOutputStream stream, int value) throws IOException {
	        stream.write((value >> 24) & 0xFF);
	        stream.write((value >> 16) & 0xFF);
	        stream.write((value >> 8) & 0xFF);
	        stream.write(value & 0xFF);
	    }

	    private static void writeChunk(ByteArrayOutputStream stream, String type, byte[] data) throws IOException {
	        writeInt(stream, data.length);
	        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
	        chunk.write(type.getBytes(StandardCharsets.US_ASCII));
	        chunk.write(data);
	        CRC32 crc = new CRC32();
	        crc.update(chunk.toByteArray());
	        stream.write(chunk.toByteArray());
	        writeInt(stream, (int) crc.getValue());
	    }

}
