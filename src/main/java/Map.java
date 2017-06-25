import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.SublayerList;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Map extends Application {

  private MapView mapView;
  private FeatureLayer featureLayer;
  private ServiceFeatureTable featureTable;
  private Point startPoint;
  private ListenableFuture<FeatureQueryResult> tableQueryResult;
  private String searchTerm;
  private Label searchLabel;
  private StackPane stackPane;
  private Label error;
  private VBox vBoxControl;
  private String searchText;
  private ArcGISMap map;
  private HBox searchBox;
  private SimpleFillSymbol fillSymbol;
  private ArcGISMapImageLayer imageLayer;
  private static Feature selected;
  private static Feature selected2;

  private static ServiceFeatureTable featureTableMovement;
  private static FeatureLayer feautureLayerMovement;
  private ComboBox<String> comboBox;
  private ComboBox<String> comboBox2;
  private static ServiceFeatureTable militaryTable;
  private static FeatureLayer militaryLayer;
  private static ServiceFeatureTable militaryTable2;
  private static FeatureLayer militaryLayer2;

  private static  String SERVICE_FEATURE_URL="https://sampleserver6.arcgisonline.com/arcgis/rest/services/SampleWorldCities/MapServer/0";
  private static final String MAP_URL =
      "https://sampleserver6.arcgisonline.com/arcgis/rest/services/SampleWorldCities/MapServer";
  private static final String movement_URL = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/CommercialDamageAssessment/FeatureServer/0";
  private static final String military_URL = "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Military/FeatureServer/2";
  private static final String military_URL2 = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Military/FeatureServer/3";
  
  private static final int SCALE = 10000000;
  
  private void setLayer() {
      if(map.getOperationalLayers().contains(featureLayer)) {
    	  map.getOperationalLayers().remove(featureLayer);
      }
      featureTable = new ServiceFeatureTable(SERVICE_FEATURE_URL);

      featureLayer = new FeatureLayer(featureTable);
      featureLayer.setOpacity(0.8f);
      
      featureLayer.addDoneLoadingListener(() -> searchBox.setDisable(false));

      featureLayer.setRenderer(new SimpleRenderer(fillSymbol));
      map.getOperationalLayers().add(featureLayer);
  }
    
  private MenuButton startOptions() {
      MenuItem menuItem1 = new MenuItem("Search for capitals");
      MenuItem menuItem2 = new MenuItem("Search for continent");
      MenuItem menuItem3 = new MenuItem("Search for world");
    
      menuItem1.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	        searchTerm = "CITY_NAME";
    	        SERVICE_FEATURE_URL = "https://sampleserver6.arcgisonline.com/arcgis/rest/services/SampleWorldCities/MapServer/0";
    	        setLayer();
    	        searchLabel.setText(menuItem1.getText());
    	        stackPane.requestLayout();
    	    }
    	});
      menuItem2.setOnAction(new EventHandler<ActionEvent>() {
  	    @Override
  	    public void handle(ActionEvent event) {
  	        searchTerm = "CONTINENT";
	        SERVICE_FEATURE_URL = "https://sampleserver6.arcgisonline.com/arcgis/rest/services/SampleWorldCities/MapServer/1";
	        setLayer();
	        searchLabel.setText(menuItem2.getText());
	        stackPane.requestLayout();
  	    }
      });
      menuItem3.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	        searchTerm = "WRLD30_ID";
    	        SERVICE_FEATURE_URL = "https://sampleserver6.arcgisonline.com/arcgis/rest/services/SampleWorldCities/MapServer/2";
    	        setLayer();
    	        searchLabel.setText(menuItem3.getText()); 
    	        stackPane.requestLayout();
    	    }
      });

      MenuButton menuButton = new MenuButton("Options",null, menuItem1, menuItem2, menuItem3);
      return menuButton;
  }
  
  private void setEventHadlers(){
	  featureTableMovement = new ServiceFeatureTable(movement_URL);

	  feautureLayerMovement = new FeatureLayer(featureTableMovement);
      
      map.getOperationalLayers().add(feautureLayerMovement);

      mapView.setOnMouseClicked(event -> {
    	identify(feautureLayerMovement,event);
        if (event.isStillSincePress() && event.getButton() == MouseButton.PRIMARY && !militaryLayer.isVisible() && !militaryLayer2.isVisible()) {
          Point2D point = new Point2D(event.getX(), event.getY());
          Point mapPoint = mapView.screenToLocation(point);
          
          ListenableFuture<IdentifyLayerResult> results = mapView.identifyLayerAsync(feautureLayerMovement, point, 1, false);
          results.addDoneListener(() -> {
            try {
              List<GeoElement> elements = null;
			try {
				elements = results.get().getElements();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
              if (elements.size() > 0 && elements.get(0) instanceof Feature) {
                selected = (Feature) elements.get(0);

                feautureLayerMovement.clearSelection();
                feautureLayerMovement.selectFeature(selected);
              } else {
                moveSelected(mapPoint);
                feautureLayerMovement.clearSelection();
              }
            } catch (ExecutionException e) {
            	e.getStackTrace();
            }
          });
        } else if (militaryLayer.isVisible() && !militaryLayer2.isVisible()) {
        	setEventHandlerMilitary(event);
        }
        else if (militaryLayer2.isVisible()){
        	setEventHandlerMilitary2(event);
        }
      });
  }
  
  public static void applyEdits() {

	  ListenableFuture<List<FeatureEditResult>> editResult = featureTableMovement.applyEditsAsync();
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
  
  private void moveSelected(Point newPoint) {
	Stream.of(selected).map(f -> (ArcGISFeature) f).filter(ArcGISFeature::canUpdateGeometry).forEach(f -> {
	f.setGeometry(newPoint);
	
	ListenableFuture<Void> featureTableResult = featureTableMovement.updateFeatureAsync(f);
		featureTableResult.addDoneListener(Map::applyEdits);
	});
  }
  
  private void setEventHandlerMilitary(MouseEvent event) {
	          Point2D point = new Point2D(event.getX(), event.getY());

	          militaryLayer.clearSelection();
	          comboBox.setDisable(true);

	          ListenableFuture<IdentifyLayerResult> results = mapView.identifyLayerAsync(militaryLayer, point, 1, false);
	          results.addDoneListener(() -> {
	            try {
	              IdentifyLayerResult layer = results.get();
	              List<GeoElement> identified = layer.getElements();
	              if (identified.size() > 0) {
	                GeoElement element = identified.get(0);
	                if (element instanceof ArcGISFeature) {
	                  selected = (ArcGISFeature) element;
	                  militaryLayer.selectFeature(selected);
	                  ((ArcGISFeature) selected).loadAsync();
	                  ((ArcGISFeature) selected).addDoneLoadingListener(() -> EditAttributes.selectAttribute((ArcGISFeature) selected,militaryLayer,comboBox,comboBox2));
	                  comboBox.setDisable(false);
	                }
	              }
	            } catch (InterruptedException | ExecutionException e) {
	            	e.getStackTrace();
	            }
	          });
  }
  
  private void identify(FeatureLayer layerToIdentify,MouseEvent event) {
      Point2D point = new Point2D(event.getX(), event.getY());

      layerToIdentify.clearSelection();

      ListenableFuture<IdentifyLayerResult> results = mapView.identifyLayerAsync(layerToIdentify, point, 1, false);
      results.addDoneListener(() -> {
        try {
          IdentifyLayerResult layer = results.get();
          List<GeoElement> identified = layer.getElements();
          if (identified.size() > 0) {
            GeoElement element = identified.get(0);
                        
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            VBox dialogVbox = new VBox(20);
            if(element.getAttributes().get("contloss") != null){
            	dialogVbox.getChildren().add(new Text("Count loss: " + element.getAttributes().get("contloss").toString()));         
            }
            if(element.getAttributes().get("strloss") != null) {
            	dialogVbox.getChildren().add(new Text("Str loss: " + element.getAttributes().get("strloss").toString()));         
            }
            if(element.getAttributes().get("typdamage") != null){
            	dialogVbox.getChildren().add(new Text("Type damage: " + element.getAttributes().get("typdamage").toString()));         
            }
            Scene dialogScene = new Scene(dialogVbox, 300, 200);
            dialog.setScene(dialogScene);
            dialog.show();
          }
        } catch (InterruptedException | ExecutionException e) {
        	e.getStackTrace();
        }
      });
  }
  
  private void setEventHandlerMilitary2(MouseEvent event) {
      Point2D point = new Point2D(event.getX(), event.getY());

      militaryLayer2.clearSelection();
      comboBox2.setDisable(true);

      ListenableFuture<IdentifyLayerResult> results = mapView.identifyLayerAsync(militaryLayer2, point, 1, false);
      results.addDoneListener(() -> {
        try {
          IdentifyLayerResult layer = results.get();
          List<GeoElement> identified = layer.getElements();
          if (identified.size() > 0) {
            GeoElement element = identified.get(0);
               
            if (element instanceof ArcGISFeature) {
              selected2 = (ArcGISFeature) element;
              militaryLayer2.selectFeature(selected2);
              ((ArcGISFeature) selected2).loadAsync();
              ((ArcGISFeature) selected2).addDoneLoadingListener(() -> EditAttributes.selectAttribute((ArcGISFeature) selected2,militaryLayer,comboBox,comboBox2));;
              comboBox2.setDisable(false);
            }
          }
        } catch (InterruptedException | ExecutionException e) {
        	e.getStackTrace();
        }
      });
}

@Override
  public void start(Stage stage) throws Exception {

    try {
      stackPane = new StackPane();
      Scene scene = new Scene(stackPane);

      stage.setTitle("Klara's project");
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setScene(scene);
      stage.show();

      map = new ArcGISMap(Basemap.createTopographic());
     
      imageLayer = new ArcGISMapImageLayer(MAP_URL);
      //imageLayer = new ArcGISTiledLayer(MAP_URL);
      imageLayer.setOpacity(1);

      militaryTable = new ServiceFeatureTable(military_URL);
      militaryTable2 = new ServiceFeatureTable(military_URL2);

      militaryLayer = new FeatureLayer(militaryTable);
      militaryLayer.setOpacity(1);
      
      militaryLayer2 = new FeatureLayer(militaryTable2);
      militaryLayer.setOpacity(1);
      
      map.getOperationalLayers().add(militaryLayer);
      map.getOperationalLayers().add(militaryLayer2);

      map.getOperationalLayers().add(imageLayer);

      vBoxControl = new VBox(6);
      vBoxControl.setMaxSize(250, 80);

      searchLabel = new Label("Search:");
      TextField searchField = new TextField();
      searchField.setMaxWidth(150);
      
      Button searchButton = new Button("Search");
      searchBox = new HBox(5);
      searchBox.getChildren().addAll(searchField, searchButton);
      searchBox.setDisable(true);

      searchButton.setOnAction(e -> {
        featureLayer.clearSelection();
        searchText = searchField.getText();

        if (searchText.trim().length() > 0) {
          search(searchText,searchTerm);
        } else {
        	error.setText(searchText + " Not found");
        	vBoxControl.getChildren().add(error);
        	stackPane.requestLayout();
        	mapView.setViewpointCenterAsync(startPoint, SCALE);
        }
      });

      vBoxControl.getChildren().addAll(startOptions(),searchLabel, searchBox);

      startPoint = new Point(-11000000, 5000000, SpatialReferences.getWebMercator());

      SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF000000, 1);
      fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFFFCC00, lineSymbol);

      StackPane.setAlignment(vBoxControl, Pos.TOP_LEFT);
      StackPane.setMargin(vBoxControl, new Insets(10, 0, 0, 10));
      
      setLayer();
      EditAttributes.startEditAttributes(comboBox,comboBox2,militaryLayer,selected,militaryTable,militaryTable2,selected2,vBoxControl);
      
      CheckBox citiesBox = new CheckBox("Cities");
      CheckBox continentsBox = new CheckBox("Continents");
      CheckBox worldBox = new CheckBox("World");
      CheckBox enableMovement = new CheckBox("Enable layer for editing");
      CheckBox enableEditingAttributes = new CheckBox("Enable layer for editing attr");
      CheckBox enableEditingAttributes2 = new CheckBox("Enable layer for editing attr2");

      vBoxControl.getChildren().addAll(citiesBox, continentsBox, worldBox, enableMovement,enableEditingAttributes,enableEditingAttributes2);

      vBoxControl.getChildren().forEach(c -> {
    	  try{
			((CheckBox) c).setSelected(true);
    	  }
    	  catch(Exception e){}
      });

      SublayerList layers = imageLayer.getSublayers();

      citiesBox.selectedProperty().addListener(e -> layers.get(0).setVisible(citiesBox.isSelected()));
      continentsBox.selectedProperty().addListener(e -> layers.get(1).setVisible(continentsBox.isSelected()));
      worldBox.selectedProperty().addListener(e -> layers.get(2).setVisible(worldBox.isSelected()));
      
  		enableMovement.selectedProperty().addListener(e -> feautureLayerMovement.setVisible(enableMovement.isSelected()));
  		enableEditingAttributes.selectedProperty().addListener(e -> militaryLayer.setVisible(enableEditingAttributes.isSelected()));
  		enableEditingAttributes.selectedProperty().addListener(e -> militaryLayer2.setVisible(enableEditingAttributes2.isSelected()));

      EventHandler<ActionEvent> eh = new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	        if (event.getSource() instanceof CheckBox && enableMovement.isSelected()) {
    	        	feautureLayerMovement.setVisible(true);
    	        	feautureLayerMovement.setOpacity(1);
    	        }
    	        else {
    	        	feautureLayerMovement.setVisible(false);
    	        }
    	    }
    	};
    	
        EventHandler<ActionEvent> eh2 = new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent event) {
    	        if (event.getSource() instanceof CheckBox && enableEditingAttributes.isSelected()) {
    	        	militaryLayer.setVisible(true);
    	            militaryLayer.setOpacity(1);
    	        }
    	        else {
    	        	militaryLayer.setVisible(false);
    	        }
    	    }
    	};
    	
    	  EventHandler<ActionEvent> eh3 = new EventHandler<ActionEvent>() {
      	    @Override
      	    public void handle(ActionEvent event) {
      	        if (event.getSource() instanceof CheckBox && enableEditingAttributes2.isSelected()) {
      	        	militaryLayer2.setVisible(true);
      	            militaryLayer2.setOpacity(1);
      	        }
      	        else {
      	        	militaryLayer2.setVisible(false);
      	        }
      	    }
      	};

      enableMovement.setOnAction(eh);
      enableEditingAttributes.setOnAction(eh2);
      enableEditingAttributes2.setOnAction(eh3);
           
      mapView = new MapView();
      
      setEventHadlers();

      mapView.setMap(map);
      mapView.setViewpointCenterAsync(startPoint, SCALE);
      stackPane.getChildren().addAll(mapView, vBoxControl);

      StackPane.setAlignment(vBoxControl, Pos.TOP_LEFT);
      StackPane.setMargin(vBoxControl, new Insets(10, 0, 0, 10));
            
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  // typeOfQuery = name, etc.
  // query - the actual query
  private void search(String queryString,String typeOfQuery) {      
    QueryParameters query = new QueryParameters();
    query.setWhereClause("upper("+typeOfQuery+") LIKE '" + queryString.toUpperCase() + "'");

    tableQueryResult = featureTable.queryFeaturesAsync(query);

    tableQueryResult.addDoneListener(() -> {
      try {
        FeatureQueryResult result = tableQueryResult.get();
        if (result.iterator().hasNext()) {
          Feature feature = result.iterator().next();
          Envelope envelope = feature.getGeometry().getExtent();
          mapView.setViewpointGeometryAsync(envelope);
          featureLayer.selectFeature(feature);
        } else {
          Platform.runLater(() -> {
         	error = new Label(searchText + " Not found");
         	vBoxControl.getChildren().add(error);
         	stackPane.requestLayout();
            mapView.setViewpointCenterAsync(startPoint, SCALE);
          });
        }
      } catch (Exception e) {
      	error.setText(searchText + " Not found");
      	vBoxControl.getChildren().add(error);
      	stackPane.requestLayout();
        e.printStackTrace();
      }
    });
  }
 

  @Override
  public void stop() throws Exception {

    if (mapView != null) {
      mapView.dispose();
    }
  }

  public static void main(String[] args) {

    Application.launch(args);
  }

}