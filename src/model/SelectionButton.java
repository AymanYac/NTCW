package model;

import javafx.scene.control.RadioButton;

public class SelectionButton extends RadioButton {
	private String pid;

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public SelectionButton(String pid){
		super();
		this.pid = pid;
	}

	
	
}
