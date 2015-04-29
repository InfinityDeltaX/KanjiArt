import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class Main {
	//33 high
	//62 accross
	//33*62
	
	static int bufferFromFront = 50; //
	
	static int lineThickness = 1;
	static int kanjiWidth = 18; //200

	//all images will be scaled to 51 by 40.
	static int finalWidth = 51;
	static int finalHeight = 40;

	//cropped image is 1400x860.
	
	static int allW;
	static int allH;
	static int margin = 100;

	static int hWin = 1400 / 62 + 1;
	static int vWin = 860 / 33 + 1;
	static int Cthreshold = 400;
	static int bgColor = -1; //white-on-black: -14342875

	public static void main(String[] args) throws IOException {
		BufferedImage all = ImageIO.read(new File("M:\\Documents\\Kanji Art Data\\KanjiRTKWhite.png"));
		allH = all.getHeight();
		allW = all.getWidth();
		bgColor = all.getRGB(0, 0);
		
		System.out.println("Done loading kanji.");
		System.out.println("Please type the image you want drawn.");
		Scanner in = new Scanner(System.in);
		String image = in.nextLine();
		BufferedImage target = ImageIO.read(new File("M:\\Documents\\Kanji Art Data\\Drawings\\" + image + ".png"));
		double ratio = target.getHeight()/(double)target.getWidth();
		finalWidth = (int) (Math.sqrt(2042.0/ratio));
		finalHeight = (int) (2042.0/finalWidth);
		System.out.println(finalWidth + " " + finalHeight);
		finalWidth = 24*2;
		finalHeight = 18*2;
		System.out.println("Done loading target.");
		target = resizeForKanji(target);
		System.out.println("Done resizing target.");
		List<Pixel> targetPixels = getTargetPixels(target);
		System.out.println("Done extracting pixels from target.");
		Collections.sort(targetPixels);
		System.out.println("Done sorting target pixels");
		
		List<Kanji> kanji = smartSplit(all);
		System.out.println("Done splitting kanji.");
		Collections.sort(kanji);
		kanji = kanji.subList(bufferFromFront, kanji.size());
		System.out.println("Done sorting kanji.");
		
		//Collection<Kanji> byLocation = assignKanjiToPixels(kanji, targetPixels).values();
	
		ImageIO.write(drawKanji(assignKanjiToPixels(kanji, targetPixels)), "png", new File("M:\\Documents\\Kanji Art Data\\Drawn\\" + image + ".png"));
		
		System.out.println("DONE!");
	}
	
	public static BufferedImage drawKanji(Map<Pixel, Kanji> input){
		int xJump = kanjiWidth;
		int yJump = kanjiWidth;
		
		int xMargin = kanjiWidth;
		int yMargin = kanjiWidth;
		
		int gridJump = 5;
		
		BufferedImage output = new BufferedImage(finalWidth*xJump+2*xMargin, finalHeight*yJump + 2*yMargin, BufferedImage.TYPE_INT_RGB);
		output = uniformAdjustImage(output, fill);
		
		Graphics g = output.getGraphics();
		
		for(Pixel p : input.keySet()){
			Kanji k = input.get(p);
			g.drawImage(k.drawing, p.x*xJump + xMargin, p.y*yJump + yMargin, null);
		}
		
		g.setColor(Color.RED);
		for(int x = xMargin; x < output.getWidth(); x+=xJump*gridJump){
			g.fillRect(x, 0, lineThickness, output.getHeight());
		}
		for(int y = yMargin; y < output.getHeight(); y+=yJump*gridJump){
			g.fillRect(0, y, output.getWidth(), lineThickness);
		}
		
		
		return output;
	}
	
	public static TreeMap<Pixel, Kanji> assignKanjiToPixels(List<Kanji> kanji, List<Pixel> pixels){ //both must be sorted already.
		
		Comparator<Pixel> byPosition = (Pixel a, Pixel b) -> {
			if(a.x == b.x) return Integer.compare(a.y, b.y);
			else return Integer.compare(a.x, b.x);
		};
		TreeMap<Pixel, Kanji> output = new TreeMap<Pixel, Kanji>(byPosition);
		
		for(int i = 0; i < pixels.size(); i++){
			//System.out.println(i/(double)pixels.size() + "done");
			output.put(pixels.get(i), kanji.get(i));
		}
		return output;
	}
	
	public static List<Pixel> getTargetPixels(BufferedImage input){
		List<Pixel> output = new ArrayList<Pixel>();
		for(int i = 0; i < input.getWidth(); i++){
			for(int j = 0; j < input.getHeight(); j++){
				output.add(new Pixel(i, j, brightness(input.getRGB(i, j))));
			}
		}
		return output;
	}

	public static BufferedImage getScaledImage(BufferedImage image, int width, int height){ //NOT MY CODE!
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleX = (double) width / imageWidth;
		double scaleY = (double) height / imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		return bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
	}

	public static BufferedImage resizeForKanji(BufferedImage input){
		return getScaledImage(input, finalWidth, finalHeight);
	}

	public static int brightness(int color) {
		Color c = new Color(color);
		return c.getRed() + c.getBlue() + c.getGreen();
	}

	public static void printImages(Collection<Kanji> c) throws IOException {
		int i = 0;
		for (Kanji k : c) {
			i++;
			if(k != null) ImageIO.write(k.drawing, "png", new File("M:\\Documents\\Kanji Art Data\\split\\sorted-" + i + ".png"));
		}
	}

	public static void printImages(BufferedImage[][] input) throws IOException {
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[0].length; j++) {
				if(input[i][j] != null) ImageIO.write(input[i][j], "png", new File("M:\\Documents\\Kanji Art Data\\split\\k-" + j + " " + i + ".png"));
			}
		}
	}

	public static BufferedImage[][] roteSplit(BufferedImage input) {
		return slidingWindows(hWin, vWin, hWin, vWin, input); //26, 24, 22, 19
	}

	@FunctionalInterface
	interface pixelOp {
		int op(int c);
	}
	
	static pixelOp fill = (x -> Color.WHITE.getRGB());

	static pixelOp threshold = (x -> {
		Color c = new Color(x);
		int sum = c.getRed() + c.getBlue() + c.getGreen();
		return sum > Cthreshold ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
	});

	public static BufferedImage uniformAdjustImage(BufferedImage input, pixelOp p) {
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
		for (int i = 0; i < input.getHeight(); i++) {
			for (int j = 0; j < input.getWidth(); j++) {
				output.setRGB(j, i, p.op(input.getRGB(j, i)));
			}
		}
		return output;
	}

	public static List<BufferedImage> vSplit(BufferedImage input) {
		boolean prevHadKanji = false;
		List<BufferedImage> cols = new ArrayList<BufferedImage>();
		int streakWidth = 1;
		boolean hasKanji = false;
		for (int x = 0; x < input.getWidth(); x++) {
			hasKanji = checkX(input, x);
			if(!hasKanji && prevHadKanji != hasKanji) { //just coming from kanji.
				try {
					cols.add(input.getSubimage(x - streakWidth, 0, streakWidth, input.getHeight()));
				} catch (RasterFormatException e) {
				}
				;
			}
			if(hasKanji) streakWidth++;
			if(!hasKanji) streakWidth = 1;
			prevHadKanji = hasKanji;
		}
		return cols;
	}

	private static boolean checkX(BufferedImage input, int x) { //does this column run continuously?
		for (int y = 0; y < input.getHeight(); y++) {
			if(input.getRGB(x, y) != bgColor) return true;
		}
		return false;
	}

	public static List<Pair> hSplit(BufferedImage input) {
		boolean prevHadKanji = false;
		List<Pair> rows = new ArrayList<Pair>();
		int streakWidth = 1;
		boolean hasKanji = false;
		for (int y = 0; y < input.getHeight(); y++) {
			hasKanji = checkY(input, y);
			if(!hasKanji && prevHadKanji != hasKanji) { //just coming from kanji.
				//try{rows.add(input.getSubimage(0, y-streakWidth, input.getWidth(), streakWidth));} catch (RasterFormatException e){System.out.println("!");};
				rows.add(new Pair(y - streakWidth, streakWidth));
			}
			if(hasKanji) streakWidth++;
			if(!hasKanji) streakWidth = 1;
			prevHadKanji = hasKanji;
		}
		return rows;
	}

	private static boolean checkY(BufferedImage input, int y) { //does this column run continuously?
		for (int x = 0; x < input.getWidth(); x++) {
			if(input.getRGB(x, y) != bgColor) return true;
		}
		return false;
	}

	public static List<Kanji> smartSplit(BufferedImage input) {
		List<Kanji> kanji = new ArrayList<Kanji>();
		List<ArrayList<BufferedImage>> output = new ArrayList<ArrayList<BufferedImage>>();
		List<BufferedImage> vSplit = vSplit(input);
		List<Pair> hSplit = hSplit(input);

		for (int i = 0; i < vSplit.size(); i++) {
			output.add(new ArrayList<BufferedImage>());
		}

		for (int i = 0; i < vSplit.size(); i++) {
			for (Pair p : hSplit) {
				output.get(i).add(vSplit.get(i).getSubimage(0, p.a, vSplit.get(i).getWidth(), p.b));
			}
		}

		for (int i = 0; i < vSplit.size(); i++) {
			for (int j = 0; j < hSplit.size(); j++) {
				kanji.add(new Kanji(output.get(i).get(j), i, j));
			}
		}

		//conversion
		BufferedImage[][] ret = new BufferedImage[output.size()][output.get(0).size()];
		for (int i = 0; i < output.size(); i++) {
			for (int j = 0; j < output.get(0).size(); j++) {
				try {
					ret[i][j] = output.get(i).get(j);
				} catch (IndexOutOfBoundsException e) {
				}
			}
		}
		
		return kanji;
	}

	public static BufferedImage[][] slidingWindows(int windowWidth, int windowHeight, int stepHorizontal, int stepVertical, BufferedImage inputImage) {
		int stepsNeededHorizontal = (inputImage.getWidth() - windowWidth) / stepHorizontal + 1;
		int stepsNeededVertical = (inputImage.getHeight() - windowHeight) / stepVertical + 1;
		BufferedImage[][] output = new BufferedImage[stepsNeededHorizontal][stepsNeededVertical];
		for (int i = 0; i < stepsNeededHorizontal; i++) {
			for (int j = 0; j < stepsNeededVertical; j++) {
				output[i][j] = new BufferedImage(windowWidth, windowHeight, inputImage.getType());
				Graphics2D g = (Graphics2D) output[i][j].getGraphics();
				g.drawImage(inputImage, 0, 0, windowWidth, windowHeight, i * stepHorizontal, j * stepVertical, i * stepHorizontal + windowWidth, j * stepVertical + windowHeight, null);
				g.dispose();
			}
		}
		return output;
	}

	static class Pair {
		int a;
		int b;

		public Pair(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}
	
	static class Pixel implements Comparable<Pixel>{
		int x;
		int y;
		int b;

		public Pixel(int x, int y, int b) {
			this.x = x;
			this.y = y;
			this.b = b;
		}

		@Override
		public int compareTo(Pixel o) {
			return Integer.compare(o.b, this.b);
		}
	}

}
