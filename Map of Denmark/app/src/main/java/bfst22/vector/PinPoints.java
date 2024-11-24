package bfst22.vector;

import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class PinPoints extends Dialog<ButtonType> {
	private final List<Pin> pins;
	private ListView<HBox> pinPointList;

	@FXML private TextField PinTitle;
	@FXML private TextArea PinDescription;
	@FXML private CheckBox PinCheckbox;
	@FXML private ButtonType PinOKButton, PinCancelButton, PinDeleteButton;

	public PinPoints(){
		this.pins = new ArrayList<>();
		super.setDialogPane((DialogPane) Controller.smartFXMLLoader(this,"PinDialog.fxml"));
		this.PinTitle.textProperty().addListener(observable -> super.getDialogPane().lookupButton(this.PinOKButton).setDisable(this.PinTitle.getText().length() <= 0));
	}

	public void init(final ListView<HBox> pinPointList){
		this.pinPointList = pinPointList;
	}

	public void newWindow(final MapCanvas canvas){
		this.displayWindow("Add Pin Point", null, canvas);
	}

	private void displayWindow(final String header, final Pin point, final MapCanvas canvas){
		super.setTitle(header);
		this.PinTitle.clear();
		this.PinDescription.clear();
		this.PinCheckbox.setSelected(false);

		if(point != null) {
			this.PinTitle.setText(point.title);
			this.PinDescription.setText(point.description);
			this.PinCheckbox.setSelected(!point.isMovable());
		}

		super.getDialogPane().lookupButton(this.PinOKButton).setDisable(this.PinTitle.getText().isEmpty());
		super.getDialogPane().lookupButton(this.PinDeleteButton).setDisable(point == null);
		super.showAndWait().ifPresent(response -> {
			switch (response.getButtonData()){
				case YES -> { // CREATE, UPDATE
					if(point == null){
						HBox PinEntry 	 = (HBox) Controller.smartFXMLLoader(this,"PinListEntry.fxml");
						String labelText = this.PinTitle.getText().length() > 20 ? this.PinTitle.getText().substring(0,20) + "..." : this.PinTitle.getText();
						Pin newPoint 	 = new Pin(PinEntry, canvas.mousePos[0], canvas.mousePos[1], 30, !this.PinCheckbox.isSelected(), this.PinTitle.getText(), this.PinDescription.getText());

						Label PinLabel 		 = ((Label) PinEntry.lookup("#PinLabel"));
						Button PinButtonGoto = ((Button) PinEntry.lookup("#PinButtonGoto"));
						Button PinButtonEdit = ((Button) PinEntry.lookup("#PinButtonEdit"));

						PinLabel.setText(labelText);
						PinButtonGoto.setOnMousePressed(f -> canvas.goToPosAbsolute(new float[]{newPoint.lat,newPoint.lon}));
						PinButtonEdit.setOnMousePressed(g -> this.displayWindow("Edit Pin Point", newPoint, canvas));

						this.pinPointList.getItems().add(PinEntry);
						this.pins.add(newPoint);
					} else {
						((Label) point.listEntry.lookup("#PinLabel")).setText(this.PinTitle.getText().length() > 20 ? this.PinTitle.getText().substring(0,20) + "..." : this.PinTitle.getText());
						Pin curr = this.pins.get(this.pins.indexOf(point));
						curr.setContent(this.PinTitle.getText(),this.PinDescription.getText());
						curr.setMovableState(!this.PinCheckbox.isSelected());
					}
				} case NO -> { // DELETE
					this.pinPointList.getItems().remove(point.listEntry);
					this.pins.remove(point);
				}
			}
		});
	}

	public void doubleClick(MapCanvas canvas){
		for(Pin point : this.pins){
			if(point.inRadius(canvas.mousePos, canvas.zoom_current)){
				this.displayWindow("Edit Pin Point", point, canvas);
				return;
			}
		}
	}

	public boolean drag(float[] mousePos, double zoom, boolean state){
		return this.pins.stream().anyMatch(obj -> {
			boolean in = obj.inRadius(mousePos,zoom);
			if(in) obj.move(mousePos,state);
			return in;
		});
	}

	public void draw(GraphicsContext gc, double zoom, float[] mousePos){
		this.pins.forEach(point -> point.draw(gc,zoom,mousePos));
	}
}