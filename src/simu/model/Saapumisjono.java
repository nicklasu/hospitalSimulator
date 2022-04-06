package src.simu.model;

import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Luokan avulla määritetään palvellaanko {@link src.simu.model.Asiakas
 * Asiakas} saapumis- vai tärkeysjärjestyksessä
 * 
 * @author Mete Güneysel
 *
 */
public class Saapumisjono {
	private PriorityQueue<Asiakas> saapumisjono;
	private LinkedList<Asiakas> saapumisjonoFifo;
	private boolean fifo;

	public Saapumisjono(boolean fifo) {
		this.saapumisjonoFifo = new LinkedList<Asiakas>();
		this.fifo = fifo;
	}

	public Saapumisjono() {
		this.saapumisjono = new PriorityQueue<Asiakas>();
	}

	/**
	 * Lisää asiakkaan saapumisjonoon
	 * @param a saapunut asiakas
	 */
	public void lisaaJonoon(Asiakas a) {
		if (fifo)
			saapumisjonoFifo.add(a);
		else {
			saapumisjono.add(a);
		}
	}

	/**
	 * Palauttaa seuraavana jonossa olevan asiakkaan
	 * @return {@link src.simu.model.Asiakas Asiakas}
	 */
	public Asiakas otaJonosta() {
		return fifo ? saapumisjonoFifo.poll() : saapumisjono.poll();
	}

	/**
	 * Palauttaa jonon pituuden kokonaislukuna
	 * @return jonon pituus
	 */
	public int getKoko() {
		return fifo ? saapumisjonoFifo.size() : saapumisjono.size();
	}

	/**
	 * Palauttaa jonon arrayna
	 * @return {@link src.simu.model.Asiakas Asiakas} []
	 */
	public Asiakas[] getJono() {
		return fifo ? (Asiakas[]) saapumisjonoFifo.toArray() : (Asiakas[]) saapumisjono.toArray();
	}

	/**
	 * Palauttaa seuraavana jonossa olevan asiakkaan mutta ei poista listasta
	 * @return Seuraavana vuorossa oleva {@link src.simu.model.Asiakas Asiakas}
	 */
	public Asiakas getSeuraava() {
		return fifo ? saapumisjonoFifo.peek() : saapumisjono.peek();
	}
}
