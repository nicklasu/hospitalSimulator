package src.simu.framework;


import src.controller.IKontrolleri;
import src.simu.model.Osasto;
import src.simu.model.Palvelupiste;

import java.util.HashMap;


public abstract class Moottori extends Thread implements IMoottori { // UUDET MÄÄRITYKSET

	private double simulointiaika = 0;
	private long viive = 0;

	private Kello kello;

	protected Tapahtumalista tapahtumalista;
	protected Palvelupiste sairaalanAula;
	protected HashMap<String, Osasto> sairaala;

	protected IKontrolleri kontrolleri; // UUSI

	public Moottori(IKontrolleri kontrolleri) { // UUSITTU

		this.kontrolleri = kontrolleri; // UUSI

		kello = Kello.getInstance(); // Otetaan kello muuttujaan yksinkertaistamaan koodia

		tapahtumalista = new Tapahtumalista();

		// Palvelupisteet luodaan simu.model-pakkauksessa Moottorin aliluokassa

	}

	@Override
	public void setSimulointiaika(double aika) {
		simulointiaika = aika;
	}

	@Override // UUSI
	public void setViive(long viive) {
		this.viive = viive;
	}

	@Override // UUSI
	public long getViive() {
		return viive;
	}

	@Override
	public void run() { // Entinen aja()
		alustukset(); // luodaan mm. ensimmäinen tapahtuma
		while (simuloidaan()) {
			viive(); // UUSI
			kello.setAika(nykyaika());
			suoritaBTapahtumat();
			yritaCTapahtumat();
		}
		tulokset();

	}

	private void suoritaBTapahtumat() {
		while (tapahtumalista.getSeuraavanAika() == kello.getAika()) {
			suoritaTapahtuma(tapahtumalista.poista());
		}
	}

	private void yritaCTapahtumat() { // määrittele protectediksi, josa haluat ylikirjoittaa

		for (Osasto o : sairaala.values()) {
			if (o.onSaapumisjonossa() && o.annaVapaaHuone() != null) {
				o.aloitaPalvelu();
			}
			for (Palvelupiste pp : o.getKaikkiHuoneet()) {
				if (!pp.onVarattu() && pp.onJonossa()) {
					pp.aloitaPalvelu();
					o.lisaaPoistumisjonoon(pp);
				}
			}
		}
		if (sairaalanAula.onJonossa()) {
			sairaalanAula.aloitaPalvelu();
		}
	}

	private double nykyaika() {
		return tapahtumalista.getSeuraavanAika();
	}

	private boolean simuloidaan() {
		Trace.out(Trace.Level.INFO, "Kello on: " + kello.getAika());
		return kello.getAika() < simulointiaika;
	}

	private void viive() { // UUSI
		Trace.out(Trace.Level.INFO, "Viive " + viive);
		try {
			sleep(viive);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected abstract void alustukset(); // Määritellään simu.model-pakkauksessa Moottorin aliluokassa

	protected abstract void suoritaTapahtuma(Tapahtuma t); // Määritellään simu.model-pakkauksessa Moottorin aliluokassa

	protected abstract void tulokset(); // Määritellään simu.model-pakkauksessa Moottorin aliluokassa

}