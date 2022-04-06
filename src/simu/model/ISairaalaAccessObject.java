package src.simu.model;

public interface ISairaalaAccessObject {
	boolean lisaaTietokantaan(Object asiakas);
	public Osasto[] getOsastoData(int ajokerta);
	public Palvelupiste[] getPalvelupisteet(int ajokerta);
	public Palvelupiste[] getAulojenDatat(String osasto);
}
