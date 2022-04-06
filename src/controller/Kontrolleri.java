package src.controller;

import java.text.DecimalFormat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import src.simu.framework.IMoottori;
import src.simu.framework.Kello;
import src.simu.model.OmaMoottori;
import src.simu.model.Osasto;
import src.simu.model.Palvelupiste;
import src.view.IVisualisointi;
import src.view.Visualisointi;


/**
 * Kontrolleri keskustelee muiden tasojen kanssa ja välittää tietoa moottorilta
 * guille sekä toisinpäin.
 * 
 * @author Nicklas Sundell
 * @author Mete Güneysel
 */

public class Kontrolleri implements IKontrolleri { // UUSI
	private GraphicsContext gc;
	private IVisualisointi asiakasVisualisointi;
	private IMoottori moottori;
	@FXML
	private TextField aika;
	@FXML
	private TextField viive;
	@FXML
	private Label tulosLabel;
	@FXML
	private Label kuolleetLabel;
	@FXML
	private Label palvellutasiakkaatLabel;
	@FXML
	private Label asiakkaitasairaalassaLabel;
	@FXML
	private Label kayttoasteLabel;
	@FXML
	private Label suoritustehoLabel;
	@FXML
	private Label asiakkaidenSaapumisTiheysLabel;
	@FXML
	private TextFlow tulostukset;
	@FXML
	private RadioButton rbTarkeys;
	@FXML
	private ListView<String> ajolista;
	@FXML
	private Canvas superCanvas;
	@FXML
	private Label laakarijono;
	@FXML
	private Label leikkausjono;
	@FXML
	private Label vuodejono;
	@FXML
	private LineChart<String, Number> lineChart;
	@FXML
	private ImageView kaynnistaBtn;
	@FXML
	private Slider saapumisvaliSlider;
	@FXML
	private Slider laakaritSlider;
	@FXML
	private Slider salitSlider;
	@FXML
	private Slider petipaikatSlider;
	@FXML
	private Text aukiOlevatLaakaritAluksiText;
	@FXML
	private Text aukiOlevatSalitAluksiText;
	@FXML
	private Text aukiolevatPetipaikatAluksiText;
	@FXML
	private Slider aukiOlevatLaakaritAluksiSlider;
	@FXML
	private Slider aukiOlevatSalitAluksiSlider;
	@FXML
	private Slider aukiolevatPetipaikatAluksiSlider;
	@FXML
	private Slider laakarinMaxJonoSlider;
	@FXML
	private Slider leikkausosastonMaxJonoSlider;
	@FXML
	private BarChart<String, Number> barChart;
	@FXML
	private Text petipaikkojenMaaraText;
	@FXML
	private Text laakareidenMaaraText;
	@FXML
	private Text salienMaaraText;
	@FXML
	private Text laakaritJonoText;
	@FXML
	private Text leikkausosastotJonoText;
	@FXML
	private Text saapuvatAsiakkaatText;

	/**
	 * Metodin tarkoitus on käynnistää simulaatio. Kutsutaan napinpainalluksella
	 * GUI:sta
	 */
	@Override
	public void kaynnistaSimulointi() {
		moottori = new OmaMoottori(this/*, dao*/); // luodaan uusi moottorisäie jokaista simulointia varten
		try {
			moottori.setSimulointiaika(Double.parseDouble(aika.getText()));
		} catch (NumberFormatException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("Väärä syöte simulointiaikana! Käytetään oletusaikaa 10000!");
			a.show();
			moottori.setSimulointiaika(10000);
			aika.setText("100000");
		}
		setViive();
		((Thread) moottori).start();
		moottori.setSaapumisprosessiMaara((int) saapumisvaliSlider.getValue());
		sliderDisable(true);
		kaynnistaBtn.setDisable(true);
		kaynnistaBtn.setOpacity(0.2);
		aika.setDisable(true);
		asiakasVisualisointi = new Visualisointi(superCanvas);
		gc = superCanvas.getGraphicsContext2D();
		gc.setFill(Color.YELLOW);
		gc.fillRect(0, 0, 200, 400);
		laakarijono.setVisible(true);
		leikkausjono.setVisible(true);
		vuodejono.setVisible(true);
	}

