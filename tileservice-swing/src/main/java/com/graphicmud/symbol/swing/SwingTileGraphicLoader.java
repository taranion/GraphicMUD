package com.graphicmud.symbol.swing;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import javax.imageio.ImageIO;

import com.graphicmud.map.ViewportMap;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.Symbol.GraphicSymbol;
import com.graphicmud.symbol.SymbolSet;
import com.graphicmud.symbol.TileGraphicService;

/**
 *
 */
public class SwingTileGraphicLoader implements TileGraphicService {

	private final static Logger logger = System.getLogger(SwingTileGraphicLoader.class.getPackageName());

	//-------------------------------------------------------------------
	public SwingTileGraphicLoader(Path baseDir) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSymbolImages(SymbolSet set) throws IOException {
		logger.log(Level.WARNING, "ENTER: loadSymbolImages {0}", set.getImageFile());
		Instant start = Instant.now();
		int symbolWidth = set.getTileSize();
		int symbolHeight= set.getTileSize();

		Path path = set.getFile();
		if (path==null) {
			String filename = set.getImageFile();
			if (filename==null) {
				logger.log(Level.WARNING, "Symbolset ''{0} - {1}'' has no attached image", set.getId(), set.getTitle());
				return;
			}
			path = Paths.get(filename);
		}
		FileInputStream fin = new FileInputStream(path.toFile());
		BufferedImage img = ImageIO.read(fin);
		fin.close();

		int symbolsPerLine = img.getWidth() / symbolWidth;
		int symbolLines = img.getHeight() / symbolHeight;
		int total = symbolLines * symbolsPerLine;
		if (set.asList().isEmpty()) {
			logger.log(Level.INFO, "No symbol definitions yet - add {0} of it", total);
			for (int x=0; x<total; x++) {
				set.addSymbol(new Symbol(x));
			}
		}

		int tile = 0;
		int numRead = 0;
		outer:
		for (Symbol symbol : set.asList()) {
			int frames = (symbol.getImage()!=null)?symbol.getImage().getFrames():1;
			BufferedImage[] imgFrames = new BufferedImage[frames];
			for (int f=0; f<frames; f++) {
				int x = numRead%symbolsPerLine;
				int y = numRead/symbolsPerLine;
				int fromX = x*symbolWidth;
				int fromY = y*symbolHeight;

				imgFrames[f] = img.getSubimage(fromX, fromY, symbolWidth, symbolHeight);
				numRead++;
			}
			GraphicSymbol imgSymbol = (symbol.getImage()!=null)?symbol.getImage():(new GraphicSymbol());
			imgSymbol.setFrames(frames);
			imgSymbol.setUiInternal(imgFrames);
			symbol.setImage(imgSymbol);

			byte[][] data = new byte[frames][];
			for (int i=0; i<frames; i++) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(imgFrames[i], "PNG", baos);
				data[i]= baos.toByteArray();
				baos.close();
			}
			imgSymbol.setBytes(data);
			tile++;
		}

		Instant end = Instant.now();
		logger.log(Level.WARNING, "LEAVE: loadSymbolImages took {0} ms", Duration.between(start, end).toMillis());
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.mud.symbol.TileGraphicService#renderMap(int[][], org.prelle.mud.symbol.SymbolSet)
	 */
	@Override
	public byte[] renderMap(ViewportMap<Symbol> data, SymbolSet set) {
		Dimension mapSize = new Dimension(data.getWidth(), data.getHeight());
		Dimension size = new Dimension(data.getWidth()*set.getTileSize(), data.getHeight()*set.getTileSize());

		BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

		int frame = 0;
		for (int y=0; y<mapSize.height; y++) {
			for (int x=0; x<mapSize.width; x++) {
				int xPos = x*set.getTileSize();
				int yPos = y*set.getTileSize();
				Symbol symbol = data.get(x, y);
				GraphicSymbol stamp = symbol.getImage();
				Image imgStamp = ((Image[]) stamp.getUiInternal())[frame];
				img.getGraphics().drawImage(imgStamp, xPos, yPos, set.getTileSize(), set.getTileSize(), null);
			}
			//	    System.out.println("");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream(size.height*size.width*4);
		try {
			boolean success = ImageIO.write(img, "png", baos);
	        byte[] ret = baos.toByteArray();
			logger.log(Level.DEBUG, "Have byte buffer with {0} bytes", ret.length);
			return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

//		int[] pixels = img.getData().getPixels(0, 0, size.width, size.height, (int[])null);
//		ByteBuffer byteBuffer = ByteBuffer.allocate(pixels.length * 4);
//        IntBuffer intBuffer = byteBuffer.asIntBuffer();
//        intBuffer.put(pixels);
//        byte[] ret = byteBuffer.array();
//		logger.log(Level.DEBUG, "Have byte buffer with {0} bytes", ret.length);
//		return ret;
	}

}
