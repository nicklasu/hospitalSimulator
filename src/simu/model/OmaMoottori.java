package src.simu.model;

import java.util.HashMap;

import src.controller.IKontrolleri;
import src.eduni.distributions.Negexp;
import src.eduni.distributions.Normal;
import src.simu.framework.Kello;
import src.simu.framework.Moottori;
import src.simu.framework.Saapumisprosessi;
import src.simu.framework.Tapahtuma;
import src.simu.framework.Trace;

/**
 * Luokassa luodaan sairaalaympäristö, suoritetaan b-tapahtumat ja
 * lopputulostukset
 * 
 * @author Mete Güneysel
 * @author Nicklas Sundell
 */
public class OmaMoottori extends Moottori {
	private Saapumisprosessi saapumisprosessi;
	private double asiakkaidenKa = 0;
	private double keskiarvoTunneissa = 0;
	private final int saapumispSatunnaisuus = 5;

	public OmaMoottori(IKontrolleri kontrolleri/*, ISairaalaAccessObject sairaalaDAO*/) {
		super(kontrolleri);
		Kello.getInstance().setAika(0);
		saapumisprosessi = new Saapumisprosessi(new Negexp(20, saapumispSatunnaisuus), tapahtumalista,
				TapahtumanTyyppi.SAIRAALA_SAAPUMINEN);
		luoSairaala(); // Luodaan sairaalan osastot, metodi tiedoston lopussa
	}

	@Override
	protected void alustukset() {
		saapumisprosessi.generoiSeuraava(); // Ensimmäinen saapuminen järjestelmään
	}

	@Override
	protected void suoritaTapahtuma(Tapahtuma t) { // B-vaiheen tapahtumat

		Asiakas a;
		Palvelupiste AULA = sairaalanAula;

		Osasto VASTAANOTTO = sairaala.get("VASTAANOTTO");
		Osasto LEIKKAUSOSASTO = sairaala.get("LEIKKAUSOSASTO");
		Osasto VUODEOSASTO = sairaala.get("VUODEOSASTO");

		switch (t.getTyyppi()) {

		case SAIRAALA_SAAPUMINEN:
			AULA.addSaapuneetAsiakkaat();
			a = new Asiakas();
			saapumisprosessi.generoiSeuraava(); // Generoi seuraava asiakas
			kontrolleri.visualisoiSaapuvaAsiakas();
			// Asiakkaan kiireellisyys arvotaan väliltä 1-4, asiakkaan saapuessa sairaalaan
			// Seuraava palvelu määräytyy asiakkaan kiireellisyyden mukaan
			switch (a.getKiireellisyys()) {
			case 1:
				LEIKKAUSOSASTO.lisaaJonoon(a);
				break;
			case 2:
				VASTAANOTTO.lisaaJonoon(a);
				break;
			case 3:
				VASTAANOTTO.lisaaJonoon(a);
				break;
			case 4:
				VASTAANOTTO.lisaaJonoon(a);
				break;
			}
			break;

		case LAAKARI_SAAPUMINEN:
			saapumisToimenpiteet(VASTAANOTTO);
			break;

		case LAAKARI_POISTUMINEN:
			a = VASTAANOTTO.otaPoistumisjonosta();
			// Lääkäri tarkastaa ja arvioi asiakkaan kiireellisyyden vastaanotolla
			a.setKiireellisyys(a.arvoKiireellisyys());
			switch (a.getKiireellisyys()) {
			case 1:
				LEIKKAUSOSASTO.lisaaJonoon(a);
				break;
			case 2:
				LEIKKAUSOSASTO.lisaaJonoon(a);
				break;
			case 3:
				VUODEOSASTO.lisaaJonoon(a);
				break;
			case 4:
				AULA.lisaaJonoon(a);
				break;
			}
			break;

		case LEIKKAUSOSASTO_SAAPUMINEN:
			saapumisToimenpiteet(LEIKKAUSOSASTO);
			break;

		case LEIKKAUSOSASTO_POISTUMINEN:
			a = LEIKKAUSOSASTO.otaPoistumisjonosta();
			VUODEOSASTO.lisaaJonoon(a);
			break;

		case VUODEOSASTO_SAAPUMINEN:
			saapumisToimenpiteet(VUODEOSASTO);
			break;

		case VUODEOSASTO_POISTUMINEN:
			a = VUODEOSASTO.otaPoistumisjonosta();
			AULA.lisaaJonoon(a);
			break;

		case SAIRAALA_POISTUMINEN:
			a = AULA.otaJonosta();
			a.setPoistumisaika(Kello.getInstance().getAika());
			a.raportti();
			kontrolleri.visualisoiPoistuvaAsiakas();

			asiakkaidenKa += (a.getPoistumisaika() - a.getSaapumisaika());
			keskiarvoTunneissa = asiakkaidenKa / AULA.getPalvellutAsiakkaat() / 60;
			AULA.setSuoritustehoTunneissa(keskiarvoTunneissa);
			Trace.out(Trace.Level.INFO, "Keskimääräisen asiakkaan läpimenoaika tunneissa tällä hetkellä: "
					+ String.format("%.2f", keskiarvoTunneissa));
			break;
		}

		kontrolleri.naytavaliTulostukset(AULA, VASTAANOTTO, LEIKKAUSOSASTO, VUODEOSASTO, keskiarvoTunneissa,
				getSairaalanKayttoaste());
	}

