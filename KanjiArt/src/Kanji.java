import java.awt.image.BufferedImage;


public class Kanji implements Comparable<Kanji>{
	BufferedImage drawing;
	int x;
	int y;
	int b = -1;
	
	public Kanji(BufferedImage kanji, int x, int y){
		this.drawing = kanji;
		this.x = x;
		this.y = y;
	}
	
	public int getBrightness(){
		if(b == -1){
		int sum = 0;
		for (int i = 0; i < drawing.getWidth(); i++) {
			for (int j = 0; j < drawing.getHeight(); j++) {
				sum += Main.brightness(drawing.getRGB(i, j));
			}
		}
		b = sum;
		}
		return b;
	}

	@Override
	public int compareTo(Kanji k) {
		return(Integer.compare(k.getBrightness(), this.getBrightness()));
	}
}
