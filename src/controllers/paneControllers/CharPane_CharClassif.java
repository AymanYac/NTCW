package controllers.paneControllers;

import controllers.Char_description;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import model.*;
import service.CharValuesLoader;
import service.TableViewExtra;
import transversal.dialog_toolbox.CaracEditionDialog;
import transversal.generic.Tools;
import transversal.language_toolbox.WordUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

import static transversal.data_exchange_toolbox.CharDescriptionExportServices.addCaracDefinitionToPush;
import static transversal.data_exchange_toolbox.CharDescriptionExportServices.flushCaracDefinitionToDB;

public class CharPane_CharClassif {

	public Char_description parent;
	private CharDescriptionRow selected_row;
	@FXML public TableView<CharPaneRow> tableGrid;
	@FXML private TableColumn<?, ?> critcalityColumn;
	@FXML private TableColumn<?, ?> seqColumn;
	@FXML private TableColumn<String, CharPaneRow> charNameColumn;
	@FXML private TableColumn<?, ?> uomColumn;
	@FXML private TableColumn<CharPaneRow, String> valueColumn;
	
	private ArrayList<CharPaneRow> paneRows = new ArrayList<CharPaneRow>();
	private boolean triggerItemTableRefresh;
	private TableViewExtra tvx;

	public void load_item_chars() {
		this.paneRows.clear();
		try {
			this.selected_row = (CharDescriptionRow) parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem();
		}catch(Exception V) {
			
		}
		if(this.selected_row!=null) {
			CharValuesLoader.active_characteristics.get(selected_row.getClass_segment_string().split("&&&")[0]).forEach(carac->{
				CharPaneRow tmp = new CharPaneRow(this.parent);
				tmp.setItem(selected_row);
				tmp.setChar_index(CharValuesLoader.active_characteristics.get(selected_row.getClass_segment_string().split("&&&")[0]).indexOf(carac));
				tmp.setCarac(carac);
				tmp.setValue(selected_row.getData(selected_row.getClass_segment_string().split("&&&")[0]).get(carac.getCharacteristic_id()));
				this.paneRows.add(tmp);
			});
		}
		this.tableGrid.getItems().clear();
		//tvx.rows.clear();
		this.paneRows.sort(new Comparator<CharPaneRow>() {
			@Override
			public int compare(CharPaneRow o1, CharPaneRow o2) {
				return Integer.compare(o1.getChar_sequence(),o2.getChar_sequence());
			}
		});
		this.tableGrid.getItems().addAll( this.paneRows);
		try {
			triggerItemTableRefresh = false;
			this.tableGrid.getSelectionModel().select(Math.floorMod(this.parent.tableController.selected_col,CharValuesLoader.active_characteristics.get(selected_row.getClass_segment_string().split("&&&")[0]).size()));
			tvx.scrollToSelection();
			triggerItemTableRefresh = true;
		}catch(Exception V) {
			triggerItemTableRefresh = true;
		}

		
	}

