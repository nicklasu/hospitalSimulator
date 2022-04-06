package src.simu.model;

import java.util.PriorityQueue;

import src.eduni.distributions.ContinuousGenerator;
import src.simu.framework.Kello;
import src.simu.framework.Tapahtuma;
import src.simu.framework.Tapahtumalista;
import src.simu.framework.Trace;

/**
 * Luokan avulla luodaan ja hallinnoidaan osaston omia palvelupisteitä. Luokka tallennetaan omana tauluna tietokantaan
 * 
 * @author Mete Güneysel
 * @author Nicklas Sundell
 */
public class Osasto {
	private int id;
	private String nimi;
	// Käytetään sql:ssä kun tehdään hakuja eri ajokerroille.
	private int ajokerta;
	private double kokonaisoleskeluaika;
	private int palvellutAsiakkaat;
	private double suoritustehoTunnissa;
	private double keskLapimenoaika;
	private double keskJononpituus;
	private int maxJononKoko;

	// Näiden poistaminen Transientista hajottaa sql-tietokantaan viennin
	private transient Saapumisjono saapumisjono;
	private transient PriorityQueue<Palvelupiste> poistumisjono = new PriorityQueue<Palvelupiste>();
	private transient Palvelupiste[] huoneet;
	private transient Tapahtumalista tapahtumalista;
	private transient TapahtumanTyyppi skeduloitavanTapahtumanTyyppi;
	private transient int avoimetHuoneet;
	private transient int avoinna;

	public Osasto(Tapahtumalista tapahtumalista, TapahtumanTyyppi tyyppi, int maxJononKoko, String nimi, boolean fifo) {
		this.ajokerta = Kello.getInstance().getAjokerta();
		this.tapahtumalista = tapahtumalista;
		this.skeduloitavanTapahtumanTyyppi = tyyppi;
		this.maxJononKoko = maxJononKoko;
		this.nimi = nimi;
		this.saapumisjono = fifo ? new Saapumisjono(true) : new Saapumisjono();
	}

	public Osasto() {
		super();
	}

	/**
	 * Osasto luo itse omat huoneensa (palvelupisteet) asetetuilla arvoilla
	 * 
	 * @param lkm     huoneiden kokonaismäärä
	 * @param avoinna hallitaan kuinka monta huonetta on valmiiksi auki
	 * @param g       palveluajan jakauma
	 * @param lista   tapahtumalista
	 * @param t       huoneen tapahtumatyyppi
	 * @param nimi    huoneen nimi
	 */
	public void luoHuoneet(int lkm, int avoinna, ContinuousGenerator g, Tapahtumalista lista, TapahtumanTyyppi t,
			String nimi) {
		this.avoinna = avoinna;
		huoneet = new Palvelupiste[lkm];
		for (int i = 0; i < lkm; i++) {
			if (i < avoinna) {
				huoneet[i] = new Palvelupiste(g, lista, t, (nimi + (i + 1)), true);
				huoneet[i].setOsasto(this.nimi);
				continue;
			}
			huoneet[i] = new Palvelupiste(g, lista, t, (nimi + (i + 1)), false);
			huoneet[i].setOsasto(this.nimi);
		}
	}

	/**
	 * @return palauttaa maksimijononpituuden kokonaislukuna
	 */
	public int getMaxJononKoko() {
		return maxJononKoko;
	}

	/**
	 * Asettaa maksimijononpituuden kokonaislukuna
	 * 
	 * @param max jonon max pituus
	 */
	public void setMaxJononKoko(int max) {
		maxJononKoko = max;
	}

	/**
	 * Palauttaa sen hetkisen saapumisjonon pituuden
	 * 
	 * @return jonon pituus
	 */
	public int getJononPituus() {
		if (saapumisjono.getKoko() == 0) {
			return 0;
		}
		return saapumisjono.getKoko();
	}

	/**
	 * Lisää asiakkaan osaston saapumisjonoon ja merkkaa jonotuksen alkamisajan
	 * asiakkaan tietoihin
	 * 
	 * @param a osastolle saapuva asiakas
	 */
	public void lisaaJonoon(Asiakas a) {
		a.getJonotusPassi().put(nimi, Kello.getInstance().getAika());
		a.getLapimenoajat().put(nimi, Kello.getInstance().getAika());
		saapumisjono.lisaaJonoon(a);
		paivitaTiedot();
	}

	/**
	 * Palauttaa osaston nimen
	 * 
	 * @return osaston nimi
	 */
	public String getNimi() {
		return nimi;
	}