	/**
	 * Metodilla asetetaan viive GUI:sta syöttämällä numeroita viive-kenttään
	 */
	@Override
	public void setViive() {
		try {
			if (moottori != null)
				if (viive.getText() == "") {
					moottori.setViive(0);
					viive.setText("0");
				} else {
					if (Long.parseLong(viive.getText()) > 5000) {
						moottori.setViive(5000);
						return;
					}
					moottori.setViive(Long.parseLong(viive.getText()));
				}
		} catch (NumberFormatException e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("Väärä syöte viiveenä! Käytetään oletuviivettä 20!");
			a.show();
			moottori.setViive(20);
			viive.setText("20");
		}
	}

	/**
	 * Metodi hidastaa simulaation toimintaa. Kutsutaan napinpainalluksella.
	 */
	@Override
	public void hidasta() { // hidastetaan moottorisäiettä
		if (moottori == null) {
			if (viive.getText().equals("0")) {
				viive.setText("1");
				return;
			}
			viive.setText(String.valueOf((long) Math.ceil(Long.parseLong(viive.getText()) * 1.1)));
			return;
		}
		if (moottori.getViive() == 0) {
			moottori.setViive(moottori.getViive() + 1);
			viive.setText(String.valueOf(moottori.getViive()));
			return;
		}
		moottori.setViive((long) Math.ceil((moottori.getViive() * 1.1)));
		viive.setText(String.valueOf(moottori.getViive()));

	}

	/**
	 * Metodi nopeuttaa simulaation toimintaa. Kutsutaan napinpainalluksella.
	 */
	@Override
	public void nopeuta() { // nopeutetaan moottorisäiettä
		if (moottori == null) {
			if (viive.getText().equals("0")) {
				viive.setText("0");
				return;
			}
			viive.setText(String.valueOf((long) Math.floor(Long.parseLong(viive.getText()) * 0.9)));
			return;
		}
		moottori.setViive((long) (moottori.getViive() * 0.9));
		viive.setText(String.valueOf(moottori.getViive()));
	}

	/**
	 * Metodi päivittää GUI:n uusilla arvoilla.
	 * 
	 * @param aika double, päivittää ajan 
	 * @param kuolleet int, päivittää  kuolleiden määrän
	 * @param palvellutAsiakkaat int, päivittää palvelleiden asiakkaiden määrän
	 * @param asiakkaatSairaalassa int, päivittää asiakkaiden määrän sairaalassa
	 * @param kayttoaste double, päivittää sairaalan käyttöasteen
	 * @param suoritusteho double, päivittää suoritustehon
	 */
	public void paivitaUI(double aika, int kuolleet, int palvellutAsiakkaat, int asiakkaatSairaalassa,
			double kayttoaste, double suoritusteho) {
		DecimalFormat formatter = new DecimalFormat("#0.00");
		tulosLabel.setText(formatter.format(aika));
		kuolleetLabel.setText(String.valueOf(kuolleet));
		palvellutasiakkaatLabel.setText(String.valueOf(palvellutAsiakkaat));
		asiakkaitasairaalassaLabel.setText(String.valueOf(asiakkaatSairaalassa));
		kayttoasteLabel.setText(String.valueOf(formatter.format(kayttoaste) + "%"));
		suoritustehoLabel.setText(formatter.format(suoritusteho) + " h");
	}

