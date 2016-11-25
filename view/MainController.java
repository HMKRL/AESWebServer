package view;

import java.util.concurrent.BlockingQueue;
import network.AESUtility;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import network.Server;

public class MainController {
	private BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>();
	private Server sender = new Server(8080, workQueue);
	
	public MainController() {
		sender.start();
	}
	
	public void stop() {
		sender.sendStopSignal();
	}
	
	@FXML
	private TextArea recieved;
	
	@FXML
	private Button refresh;
	
	@FXML
	private void handleButtonAction(ActionEvent event) {
		if(!workQueue.isEmpty()) {
			System.out.println();
			AESUtility cipher = new AESUtility();
			recieved.setText(cipher.Decrypt("Balwisyall_Nescell_gungnir_tron!", "thisisjusteasyIV", "ECB", workQueue.peek().toCharArray()));
			workQueue.remove();
		}
	}
}