	/**
	 * Palauttaa seuraavassa vuorossa olevan asiakkaan
	 * 
	 * @return seuraava asiakas
	 */
	public Asiakas otaJonosta() {
		return saapumisjono.otaJonosta();
	}

	/**
	 * Lisää palvelupisteen poistumisjonoon
	 * 
	 * @param pp osaston huone
	 */
	public void lisaaPoistumisjonoon(Palvelupiste pp) {
		poistumisjono.add(pp);
	}

	/**
	 * Palauttaa osastolta poistuvan asiakkaan
	 * 
	 * @return osastolta poistuva asiakas
	 */
	public Asiakas otaPoistumisjonosta() {
		Asiakas a = poistumisjono.poll().otaJonosta();
		asetaLapimenoaika(a);
		if (saapumisjono.getKoko() < maxJononKoko)
			suljeHuone();
		return a;
	}

	/**
	 * Päivittää asiakkaan läpimenoaikatilastoa osastolla vietetyn ajan mukaisesti
	 * 
	 * @param a osastolta poistuva asiakas
	 */
	public void asetaLapimenoaika(Asiakas a) {
		double lapimeno = Kello.getInstance().getAika() - a.getLapimenoajat().get(nimi);
		kokonaisoleskeluaika += lapimeno;
		a.getLapimenoajat().put(nimi, lapimeno);
	}

	/**
	 * Lisää uuden tapahtuman tapahtumalistaan
	 */
	public void aloitaPalvelu() {
		Trace.out(Trace.Level.INFO, "Aloitetaan uusi palvelu asiakkaalle " + saapumisjono.getSeuraava().getId() + " "
				+ skeduloitavanTapahtumanTyyppi);
		tapahtumalista.lisaa(new Tapahtuma(skeduloitavanTapahtumanTyyppi, Kello.getInstance().getAika()));
	}

