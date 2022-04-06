package src.simu.model;

import java.util.PriorityQueue;

import src.eduni.distributions.ContinuousGenerator;
import src.simu.framework.Kello;
import src.simu.framework.Tapahtuma;
import src.simu.framework.Tapahtumalista;
import src.simu.framework.Trace;
/**
 * {@link src.simu.model.Osasto Osaston} alaisuudessa toimiva huone, joka palvelee asiakkaita
 * @author Mete Güneysel
 * @author Nicklas Sundell
 *
 */
public class Palvelupiste implements Comparable<Palvelupiste> {

	private transient PriorityQueue<Asiakas> jono = new PriorityQueue<Asiakas>();
	private transient ContinuousGenerator generator;
	private transient Tapahtumalista tapahtumalista;
	private transient TapahtumanTyyppi skeduloitavanTapahtumanTyyppi;
	private transient boolean varattu = false;
	private int kuolleet = 0;
	private int id;
	private int ajokerta;
	private String nimi;
	private double aktiiviaika;
	private boolean auki = false;
	private String osasto;

	private double paattymisaika;
	private double palvelunkesto;
	private int saapuneetAsiakkaat;
	private int palvellutAsiakkaat;
	private double kayttoaste;
	private double suoritusteho;
	private double suoritustehoTunneissa;

	public Palvelupiste(ContinuousGenerator generator, Tapahtumalista tapahtumalista, TapahtumanTyyppi tyyppi,
			String nimi, boolean auki) {
		this.ajokerta = Kello.getInstance().getAjokerta();
		this.tapahtumalista = tapahtumalista;
		this.generator = generator;
		this.skeduloitavanTapahtumanTyyppi = tyyppi;
		this.nimi = nimi;
		this.auki = auki;
	}

	public Palvelupiste() {

	}

	/**
	 * Lisää asiakkaan jonoon (palveltavaksi)
	 * @param a palveltava asiakas
	 */
	public void lisaaJonoon(Asiakas a) {
		jono.add(a);
	}

	/**
	 * Palauttaa palvelupisteen palveltavana olevan asiakkaan
	 * @return palveltavana oleva asiakas
	 */
	public Asiakas getPalveltavaAsiakas() {
		return jono.peek();
	}

	/**
	 * Poistaa asiakkaan palvelupisteeltä palvelun loputtua
	 * Päivittää myös palvelupisteen tilastoja
	 * @return palveltu asiakas
	 */
	public Asiakas otaJonosta() {
		varattu = false;
		aktiiviaika += palvelunkesto;
		palvellutAsiakkaat++;
		return jono.poll();
	}

	/**
	 * Aloittaa palvelun. Palvelupisteen tila on varattu palvelun ajan
	 * Lisää tapahtumalistaan tapahtuman palveluajan loputtua
	 */
	public void aloitaPalvelu() {
		varattu = true;

		double palveluaika = 0;
		if (generator != null) {
			palveluaika = generator.sample();
		}
		palvelunkesto = palveluaika;
		paattymisaika = Kello.getInstance().getAika() + palveluaika;
		Trace.out(Trace.Level.INFO, "Aloitetaan uusi palvelu asiakkaalle " + jono.peek().getId() + " "
				+ skeduloitavanTapahtumanTyyppi + ". Arvioitu poistumisaika " + paattymisaika);

		tapahtumalista.lisaa(new Tapahtuma(skeduloitavanTapahtumanTyyppi, Kello.getInstance().getAika() + palveluaika));
	}

	/**
	 * Asettaa osaston nimen palvelupisteelle
	 * @param nimi Osaston nimi
	 */
	public void setOsasto(String nimi) {
		this.osasto = nimi;
	}

	/**
	 * Palauttaa palvelupisteen Osaston nimen
	 * @return osaston nimi
	 */
	public String getOsasto() {
		return osasto;
	}

	/**
	 * Palauttaa kuolleiden asiakkaiden määrän (tätä metodia käyttää vain aula)
	 * @return kuolleiden määrä
	 */
	public int getKuolleet() {
		return kuolleet;
	}

	/**
	 * Lisää kuollut kokonaismäärään
	 */
	public void addKuolleet() {
		kuolleet++;
	}

