import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class EditAttributes {
	  
	  	  public static void selectAttribute(ArcGISFeature feature, FeatureLayer layer,ComboBox<String> comboBox,
	  			  ComboBox<String> secondComboBox) {
	  		ComboBox currentbox;
			 if(layer.isVisible()) {
				 currentbox = comboBox;
			 }
			 else {
				 currentbox = secondComboBox;
			 }
		    Platform.runLater(() -> currentbox.getSelectionModel().select((String) feature.getAttributes().get("symbolname")));
		  }

		  public static void updateAttributes(ArcGISFeature selected,FeatureLayer layer,
				  ServiceFeatureTable table,ServiceFeatureTable table2,ComboBox<String> comboBox,ComboBox<String> secondComboBox) {
			 ServiceFeatureTable current;
			 ComboBox currentbox;
			 if(layer.isVisible()) {
				 current = table;
				 currentbox = comboBox;
			 }
			 else {
				 current = table2;
				 currentbox = secondComboBox;
			 }
		    if (current.canUpdate(selected)) {
		    	selected.getAttributes().put("symbolname", currentbox.getValue());

		      ListenableFuture<Void> editResult = current.updateFeatureAsync(selected);
		      editResult.addDoneListener(() -> applyEditsAttributes(current));
		    } else {
		    	System.out.println("error");
		    }
		  }

		  public static void applyEditsAttributes(ServiceFeatureTable table) {
		    ListenableFuture<List<FeatureEditResult>> editResult = table.applyEditsAsync();
		    editResult.addDoneListener(() -> {
		      try {
		        List<FeatureEditResult> edits = editResult.get();
		        if (edits != null && edits.size() > 0 && edits.get(0).hasCompletedWithErrors()) {
		          throw edits.get(0).getError();
		        }
		      } catch (InterruptedException | ExecutionException e) {
		    	  e.getStackTrace();
		      }
		    });
		  }
	  
	  public static void startEditAttributes(ComboBox<String> comboBox,ComboBox<String> comboBox2,
			  FeatureLayer militaryLayer,Feature selected,ServiceFeatureTable militaryTable,ServiceFeatureTable militaryTable2,
			  Feature selected2, VBox vBoxControl) {
		    Label Label1 = new Label("Type:");

		      ObservableList<String> types = FXCollections.observableArrayList();
		      types.add("Mechanized Infantry Company");
		      types.add("Combat Aviation");
		      types.add("Division Headquarters");
		      types.add("Field Artillery Combat Brigade");
		      types.add("Armored Cavalry Battalion");
		      types.add("Mechanized Brigade #1");
		      types.add("Mechanized Brigade #2");
		      types.add("Armor Brigade #1");
		      types.add("Armor-heavy Task Force");
		      types.add("Armored Cavalry Company");
		      types.add("TAC");

		      final ComboBox<String> comboBoxNew = new ComboBox<>(types);
		      comboBoxNew.setMaxWidth(Double.MAX_VALUE);
		      comboBoxNew.setTooltip(new Tooltip("Type of units"));
		      comboBoxNew.setDisable(true);

		      comboBoxNew.showingProperty().addListener((obs, wasShowing, isShowing) -> {
		        try {
		          updateAttributes((ArcGISFeature) selected, militaryLayer,militaryTable,militaryTable2,comboBoxNew,comboBox2);
		        } catch (Exception e) {
		        	e.getStackTrace();
		        }
		      });
		      
			    Label Label2 = new Label("Type:");

			      ObservableList<String> types2 = FXCollections.observableArrayList();
			      types2.addAll("Coordination Point","Decision Point","Contact Point","Passage Point",
			    		  "Start Point","Waypoint","Air Control","Air Control Point","Communications Checkpoint",
			    		  "MIW - Fixed Wing","Orbit","Orbit - Race Track","Replenish","Rescue","Strike IP",
			    		  "Tomcat","Tanking","Datum","Lost Contact","Search","Search Area","Rescue Control Point",
			    		  "Predicted Impact Point");

			      ComboBox<String> comboBoxNew2 = new ComboBox<>(types2);
			      comboBoxNew2.setMaxWidth(Double.MAX_VALUE);
			      comboBoxNew2.setTooltip(new Tooltip("Type of operation poins"));
			      comboBoxNew2.setDisable(true);

			      comboBoxNew2.showingProperty().addListener((obs, wasShowing, isShowing) -> {
			        try {
				          updateAttributes((ArcGISFeature) selected2, militaryLayer,militaryTable,militaryTable2,comboBoxNew,comboBoxNew2);
			        } catch (Exception e) {
			        	e.getStackTrace();
			        }
			      });

		      vBoxControl.getChildren().addAll(Label1,comboBoxNew,Label2,comboBoxNew2);
	  }
	
}
