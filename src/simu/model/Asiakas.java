package src.simu.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import src.simu.framework.Kello;
import src.simu.framework.Trace;

/**
 * Asiakas implementoi Comparablen jotta otetaan tärkeysjärjestys huomioon
 * Kutsutaan omamoottorissa lähinnä, mutta myös muualla
 * 
 * @author Nicklas Sundell
 * @author Mete Güneysel
 *
 */
public class Asiakas implements Comparable<Asiakas> {
	private double saapumisaika;
	private double poistumisaika;
	private int id;
	private int kiireellisyys;
	private static int i = 1;
	private transient final int KIIREISIN = 1, KIIREELLINEN = 2, KIIREETON = 3, KIIREETTOMIN = 4;
	private transient Map<String, Double> lapimenoajat = new HashMap<String, Double>();
	private transient Map<String, Double> jonotuspassi = new HashMap<String, Double>();

	private boolean elossa;
	private double jonotusYht;
	private double vastaanottoJono;
	private double leikkausosastoJono;
	private double vuodeosastoJono;

	public Asiakas() {
		elossa = eiPulssia();
		id = i++;
		kiireellisyys = arvoKiireellisyys();
		saapumisaika = Kello.getInstance().getAika();
		Trace.out(Trace.Level.INFO, "Uusi asiakas nro " + id + " saapui klo " + saapumisaika
				+ ". Asiakkaan prioriteetti: " + kiireellisyystoString());
	}

	/**
	 * Nollaa id-laskurin
	 */

	public static void alustaIdLaskin() {
		i = 1;
	}

	/**
	 * Arpoo asiakkaan kiireellisyyden
	 * 
	 * @return Palauttaa kiireellisyyden arvon 1-4 joss 1 on kiireisin 4
	 *         kiireeettömin.
	 */
	public int arvoKiireellisyys() {
		Random satunnainen = new Random();
		int satunnaisluku = (satunnainen.nextInt(99) + 1);
		if (satunnaisluku <= 25) {
			return KIIREISIN;
		}
		if (satunnaisluku <= 50) {
			return KIIREELLINEN;
		}
		if (satunnaisluku <= 75) {
			return KIIREETON;
		}
		if (satunnaisluku <= 100) {
			return KIIREETTOMIN;
		}
		return 0;

	}

	/**
	 * @return Palauttaa poistumisajan
	 */
	public double getPoistumisaika() {
		return poistumisaika;
	}

	/**
	 * Asettaa poistumisajan
	 * 
	 * @param poistumisaika double, poistumisaika
	 */
	public void setPoistumisaika(double poistumisaika) {
		this.poistumisaika = poistumisaika;
	}

	/**
	 * @return saapumisaika
	 */
	public double getSaapumisaika() {
		return saapumisaika;
	}

	/**
	 * Asettaa saapumisajan
	 * 
	 * @param saapumisaika double
	 */
	public void setSaapumisaika(double saapumisaika) {
		this.saapumisaika = saapumisaika;
	}

	/**
	 * palauttaa kiireellisyyden
	 * 
	 * @return kiireellisyys
	 */
	public int getKiireellisyys() {
		return kiireellisyys;
	}