	/**
	 * Palvelupisteen palvellut asiakkaat
	 * @return
	 */
	public int getPalvellutAsiakkaat() {
		return palvellutAsiakkaat;
	}

	/**
	 * Lisää palveltujen asiakkaiden määrää yhdellä
	 */
	public void addPalvellutAsiakkaat() {
		palvellutAsiakkaat++;
	}

	/**
	 * Palvelupisteelle saapuneiden asiakkaiden kokonaismäärä
	 * @return
	 */
	public int getSaapuneetAsiakkaat() {
		return saapuneetAsiakkaat;
	}

	/**
	 * Kasvattaa palvelupisteelle saapuneiden määrää yhdellä
	 */
	public void addSaapuneetAsiakkaat() {
		saapuneetAsiakkaat++;
	}

	/**
	 * Onko palvelupiste varattu eli onko Asiakas palveltavana
	 * @return true (varattu) / false (vapaa)
	 */
	public boolean onVarattu() {
		return varattu;
	}

	/**
	 * Onko palvelupisteellä asiakasta
	 * @return
	 */
	public boolean onJonossa() {
		return jono.size() != 0;
	}

	/**
	 * Onko palvelupiste auki vai kiinni
	 * @return true (auki) / false (kiinni)
	 */
	public boolean onAuki() {
		return auki;
	}

	/**
	 * Avaa palvelupisteen
	 */
	public void avaa() {
		auki = true;
		System.out.println(nimi + " avattu!");
	}

	/**
	 * Sulkee palvelupisteen
	 */
	public void sulje() {
		auki = false;
		System.out.println(nimi + " suljettu!");
	}

	/**
	 * Palauttaa palvelupisteen nimen
	 * @return palvelupisteen nimi
	 */
	public String getNimi() {
		return nimi;
	}

	/**
	 * Palauttaa päättymisajan
	 * @return
	 */
	public double getPaattymisaika() {
		return paattymisaika;
	}

	/**
	 * Laskee ja asettaa palvelun päättymisajan
	 * @param a
	 */
	public void setPaattymisaika(double a) {
		paattymisaika = Math.round(a * 100.0) / 100.0;
	}

	/**
	 * Palauttaa palvelupisteen käyttöajan
	 * @return palvelupisteen käyttöaika
	 */
	public double getKayttoaika() {
		return aktiiviaika;
	}

	/**
	 * Palauttaa käyttöasteen prosentteina
	 * @return käyttöaste
	 */
	public double getKayttoaste() {
		return kayttoaste;
	}

	/**
	 * Laskee ja asettaa palvelupisteen käyttöasteen
	 */
	public void setKayttoaste() {
		kayttoaste = (int) Math.round(aktiiviaika / Kello.getInstance().getAika() * 100);
	}

	/**
	 * Laskee ja asettaa palvelupisteen käyttöasteen
	 * @param ka käyttöaste
	 */
	public void setKayttoaste(double ka) {
		kayttoaste = ka;
	}

	/**
	 * Palauttaa suoritustehon
	 * @return suoritusteho prosentteina
	 */
	public double getSuoritusteho() {
		return suoritusteho;
	}

	/**
	 * Laskee ja asettaa suoritustehon tunnissa
	 */
	public void setSuoritusteho() {
		suoritusteho = getPalvellutAsiakkaat() / Kello.getInstance().getAika() * 60;
	}

	/**
	 * Laskee ja asettaa suoritustehon
	 * @param st
	 */
	public void setSuoritustehoTunneissa(double st) {
		suoritustehoTunneissa = st;
	}

	/**
	 * Palauttaa suoritustehon tunnissa
	 * @return
	 */
	public double getSuoritustehoTunneissa() {
		return this.suoritustehoTunneissa;
	}

	/**
	 * Asiakkaiden poistumiseen tarvittava vertailu
	 */
	@Override
	public int compareTo(Palvelupiste p) {
		return Double.compare(this.paattymisaika, p.getPaattymisaika());
	}
	
	/**
	 * Palauttaa simulaattorin ajokerran
	 * @return
	 */
	public int getAjokerta() {
		return ajokerta;
	}

}