	@SuppressWarnings("unchecked")
	public void setParent(Char_description char_description) {
		this.parent = char_description;
		this.tableGrid.getItems().addAll( this.paneRows);
		this.tvx = new TableViewExtra(this.tableGrid);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final ScrollBar scrollBarH = (ScrollBar) tableGrid.lookup(".scroll-bar:hotizontal");
				scrollBarH.setVisible(false);
			}
		});
		critcalityColumn.setCellValueFactory(new PropertyValueFactory<>("Criticality"));
		charNameColumn.setCellValueFactory(new PropertyValueFactory<String, CharPaneRow>("Char_name_complete"));
		//charNameColumn.setCellFactory(model.mouseHoverTableCell.forCharNameTranslation(this.tableGrid));
		charNameColumn.setCellFactory(inputEventTableCell.forCharEdition(this.tableGrid,char_description));


		seqColumn.setCellValueFactory(new PropertyValueFactory<>("Char_sequence"));
		uomColumn.setCellValueFactory(new PropertyValueFactory<>("Uom_display"));
		
		//valueColumn.setCellValueFactory(new PropertyValueFactory<>("Value_display"));
		valueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CharPaneRow, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(TableColumn.CellDataFeatures<CharPaneRow, String> r) {
				try{
					CaracteristicValue val = r.getValue().getValue();
					String dsp = null;
					try{
						dsp = r.getValue().getCarac().getIsCritical()? val.getDisplayValue(false,false): WordUtils.textFlowToString(r.getValue().getValue_display());
					}catch (Exception V){

					}
					if(dsp!=null && dsp.length()>0){
						return new ReadOnlyObjectWrapper(dsp);
					}else if (parent.tableController.charDescriptionTable.getSelectionModel().getSelectedItem().getRulePropositions(r.getValue().getCarac().getCharacteristic_id()).size()>0){
						return new ReadOnlyObjectWrapper<>("*PENDING*");
					}
					return null;
				}catch(Exception V) {
					//Object has null data at daataIndex
					return null;
				}
			}
		});
		//valueColumn.setCellFactory(mouseHoverTableCell.forTableColumn());
		
		
		seqColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		charNameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		uomColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
		valueColumn.setStyle( "-fx-alignment: CENTER-LEFT;");

		tableGrid.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null && this.triggerItemTableRefresh) {
				Platform.runLater(new Runnable (){

					@Override
					public void run() {
						parent.tableController.selected_col=((CharPaneRow) newSelection).getChar_index()-1;
						parent.tableController.nextChar();
					}

				});

			}
		    
		});
		tableGrid.setRowFactory(tv -> {
			TableRow<CharPaneRow> row = new TableRow<>();
			tvx.rows.add(row);
			row.setOnDragDetected(event -> {
				System.out.println("start drag detected");
				if (! row.isEmpty()) {
					Integer index = row.getIndex();
					Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
					db.setDragView(row.snapshot(null, null));
					ClipboardContent cc = new ClipboardContent();
					cc.put(GlobalConstants.SERIALIZED_MIME_TYPE, index);
					db.setContent(cc);
					event.consume();
				}
				System.out.println("end drag detected");
			});

			row.setOnDragOver(event -> {
				Dragboard db = event.getDragboard();
				if (db.hasContent(GlobalConstants.SERIALIZED_MIME_TYPE)) {
					if (row.getIndex() != ((Integer)db.getContent(GlobalConstants.SERIALIZED_MIME_TYPE)).intValue()) {
						event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
						event.consume();
					}
				}
			});

			row.setOnDragDropped(event -> {
				System.out.println("start drag drop");
				Dragboard db = event.getDragboard();
				if (db.hasContent(GlobalConstants.SERIALIZED_MIME_TYPE)) {
					int draggedIndex = (Integer) db.getContent(GlobalConstants.SERIALIZED_MIME_TYPE);
					CharPaneRow draggedRow = tableGrid.getItems().remove(draggedIndex);

					int dropIndex ;

					if (row.isEmpty()) {
						dropIndex = tableGrid.getItems().size() ;
					} else {
						dropIndex = row.getIndex();
					}

					tableGrid.getItems().add(dropIndex, draggedRow);
					for(int i=0;i<tableGrid.getItems().size();i++){
						tableGrid.getItems().get(i).getCarac().setSequence(i+1);
						tableGrid.getItems().get(i).setChar_index(i);
						try {
							addCaracDefinitionToPush(tableGrid.getItems().get(i).getCarac(),Tools.get_project_segments(parent.account).get(selected_row.getClass_segment_string().split("&&&")[0]));
						} catch (SQLException throwables) {
							throwables.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					CharValuesLoader.active_characteristics.get(selected_row.getClass_segment_string().split("&&&")[0]).sort(new Comparator<ClassCaracteristic>() {
						@Override
						public int compare(ClassCaracteristic o1, ClassCaracteristic o2) {
							return o1.getSequence().compareTo(o2.getSequence());
						}
					});
					event.setDropCompleted(true);
					tableGrid.getSelectionModel().select(dropIndex);
					event.consume();
				}
				System.out.println("end drag drop");
				try {
					flushCaracDefinitionToDB(parent.account);
				} catch (SQLException | ClassNotFoundException throwables) {
					throwables.printStackTrace();
				}
			});

			return row ;
		});
		
	}

	public void PaneClose() {
		parent.charButton.setSelected(false);
		parent.setBottomRegionColumnSpans(false);
	}
	
	@FXML public void add_carac() throws SQLException, ClassNotFoundException {
		CaracEditionDialog.CaracDeclarationPopUp(parent.account, Tools.get_project_segments(parent.account).get(selected_row.getClass_segment_string().split("&&&")[0]), null,parent,null);
		parent.tableController.refresh_table_preserve_sort_order();
	}

}