	/**
	 * Ottaa {@link src.simu.model.Asiakas asiakkaan} edellisen osaston jonosta ja lisää
	 * osaston jonoon tai palveltavaksi, tilanteen mukaan. Asiakas kuolee, jos ei
	 * pääse tarpeeksi ajoissa leikkausosaston jonosta leikkaukseen
	 * 
	 * @param osasto {@link src.simu.model.Osasto Osasto}
	 */
	public void saapumisToimenpiteet(Osasto osasto) {
		Asiakas a = osasto.otaJonosta();
		if (osasto.getNimi().equals("Leikkausosasto")) {
			if (a.eiPulssia()) {
				a.kuole();
				System.out.println("Asiakas " + a.getId() + " kuoli!");
				sairaalanAula.addKuolleet();
				sairaalanAula.lisaaJonoon(a);
				return;
			}
		}

		Palvelupiste p = osasto.annaVapaaHuone();
		p.setKayttoaste();
		p.setSuoritusteho();
		a.getJonotusPassi().put(osasto.getNimi(),
				Kello.getInstance().getAika() - a.getJonotusPassi().get(osasto.getNimi()));
		p.lisaaJonoon(a);
	}

	/**
	 * Metodia kutsutaan luokan konstruktorissa Haetaan kontrollerin avulla
	 * käyttöliittymään syötetyt tiedot ja luodaan sairaala niillä arvoilla. Esim.
	 * {@link src.simu.model.Palvelupiste palvelupisteiden} määrä
	 * {@link src.simu.model.Osasto osastoilla}
	 */
	private void luoSairaala() {
		Kello.getInstance().addAjokerta();
		// Jos palvelujärjestys on true, niin palvelua annetaan tärkeysjärjestyksessä.
		boolean palvelujarjestys = kontrolleri.getPalvelujarjestys();
		int[] huoneMaara = kontrolleri.getHuoneMaara();
		int[] maxJono = kontrolleri.getMaxJono();
		sairaalanAula = new Palvelupiste(null, tapahtumalista, TapahtumanTyyppi.SAIRAALA_POISTUMINEN, "Sairaalan aula",
				true);
		sairaalanAula.setOsasto("Aula");
		sairaala = new HashMap<String, Osasto>();
		sairaala.put("VASTAANOTTO", new Osasto(tapahtumalista, TapahtumanTyyppi.LAAKARI_SAAPUMINEN, maxJono[0],
				"Vastaanotto", palvelujarjestys));
		sairaala.get("VASTAANOTTO").luoHuoneet(huoneMaara[0], kontrolleri.getAukiolevatHuoneet()[0], new Normal(45, 3),
				tapahtumalista, TapahtumanTyyppi.LAAKARI_POISTUMINEN, "Lääkäri ");
		sairaala.put("LEIKKAUSOSASTO", new Osasto(tapahtumalista, TapahtumanTyyppi.LEIKKAUSOSASTO_SAAPUMINEN,
				maxJono[1], "Leikkausosasto", palvelujarjestys));
		sairaala.get("LEIKKAUSOSASTO").luoHuoneet(huoneMaara[1], kontrolleri.getAukiolevatHuoneet()[1],
				new Normal(120, 3), tapahtumalista, TapahtumanTyyppi.LEIKKAUSOSASTO_POISTUMINEN, "Leikkaussali ");
		sairaala.put("VUODEOSASTO", new Osasto(tapahtumalista, TapahtumanTyyppi.VUODEOSASTO_SAAPUMINEN, maxJono[2],
				"Vuodeosasto", palvelujarjestys));
		sairaala.get("VUODEOSASTO").luoHuoneet(huoneMaara[2], kontrolleri.getAukiolevatHuoneet()[2], new Normal(40, 3),
				tapahtumalista, TapahtumanTyyppi.VUODEOSASTO_POISTUMINEN, "Vuodepaikka ");

	}

