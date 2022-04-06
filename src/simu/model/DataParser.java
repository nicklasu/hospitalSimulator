package src.simu.model;

import java.util.ArrayList;

/**
 * Parsii tietokannasta saapuvan datan halutuiksi kokonaisuuksiksi
 * @author Mete Güneysel
 *
 */
public class DataParser {
	private Osasto[] osastot;
	private Palvelupiste[] palvelupisteet;

	public DataParser(Osasto[] os, Palvelupiste[] pps) {
		this.osastot = os;
		this.palvelupisteet = pps;
	}

	/**
	 * Palauttaa halutun {@link src.simu.model.Palvelupiste Osaston} palvelupisteet
	 * @param osasto osaston nimi
	 * @return kaikki osaston palvelupisteet
	 */
	public ArrayList<Palvelupiste> getPalvelupisteet(String osasto) {
		ArrayList<Palvelupiste> huoneet = new ArrayList<Palvelupiste>();
		for (Palvelupiste pp : palvelupisteet) {
			if (pp.getOsasto().equals(osasto))
				huoneet.add(pp);
		}
		return huoneet;
	}

	/**
	 * Palauttaa halutun {@link src.simu.model.Osasto Osaston}
	 * @param osasto osaston nimi
	 * @return Osasto
	 */
	public Osasto getOsasto(String osasto) {
		for (Osasto o : osastot) {
			if (o.getNimi().equals(osasto))
				return o;
		}
		return null;
	}
	
	/**
	 * Palauttaa halutun {@link src.simu.model.Palvelupiste Palvelupisteen}
	 * @param nimi palvelupisteen nimi
	 * @return palvelupiste / null
	 */
	public Palvelupiste getPalvelupiste(String nimi) {
		for (Palvelupiste pp : palvelupisteet) {
			if (pp.getNimi().equals(nimi))
				return pp;
		}
		return null;
	}
	
	/**
	 * Palauttaa sairaalan käyttöasteen
	 * @return sairaalan käyttöaste prosentteina
	 */
	public double getKayttoaste() {
		double sairaalanKayttoaste = 0;
		for (Osasto o : osastot) {
			sairaalanKayttoaste += o.getKayttoaste();
		}
		return sairaalanKayttoaste /= osastot.length;
	}
}
