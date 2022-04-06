package src.simu.model;

/**
 * Tapahtumantyyppejä käytetään simulaattorissa b-tapahtumassa oikean prosessin
 * valitsemiseen
 * 
 * @author Mete Güneysel
 * @author Nicklas Sundell
 *
 */
public enum TapahtumanTyyppi {
	SAIRAALA_SAAPUMINEN, LAAKARI_SAAPUMINEN, LAAKARI_POISTUMINEN, LEIKKAUSOSASTO_SAAPUMINEN, LEIKKAUSOSASTO_POISTUMINEN,
	VUODEOSASTO_SAAPUMINEN, VUODEOSASTO_POISTUMINEN, SAIRAALA_POISTUMINEN,
}