	/**
	 * Laskee sairaalan käyttöasteen {@link src.simu.model.Osasto osastojen}
	 * käyttöasteiden avulla
	 * 
	 * @return sairaalan käyttöaste prosentteina
	 */
	private double getSairaalanKayttoaste() {
		double sairaalanKayttoaste = 0;
		for (Osasto o : sairaala.values()) {
			sairaalanKayttoaste += o.getKayttoaste();
		}
		return sairaalanKayttoaste /= sairaala.values().size();
	}

	/**
	 * Siirretään tiedot tietokantaan simulaattorin loputtua. Devaajalle konsoliin
	 * lopputulostuksia. Tässä näytetään myös {@link src.simu.model.Palvelupiste
	 * palvelupisteiden} suorityskykysuureita.
	 * 
	 */
	// TODO:Palvelupisteiden suorituskykysuureiden näyttäminen käyttöliittymässä
	@Override
	protected void tulokset() {
		Osasto VASTAANOTTO = sairaala.get("VASTAANOTTO");
		Osasto LEIKKAUSOSASTO = sairaala.get("LEIKKAUSOSASTO");
		Osasto VUODEOSASTO = sairaala.get("VUODEOSASTO");
		Palvelupiste AULA = sairaalanAula;
		AULA.setKayttoaste(getSairaalanKayttoaste());

		System.out.println("Simulointi päättyi kello " + Kello.getInstance().getAika());
		System.out.println("Saapuneet asiakkaat yhteensä: " + AULA.getSaapuneetAsiakkaat());
		System.out.println("Poistuneet asiakkaat yhteensä: " + AULA.getPalvellutAsiakkaat());
		System.out.println("Kuolleiden määrä " + AULA.getKuolleet() + "\n");
		AULA.setPaattymisaika(Kello.getInstance().getAika());

		VASTAANOTTO.raportti();
		LEIKKAUSOSASTO.raportti();
		VUODEOSASTO.raportti();

		System.out.println("THE END");
		kontrolleri.naytaAjokerrat();
		System.out.println(kontrolleri.getPalvelujarjestys() ? "Tärkeysjärjestys" : "Saapumisjärjestys");

		// UUTTA graafista
		kontrolleri.naytaLopputulostus("Kuolleiden määrä: " + String.valueOf(AULA.getKuolleet()));
	}

	/**
	 * Asetetaan kuinka usein asiakkaita saapuu sairaalaan
	 */
	@Override
	public void setSaapumisprosessiMaara(int g) {
		saapumisprosessi.setGeneraattori(new Negexp(g, saapumispSatunnaisuus));
	}
}
