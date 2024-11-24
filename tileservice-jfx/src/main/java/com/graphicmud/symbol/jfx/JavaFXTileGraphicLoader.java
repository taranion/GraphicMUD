package com.graphicmud.symbol.jfx;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import com.graphicmud.map.ViewportMap;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.Symbol.GraphicSymbol;
import com.graphicmud.symbol.SymbolSet;
import com.graphicmud.symbol.TileGraphicService;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

/**
 *
 */
public class JavaFXTileGraphicLoader implements TileGraphicService {

	private final static Logger logger = System.getLogger("mud.symbol");

	private Path basePath;

	//-------------------------------------------------------------------
	public JavaFXTileGraphicLoader(Path baseDir) {
		this.basePath = baseDir;
	}

	@Override
	public void loadSymbolImages(SymbolSet set) throws IOException {
		logger.log(Level.DEBUG, "ENTER: loadSymbolImages {0}", set.getImageFile());
		Instant start = Instant.now();

		String filename = set.getImageFile();
		if (filename==null) {
			logger.log(Level.WARNING, "Symbolset ''{0} - {1}'' has no attached image", set.getId(), set.getTitle());
			return;
		}

		int size = set.getTileSize();
		Image image = new Image(new FileInputStream(basePath.resolve(set.getImageFile()).toFile()));
		int columns = (int) image.getWidth()/size;
		int rows    = (int) image.getHeight()/size;
		int maxTiles= columns*rows;
		logger.log(Level.TRACE, "re-import {0} tiles", maxTiles);

		PixelReader pixelReader = image.getPixelReader();
//		symbols.clear();
//		images.clear();
		int symbolsPerLine = columns;
		int symbolLines = rows;
		int tile = 0;
		int numRead = 0;
		outer:
		for (Symbol symbol : set.asList()) {
			int frames = (symbol.getImage()!=null)?symbol.getImage().getFrames():1;
			WritableImage[] imgFrames = new WritableImage[frames];
			for (int f=0; f<frames; f++) {
				int x = numRead%columns;
				int y = numRead/columns;
				int fromX = x*size;
				int fromY = y*size;

				WritableImage target = new WritableImage(size, size);
				PixelWriter pixelWriter = target.getPixelWriter();
				int[] pixels = new int[size * size];
				pixelReader.getPixels(fromX, fromY, size, size, PixelFormat.getIntArgbInstance(), pixels, 0, size);
				pixelWriter.setPixels(0, 0, size, size, PixelFormat.getIntArgbInstance(), pixels, 0, size);

				imgFrames[f] = target;
				numRead++;
			}
			GraphicSymbol imgSymbol = (symbol.getImage()!=null)?symbol.getImage():(new GraphicSymbol());
			imgSymbol.setFrames(frames);
			imgSymbol.setUiInternal(imgFrames);
			symbol.setImage(imgSymbol);

			byte[][] data = new byte[frames][];
			for (int i=0; i<frames; i++) {
				int[] pixels = new int[size * size];
				ByteBuffer byteBuffer = ByteBuffer.allocate(pixels.length * 4);
		        IntBuffer intBuffer = byteBuffer.asIntBuffer();
		        intBuffer.put(pixels);
		        data[i]= byteBuffer.array();
			}
			imgSymbol.setBytes(data);
			tile++;
		}
		logger.log(Level.INFO, "Loaded symbolset {0} with {1} symbols from {2}", set.getTitle(), set.asList().size(), set.getFile());

		Instant end = Instant.now();
		logger.log(Level.DEBUG, "LEAVE: loadSymbolImages took {0} ms", Duration.between(start, end).toMillis());
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.mud.symbol.TileGraphicService#renderMap(int[][], org.prelle.mud.symbol.SymbolSet)
	 */
	@Override
	public byte[] renderMap(ViewportMap<Symbol> data, SymbolSet set) {
		int width = data.getHeight() * set.getTileSize();
		int height = data.getWidth() * set.getTileSize();
		int size = set.getTileSize();
		logger.log(Level.INFO, "create an image being {0}x{0} pixels", width);
		WritableImage target = new WritableImage(width, height);
		PixelWriter pixelWriter = target.getPixelWriter();
		WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbPreInstance();

		for (int y=0; y<data.getHeight(); y++) {
			for (int x=0; x<data.getWidth(); x++) {
				int toX = x*size;
				int toY = y*size;
				try {
					Symbol symbol = data.get(x, y);
					GraphicSymbol graphic = symbol.getImage();
					WritableImage frame = ((WritableImage[])graphic.getUiInternal())[0];
					// Read all pixels from the frame
					int[] pixels = new int[set.getTileSize() * set.getTileSize()];
					//System.out.println("Read "+fromX+","+fromY+" with "+size+"x"+size);
					frame.getPixelReader().getPixels(0, 0, size, size, format, pixels, 0, size);
					pixelWriter.setPixels(toX, toY, size, size, format, pixels, 0, size);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		byte[] ret = null;
		try {
			ret = BMPEncoder.convertToBMP(target);
			FileOutputStream out = new FileOutputStream("/tmp/test.bmp");
			out.write(ret);
			out.flush();
			out.close();

			ret = PngEncoderFX2.convertToPNG(target);
			out = new FileOutputStream("/tmp/test.png");
			out.write(ret);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		PngEncoderFx encoder = new PngEncoderFx(target, false, PngEncoderFx.FILTER_NONE, 0);
//        byte[] ret = encoder.pngEncode();
//
//		int[] pixels = new int[width * height];
//		PixelReader pixelReader = target.getPixelReader();
//		pixelReader.getPixels(0, 0, width, height, format, pixels, 0, width);
//		ByteBuffer byteBuffer = ByteBuffer.allocate(pixels.length * Integer.BYTES);
//        IntBuffer intBuffer = byteBuffer.asIntBuffer();
//        intBuffer.put(pixels);
//        byte[] ret = byteBuffer.array();
		logger.log(Level.DEBUG, "Have byte buffer with {0} bytes", ret.length);
		return ret;
	}

}