	/**
	 * asettaa kiireellisyyden
	 * 
	 * @param kiireellisyys int
	 */
	public void setKiireellisyys(int kiireellisyys) {
		this.kiireellisyys = kiireellisyys;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Palauttaa numeron perusteella kiireellisyyden string-muotoisen esityksen
	 * 
	 * @return String kiireisyyden perusteella esim. "KIIREISIN"
	 */
	public String kiireellisyystoString() {
		switch (kiireellisyys) {
		case 1:
			return "KIIREISIN";
		case 2:
			return "KIIREELLINEN";
		case 3:
			return "KIIREETON";
		case 4:
			return "KIIREETTOMIN";
		default:
			return "EI MAARITELTY";
		}
	}

	/**
	 * Asiakkaan kiireellisyyteen perustuva laskelma, mitä korkeampi prioriteetti
	 * asiakkaalla sitä nopeammin asiakas kuolee.
	 * 
	 * @return true jos aikaa on kulunut tarpeeksi
	 */
	public boolean eiPulssia() {
		return (Kello.getInstance().getAika() - saapumisaika) / (kiireellisyys / 10.0) > 2000;
	}

	/**
	 * Asettaa asiakkaan elossa-muuttujan falseksi eli asiakas kuolee.
	 */
	public void kuole() {
		elossa = false;
	}

	/**
	 * Katsotaan selvisikö asiakas
	 * 
	 * @return True tai false riippuen asiakkaan elossa-muuttujasta.
	 */
	private String selvisiko() {
		return elossa ? "selvisi" : "ei selvinnyt";
	}

	/**
	 * 
	 * @return jonotuspassi
	 */
	public Map<String, Double> getJonotusPassi() {
		return jonotuspassi;
	}

	/**
	 * 
	 * @return lapimenoajat
	 */
	public Map<String, Double> getLapimenoajat() {
		return lapimenoajat;
	}

	/**
	 * Tulostaa läpimenoajat sysoutilla
	 */
	public void tulostaLapimenoajat() {
		System.out.println("Asiakas " + id + " läpimenoajat:");
		lapimenoajat.forEach((osasto, aika) -> System.out.println("  " + osasto + ": " + aika));
	}

	/**
	 * Tulostaa jonotusajat sysoutilla
	 */
	public void tulostaJonotusajat() {
		System.out.println("Asiakas " + id + " jonotusajat:");
		jonotuspassi.forEach((osasto, aika) -> System.out.println("  " + osasto + ": " + aika));
	}

	/**
	 * Hakee jonotusajat jonotuspassista ja palauttaa kookonaisjonotusajan.
	 * 
	 * @return Kaikkien osastojen yhteisen jonotusajan.
	 */
	public double getJonotusajat() {
		for (Map.Entry<String, Double> entry : jonotuspassi.entrySet()) {
			switch (entry.getKey()) {
			case "Vastaanotto":
				vastaanottoJono += entry.getValue();
				break;
			case "Leikkausosasto":
				leikkausosastoJono += entry.getValue();
				break;
			case "Vuodeosasto":
				vuodeosastoJono += entry.getValue();
				break;
			default:
				break;
			}
			jonotusYht = vastaanottoJono + leikkausosastoJono + vuodeosastoJono;
		}
		return jonotusYht;
	}

	/**
	 * Tulostaa trace.outilla kaikenlaista dataa asiakkaasta
	 */
	public void raportti() {
		Trace.out(Trace.Level.INFO, "\nAsiakas " + id + " valmis! Asiakkaan prioriteetti: " + kiireellisyystoString());
		Trace.out(Trace.Level.INFO, "Asiakas " + selvisiko() + " hengissä.");
		Trace.out(Trace.Level.INFO, "Asiakas " + id + " saapui: " + saapumisaika);
		Trace.out(Trace.Level.INFO, "Asiakas " + id + " poistui: " + poistumisaika);
		Trace.out(Trace.Level.INFO, "Asiakas " + id + " viipyi: " + (poistumisaika - saapumisaika));
		Trace.out(Trace.Level.INFO, "Asiakas " + id + " jonotti yhteensä: " + getJonotusajat());
		tulostaLapimenoajat();
		tulostaJonotusajat();
		System.out.println();
	}

	/**
	 * Ekaksi katsoo kiireellisyyden mukaan, jos kiireellisyydet ovat molemmilla
	 * asiakkailla samat katsoo saapumisajan mukaan
	 */
	@Override
	public int compareTo(Asiakas a) {
		if (this.kiireellisyys < a.kiireellisyys)
			return -1;
		else if (this.kiireellisyys > a.kiireellisyys)
			return 1;
		return Double.compare(this.getSaapumisaika(), a.getSaapumisaika());
	}

}
