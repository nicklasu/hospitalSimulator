package src.controller;

import src.simu.model.Osasto;
import src.simu.model.Palvelupiste;

public interface IKontrolleri {

	// Rajapinta, joka tarjotaan käyttöliittymälle:

	public void kaynnistaSimulointi();

	public void nopeuta();

	public void hidasta();

	// Rajapinta, joka tarjotaan moottorille:

	public boolean getPalvelujarjestys();

	public void visualisoiSaapuvaAsiakas();

	public void naytaLopputulostus(String tulostus);

	public void naytavaliTulostukset(Palvelupiste aula, Osasto laakarit, Osasto leikkaus, Osasto vuode,
									 double suoritusteho, double kayttoaste);

	public void naytaAjokerrat();

	void visualisoiPoistuvaAsiakas();

	void setViive();

	int[] getHuoneMaara();

	int[] getMaxJono();

	int[] getAukiolevatHuoneet();

	void setAluksiTexts();

	void setSaapumisvaliText();

	void setHuoneidenMaaraTexts();

	void setMaxJonoTexts();

}
