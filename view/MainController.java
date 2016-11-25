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
	
	@FXML
	private TextArea decrypted;
	
	@FXML
	private TextArea header;

	@FXML
	private TextArea body;
	
	@FXML
	private Button refresh;
	
	@FXML
	private void handleButtonAction(ActionEvent event) {
		if(!workQueue.isEmpty()) {
			AESUtility cipher = new AESUtility();
			decrypted.setText(cipher.Decrypt("Balwisyall_Nescell_gungnir_tron!", "thisisjusteasyIV", workQueue.remove(), workQueue.remove().toCharArray()));
			header.setText(workQueue.remove());
			body.setText(workQueue.remove());
		}
	}
	
	public MainController() {
		sender.start();
	}
	
	public void stop() {
		sender.requestStop();
	}
}