	/**
	 * Päivittää GUI:n kutsumalla paivitaUI:ta, kutsutaan omaMoottorista.
	 * 
	 * @param aula Palvelupiste aula, ainoa palvelupiste joka ei ole osaston alainen
	 * @param laakarit Osasto 
	 * @param leikkaus Osasto 
	 * @param vuode	Osasto
	 * @param suoritusteho double, käytetään UI:n päivitykseen
	 * @param kayttoaste double, käytetään UI:n päivitykseen
	 */
	@Override
	public void naytavaliTulostukset(Palvelupiste aula, Osasto laakarit, Osasto leikkaus, Osasto vuode,
									 double suoritusteho, double kayttoaste) {
		Platform.runLater(() -> {
			laakarijono.setText(String.valueOf(laakarit.getJononPituus()));
			leikkausjono.setText(String.valueOf(leikkaus.getJononPituus()));
			vuodejono.setText(String.valueOf(vuode.getJononPituus()));
			paivitaUI(Kello.getInstance().getAika(), aula.getKuolleet(), aula.getPalvellutAsiakkaat(),
					aula.getSaapuneetAsiakkaat() - aula.getPalvellutAsiakkaat(), kayttoaste, suoritusteho);

			barChart.getData().clear();
			XYChart.Series<String, Number> bcSeries = new XYChart.Series<String, Number>();
			bcSeries.getData().add(new XYChart.Data<String, Number>(laakarit.getNimi(), laakarit.getJononPituus()));
			bcSeries.getData().add(new XYChart.Data<String, Number>(leikkaus.getNimi(), leikkaus.getJononPituus()));
			bcSeries.getData().add(new XYChart.Data<String, Number>(vuode.getNimi(), vuode.getJononPituus()));
			barChart.getData().add(bcSeries);
		});
	}

	/**
	 * Metodi näyttää piirtää graafin lopuksi. Kutsutaan omaMoottorista lopuksi.
	 */
	@Override
	public void naytaLopputulostus(String tulostus) {
		Platform.runLater(() -> {
			sliderDisable(false);
			kaynnistaBtn.setDisable(false);
			kaynnistaBtn.setOpacity(1);
			aika.setDisable(false);
			piirraGraafi();
		});
	}

	/**
	 * Visualisoi saapuvan asiakkaan pallerona. Kutsutaan omamoottorin
	 * suoritatapahtuma sairaalan saapumisen yhteydessä
	 */
	@Override
	public void visualisoiSaapuvaAsiakas() {
		Platform.runLater(new Runnable() {
			public void run() {
				asiakasVisualisointi.uusiAsiakas();
			}
		});
	}

	/**
	 * Visualisoi poistuvan asiakkaan pallerona. Kutsutaan omamoottorin
	 * suoritatapahtuma sairaalasta poistumisen yhteydessä
	 */
	@Override
	public void visualisoiPoistuvaAsiakas() {
		Platform.runLater(new Runnable() {
			public void run() {
				asiakasVisualisointi.poistuvaAsiakas();
			}
		});
	}

	// Nämä asiat voisi tehdä yhdessäkin metodissa, mutta näin on mielestäni
	// selkeämpää.
	/**
	 * Metodilla saadaan GUI:sta huonemäärä, mikä syötetään sairaalan alkuarvoihin
	 * omamoottoriin.
	 */
	@Override
	public int[] getHuoneMaara() {
		// Lääkäri 0, Leikkaussali 1, Vuodepaikka 2
		int[] huoneMaara = { (int) laakaritSlider.getValue(), (int) salitSlider.getValue(),
				(int) petipaikatSlider.getValue() };
		return huoneMaara;
	}

	/**
	 * Metodilla saadaan GUI:sta maksimijononpituus, mikä syötetään sairaalan
	 * alkuarvoihin omamoottoriin.
	 */
	@Override
	public int[] getMaxJono() {
		// Lääkäri 0, leikkausosasto 1, Vuodeosasto 2
		int[] maxJono = { (int) laakarinMaxJonoSlider.getValue(), (int) leikkausosastonMaxJonoSlider.getValue(), 0 };
		return maxJono;
	}

	/**
	 * Metodilla saadaan GUI:sta aluksi auki olevien huoneiden määrä, mikä syötetään
	 * sairaalan alkuarvoihin omamoottoriin.
	 */
	@Override
	public int[] getAukiolevatHuoneet() {
		int[] aukiolevatHuoneet = {
				// intiin castaaminen pudottaa desimaalit, eli pyöristää automaattisesti alas
				(int) ((aukiOlevatLaakaritAluksiSlider.getValue() / 100) * laakaritSlider.getValue()),
				(int) ((aukiOlevatSalitAluksiSlider.getValue() / 100) * salitSlider.getValue()),
				(int) ((aukiolevatPetipaikatAluksiSlider.getValue() / 100) * petipaikatSlider.getValue()) };
		return aukiolevatHuoneet;
	}

