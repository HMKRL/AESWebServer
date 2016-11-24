package view;

import java.util.concurrent.BlockingQueue;
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
	private Button bt;
	
	@FXML
	private TextArea toSend;
	
	@FXML
	private void handleButtonAction(ActionEvent event) {
	    workQueue.add(toSend.getText());
	    toSend.clear();
	}
}