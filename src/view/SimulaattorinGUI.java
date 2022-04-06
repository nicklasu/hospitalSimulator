package src.view;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import src.controller.IKontrolleri;
import src.controller.Kontrolleri;
import src.simu.framework.Trace;
import src.simu.framework.Trace.Level;
import javafx.scene.*;

/**
 * Luo GUI:n SimulaattorinGUI.fxml-tiedoston pohjalta ja asettaa sille
 * kontrollerin
 * 
 * @author Nicklas Sundell
 */
public class SimulaattorinGUI extends Application {

	// Kontrollerin esittely (tarvitaan käyttöliittymässä)
	private IKontrolleri kontrolleri;

	/**
	 * Initialisointi, luo kontrollerin
	 */
	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		kontrolleri = new Kontrolleri();
	}

	/**
	 * Luo GUI:n ja näyttää sen stagella
	 */
	@Override
	public void start(Stage primaryStage) {

		// Käyttöliittymän rakentaminen
		try {
			primaryStage.setTitle("Simulaattori");

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("SimulaattorinGUI.fxml"));
			Parent root = loader.load();

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					Platform.exit();
					System.exit(0);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Käynnistää ohjelman
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
