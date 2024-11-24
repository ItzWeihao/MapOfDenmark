package bfst22.vector;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.util.Locale;

public class TextFieldSuggestion extends TextField {
	private ContextMenu popup;
	private TernarySearchTree tree;
	private MapCanvas canvas;
	private boolean gotoSuggestion;
	private TernarySearchTree.Address selectedAddress;

	public void init(final TernarySearchTree tree, final MapCanvas canvas, final boolean gotoSuggestion){
		this.popup = new ContextMenu();
		this.tree = tree;
		this.canvas = canvas;
		this.gotoSuggestion = gotoSuggestion;
		this.selectedAddress = null;
		super.textProperty().addListener((observable, oldValue, newValue) -> this.bindProperty(newValue));
	}

	private void bindProperty(final String newValue){
		if(newValue.isEmpty()) this.popup.hide();
		this.selectedAddress = null;
		this.popup.getItems().clear();
		this.tree.searchSuggestions(super.getText().toLowerCase(Locale.ROOT))
				.forEach(suggestion -> {
					MenuItem item = new MenuItem(suggestion.toString());
					item.setOnAction(action -> {
						if(this.gotoSuggestion){
							this.canvas.goToPosAbsolute(new float[]{suggestion.coordPos[0],suggestion.coordPos[1]});
							this.canvas.zoomTo(300000);
						}
						super.setText(suggestion.toString());
						this.selectedAddress = suggestion;
						this.popup.hide();
					});
					this.popup.getItems().add(item);
				});
		this.popup.show(this, Side.BOTTOM, 0, 0);
	}

	public TernarySearchTree.Address getSelectedAddress(){
		return this.selectedAddress;
	}

	public void setSelectedAddress(TernarySearchTree.Address address){
		super.setText(address == null ? "" : address.toString());
		this.selectedAddress = address;
		this.popup.hide();
	}
}
