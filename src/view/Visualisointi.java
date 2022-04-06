package src.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Pallerovisualisointia saapuville ja poistuville asiakkaille
 * 
 * @author Nicklas Sundell
 */
public class Visualisointi extends Canvas implements IVisualisointi {

	private GraphicsContext gc;
	private Canvas canvas;
	private double y1;
	private double x1;
	private double x2;
	private double y2;

	/**
	 * alustetaan Graphicscontext käyttämään canvasta
	 * 
	 * @param canvas canvas, jolle pallerot piirretään
	 */
	public Visualisointi(Canvas canvas) {
		this.canvas = canvas;
		gc = canvas.getGraphicsContext2D();
		tyhjennaNaytto();
	}

	/**
	 * Tyhjennetään canvas ja nollataan piirtäjien koordinaattiarvot
	 */
	public void tyhjennaNaytto() {
		gc.setFill(Color.YELLOW);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		x1 = 0;
		y1 = 0;
		x2 = canvas.getWidth() - 10;
		y2 = 0;
	}

	/**
	 * Kutsuttaessa piirtää punaisen palleron canvakselle vasemmalta oikealle.
	 * Nollaa näytön, jos pallero meinaa koskettaa poistuvan asikkaan palleroa.
	 */
	public void uusiAsiakas() {
		gc.setFill(Color.RED);
		gc.fillOval(x1, y1, 10, 10);
		y1 = (y1 + 10) % canvas.getWidth();
		if (y1 == 0)
			x1 += 10;
		if (x2 <= x1)
			tyhjennaNaytto();
	}

	/**
	 * Kutsuttaessa piirtää sinisen palleron canvakselle oikealta vasemmalle. Nollaa
	 * näytön, jos pallero meinaa koskettaa uuden asiakkaan palleroa.
	 */
	public void poistuvaAsiakas() {
		gc.setFill(Color.BLUE);
		gc.fillOval(x2, y2, 10, 10);
		y2 = (y2 + 10) % canvas.getWidth();
		if (y2 == 0)
			x2 -= 10;
		if (x2 <= x1)
			tyhjennaNaytto();
	}

}
