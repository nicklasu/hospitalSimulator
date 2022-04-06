package src.simu.framework;

public class Kello {

	private double aika;
	private int ajokerta;
	private static Kello instanssi;

	private Kello() {
		aika = 0;
	}

	public static Kello getInstance() {
		if (instanssi == null) {
			instanssi = new Kello();
		}
		return instanssi;
	}

	public void setAika(double aika) {
		this.aika = aika;
	}

	public double getAika() {
		return aika;
	}

	public int getAjokerta() {
		return ajokerta;
	}

	public void addAjokerta() {
		ajokerta++;
	}
}
