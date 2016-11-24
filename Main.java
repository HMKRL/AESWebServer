import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import view.MainController;

public class Main extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	private FXMLLoader loader;
	private MainController controller;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("FX Practice");
		
		initRootLayout();
	}
	
	@Override
	public void stop() {
		controller.stop();
	}
	
	public void initRootLayout() {
		try {
			loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/MainView.fxml"));
			rootLayout = (AnchorPane)loader.load();
			controller = loader.getController();
		
			Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(Main.class.getResource("resources/css/MainView.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
