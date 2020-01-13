package controllers.paneControllers;
import java.util.ArrayList;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CharDescriptionRow;
import model.CharPaneRow;
import model.ClassCharacteristic;
import model.mouseHoverTableCell;

public class CharPane_CharClassif {

	public Char_description parent;
	private CharDescriptionRow selected_row;
	@FXML public TableView<CharPaneRow> tableGrid;
	@FXML private TableColumn<?, ?> critcalityColumn;
	@FXML private TableColumn<?, ?> seqColumn;
	@FXML private TableColumn<?, ?> charNameColumn;
	@FXML private TableColumn<?, ?> uomColumn;
	@FXML private TableColumn<?, ?> valueColumn;
	
	private ArrayList<CharPaneRow> paneRows = new ArrayList<CharPaneRow>();
	private boolean triggerItemTableRefresh;
	
	public void load_item_chars() {
		this.paneRows.clear();
		try {
			this.selected_row = (CharDescriptionRow) parent.tableController.tableGrid.getSelectionModel().getSelectedItem();
		}catch(Exception V) {
			
		}
		if(this.selected_row!=null) {
			for(int i=0;i<this.parent.tableController.active_characteristics.get(selected_row.getClass_segment().split("&&&")[0]).size();i++) {
				ClassCharacteristic carac = this.parent.tableController.active_characteristics.get(selected_row.getClass_segment().split("&&&")[0]).get(i);
				CharPaneRow tmp = new CharPaneRow(this.parent);
				tmp.setChar_index(i);
				tmp.setCarac(carac);
				tmp.setValue(selected_row.getData(selected_row.getClass_segment().split("&&&")[0])[i]);
				this.paneRows.add(tmp);
			}
		}
		this.tableGrid.getItems().clear();
		this.tableGrid.getItems().addAll( this.paneRows);
		
		try {
			triggerItemTableRefresh = false;
			this.tableGrid.getSelectionModel().select(Math.floorMod(this.parent.tableController.selected_col,this.parent.tableController.active_characteristics.get(selected_row.getClass_segment().split("&&&")[0]).size()));
			triggerItemTableRefresh = true;
		}catch(Exception V) {
			
		}
		
		
	}

	@SuppressWarnings("unchecked")
	public void setParent(Char_description char_description) {
		this.parent = char_description;
		this.tableGrid.getItems().addAll( this.paneRows);
		critcalityColumn.setCellValueFactory(new PropertyValueFactory<>("Criticality"));
		charNameColumn.setCellValueFactory(new PropertyValueFactory<>("Char_name"));
		charNameColumn.setCellFactory(mouseHoverTableCell.forCharNameTranslation(this.tableGrid));
		
		seqColumn.setCellValueFactory(new PropertyValueFactory<>("Char_sequence"));
		uomColumn.setCellValueFactory(new PropertyValueFactory<>("Uom_display"));
		
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("Value_display"));
		//valueColumn.setCellFactory(mouseHoverTableCell.forTableColumn());
		
		
		seqColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		charNameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		uomColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		valueColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		
		tableGrid.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if (newSelection != null && this.triggerItemTableRefresh && selected_row.getClass_segment().split("&&&")[0].equals(parent.classCombo.getValue().getClassSegment())) {
		    	Platform.runLater(new Runnable (){

					@Override
					public void run() {

				    	parent.tableController.selected_col=((CharPaneRow) newSelection).getChar_index()-1;
				    	parent.tableController.nextChar();
					}
						    		
		    	});
		    	
		    }else {
		    	/*
		    	triggerItemTableRefresh = false;
				this.tableGrid.getSelectionModel().select(Math.floorMod(this.parent.tableController.selected_col,this.parent.tableController.active_characteristics.size()));
				triggerItemTableRefresh = true;*/
		    }
		    
		});
		
	}

	public void PaneClose() {
		parent.charButton.setSelected(false);
		parent.setBottomRegionColumnSpans(false);
	}

}