	/**
	 * Palauttaa vapaan huoneen saapuvalle asiakkaalle Jos jonon pituus on suurempi
	 * kuin sallittu, uusi huone avataan
	 * 
	 * @return palauttaa vapaan huoneen tai jos kaikki huoneet ovat käytössä
	 *         palauttaa null
	 */
	public Palvelupiste annaVapaaHuone() {
		if (saapumisjono.getKoko() > maxJononKoko) {
			avaaHuone();
		}
		for (Palvelupiste p : huoneet) {
			if (!p.onJonossa() && p.onAuki()) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Devaajalle konsolitulostuksia
	 */
	public void printJonotusjono() {
		for (Asiakas a : saapumisjono.getJono()) {
			System.out.println("Asiakas " + a.getId() + " Saapumisaika " + a.getSaapumisaika());
		}
	}

	/**
	 * Palauttaa kaikki osaston huoneet
	 * 
	 * @return Osaston palvelupisteet arrayna
	 */
	public Palvelupiste[] getKaikkiHuoneet() {
		return huoneet;
	}

	/**
	 * Katsoo onko osaston saapumisjonossa asiakkaita
	 * 
	 * @return true / false
	 */
	public boolean onSaapumisjonossa() {
		return saapumisjono.getKoko() != 0;
	}

	/**
	 * Koittaa avata uuden huoneen jos se on mahdollista
	 */
	public void avaaHuone() {
		for (int i = 0; i < huoneet.length; i++) {
			if (!huoneet[i].onAuki()) {
				huoneet[i].avaa();
				break;
			}
		}
	}

	/**
	 * Sulkee käyttämättömän huoneen
	 */
	public void suljeHuone() {
		for (int i = avoinna; i < huoneet.length; i++) {
			Palvelupiste h = huoneet[i];
			if (!h.onVarattu() && h.onAuki()) {
				h.sulje();
				break;
			}
		}
	}

	/**
	 * Devaajalle tulostuksia konsoliin
	 */
	public void tulostaKayttoasteet() {
		for (Palvelupiste p : huoneet) {
			if (p.getPalvellutAsiakkaat() == 0) {
				continue;
			}
			System.out.println(p.getNimi());
			System.out.println("  Palvellut asiakkaat: " + p.getPalvellutAsiakkaat());
			System.out.println("  Käyttöaste: " + p.getKayttoaste() + "%");
			System.out.println("  Asiakkaita tunnissa: " + String.format("%.2f", p.getSuoritusteho()) + "\n");
		}
	}

	/**
	 * Palauttaa suureen kokonaisoleskeluaika
	 * 
	 * @return kokonaisoleskeluaika minuutteina
	 */
	public double getKokonaisoleskeluaika() {
		return kokonaisoleskeluaika;
	}

	/**
	 * Palauttaa keskimääräisen läpimenoajan
	 * 
	 * @return Osaston keskimääräinen läpimenoaika
	 */
	public double getKeskLapimenoaika() {
		return keskLapimenoaika;
	}

	/**
	 * Laskee ja asettaa Osaston keskimääräisen läpimenoajan
	 */
	public void setKeskLapimenoaika() {
		double d = kokonaisoleskeluaika / getPalvellutAsiakkaat();
		keskLapimenoaika = Double.isNaN(d) ? 0 : d;
	}

	/**
	 * Laskee ja palauttaa osaston käyttöasteen
	 * 
	 * @return käyttöaste prosentteina
	 */
	public double getKayttoaste() {
		double osastonKayttoaste = 0;
		for (Palvelupiste p : huoneet) {
			osastonKayttoaste += p.getKayttoaste();
		}
		return osastonKayttoaste / huoneet.length;
	}

	/**
	 * Palauttaa osaston keskimääräisen jonon pituuden
	 * 
	 * @return jonon pituus (asiakkaina)
	 */
	public double getKeskJononpituus() {
		return keskJononpituus;
	}

	/**
	 * Laskee keskimääräisen jonon pituuden (asiakkaina)
	 */
	public void setKeskJononpituus() {
		keskJononpituus = kokonaisoleskeluaika / Kello.getInstance().getAika();
	}

	/**
	 * Palauttaa Osaston suoritustehon
	 * 
	 * @return kuinka monta asiakasta osasto palvelee tunnissa
	 */
	public double getSuoritustehoTunnissa() {
		return suoritustehoTunnissa;
	}

	/**
	 * Laskee osaston suoritustehon tunnissa
	 */
	public void setSuoritustehoTunnissa() {
		suoritustehoTunnissa = getPalvellutAsiakkaat() / Kello.getInstance().getAika() * 60;
	}

	/**
	 * Palauttaa Osaston avoinna olevien huoneiden lukumäärän
	 * 
	 * @return huoneiden lukumäärä
	 */
	public int getAvoimetHuoneet() {
		return avoimetHuoneet;
	}

	/**
	 * Laskee Osaston avoimien huoneiden lukumäärän
	 */
	public void setAvoimetHuoneet() {
		int avoimet = 0;
		for (int i = 0; i < huoneet.length; i++) {
			if (huoneet[i].onAuki()) {
				avoimet++;
			}
		}
		avoimetHuoneet = avoimet;
	}

	/**
	 * Osaston palveltujen asiakkaiden lukumäärä
	 * 
	 * @return asiakkaiden lukumäärä
	 */
	public int getPalvellutAsiakkaat() {
		return palvellutAsiakkaat;
	}

	/**
	 * Laskee palveltujen asiakkaiden lukumäärän
	 */
	public void setPalvellutAsiakkaat() {
		int n = 0;
		for (Palvelupiste p : huoneet) {
			n += p.getPalvellutAsiakkaat();
		}
		palvellutAsiakkaat = n;
	}

	/**
	 * Päivittää osaston tiedot vastaamaan simulaattorin nykyhetkeä
	 */
	public void paivitaTiedot() {
		setPalvellutAsiakkaat();
		setKeskLapimenoaika();
		setKeskJononpituus();
		setSuoritustehoTunnissa();
		setAvoimetHuoneet();
	}

	/**
	 * Devaajalle tulostuksia konsoliin
	 */
	public void raportti() {
		System.out.println("---------------------------------");
		System.out.println(nimi + "n raportti");
		System.out.println("---------------------------------\n");
		System.out.println("Palveltujen asiakkaiden määrä: " + palvellutAsiakkaat);
		System.out.println("Palveltujen asiakkaiden määrä tunnissa: " + String.format("%.2f", suoritustehoTunnissa));
		System.out.println("Asiakkaiden kokonaisoleskeluaika: " + kokonaisoleskeluaika);
		System.out.println("Keskimääräinen läpimenoaika " + keskLapimenoaika);
		System.out.println("Keskimääräinen jononpituus: " + keskJononpituus);
		System.out.println("Avoimien huoneiden määrä: " + avoimetHuoneet);
		System.out.println();
		tulostaKayttoasteet();
		System.out.println();
	}

}