	/**
	 * Disabloi sliderit kutsuttaessa.
	 * 
	 * @param b Boolean, kun true disabloi kaikki sliderit. 
	 */
	private void sliderDisable(Boolean b) {
		laakaritSlider.setDisable(b);
		salitSlider.setDisable(b);
		petipaikatSlider.setDisable(b);
		laakarinMaxJonoSlider.setDisable(b);
		leikkausosastonMaxJonoSlider.setDisable(b);
		aukiOlevatLaakaritAluksiSlider.setDisable(b);
		aukiOlevatSalitAluksiSlider.setDisable(b);
		aukiolevatPetipaikatAluksiSlider.setDisable(b);
	}

	/**
	 * Asettaa aluksi auki olevien slidereiden päällä olevat tekstit reaaliajassa.
	 */
	@Override
	public void setAluksiTexts() {
		setSliderTexts(aukiOlevatLaakaritAluksiText, aukiOlevatLaakaritAluksiSlider, "%");
		setSliderTexts(aukiOlevatSalitAluksiText, aukiOlevatSalitAluksiSlider, "%");
		setSliderTexts(aukiolevatPetipaikatAluksiText, aukiolevatPetipaikatAluksiSlider, "%");
	}

	/**
	 * Asettaa saapumisvälin olevien sliderin päällä olevan tekstin reaaliajassa.
	 */
	@Override
	public void setSaapumisvaliText() {
		saapumisvaliSlider.valueProperty().addListener((observable, o, n) -> {
			if (moottori != null)
				moottori.setSaapumisprosessiMaara((int) saapumisvaliSlider.getValue());
			setSliderTexts(saapuvatAsiakkaatText, saapumisvaliSlider, "min");
		});
	}

	/**
	 * Asettaa huoneidenmäärän sliderien päällä olevat tekstit reaaliajassa.
	 */
	@Override
	public void setHuoneidenMaaraTexts() {
		setSliderTexts(petipaikkojenMaaraText, petipaikatSlider, "");
		setSliderTexts(laakareidenMaaraText, laakaritSlider, "");
		setSliderTexts(salienMaaraText, salitSlider, "");
	}

	/**
	 * Asettaa maxjonon sliderien päällä olevat tekstit reaaliajassa.
	 */
	@Override
	public void setMaxJonoTexts() {
		setSliderTexts(laakaritJonoText, laakarinMaxJonoSlider, "");
		setSliderTexts(leikkausosastotJonoText, leikkausosastonMaxJonoSlider, "");
	}

	/**
	 * Asettaa tekstit sliderien päällä oleviin teksteihin. Kutsutaan
	 * setSaapumisvaliText(), setHuoneidenMaaraTexts(), setMaxJonoTexts(),
	 * setAluksiTexts().
	 * 
	 * @param t Tekstikenttä, johon päivitettävä tieto tulee
	 * @param s Slider, käytetään arvon hakemiseen
	 * @param e String, kuten % tekstikentän loppuun
	 */
	public void setSliderTexts(Text t, Slider s, String e) {
		s.valueProperty().addListener((observable, o, n) -> {
			t.setText(String.valueOf((int) s.getValue()) + e);
		});
	}

	/**
	 * Päivittää ajolistaan uuden ajokerran.
	 */
	public void naytaAjokerrat() {
		Platform.runLater(() -> {
			ajolista.getItems().add("Ajokerta " + Kello.getInstance().getAjokerta());
		});
	}

	/**
	 * Piirtää graafin kuolleiden asiakkaiden määrästä eri ajokerroilla
	 */
	public void piirraGraafi() {
		lineChart.setVisible(true);
		Series<String, Number> series = new XYChart.Series<String, Number>();
		// Estää x-axiksen nimien stackkaantymisen 0 kohtaan (en tiedä miksi)
		lineChart.setAnimated(false);
		lineChart.getData().clear();
		lineChart.getData().add(series);
	}

	/**
	 * GUI:n radiobuttonilla valitun mukainen palveluasetus joko tärkeys tai
	 * saapumisjärjestys. Kutsutaan omaMoottorin luoSairaalassa.
	 */
	public boolean getPalvelujarjestys() {
		return rbTarkeys.isSelected();
	}
}