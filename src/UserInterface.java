import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;


public class UserInterface extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    PropertyAssessmentDAO dao = null;
    String source = "n/a";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Test");
        BorderPane rootNode = new BorderPane();
        Scene scene = new Scene(rootNode, 1100, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        //create VBox for left side menu
        VBox menu = new VBox();
        menu.setSpacing(5.0);
        menu.setPadding(new Insets(5,5,5,5));
        menu.setPrefWidth(220);
        //REF: https://www.tabnine.com/code/java/methods/javafx.scene.layout.Pane/setBorder
        menu.setBorder(new Border(new BorderStroke(Color.DIMGREY, BorderStrokeStyle.SOLID, null, null)));
        //menu.setStyle("-fx-background-color: #ffd68a;");

        Separator separator = new Separator();

        // Labels
        Font roboto = Font.font("Roboto", FontWeight.BOLD, 16);
        Font smallboto = new Font("Roboto", 14);
        Label sourceLabel = new Label("Select Data Source");
        sourceLabel.setFont(roboto);
        Label paLabel = new Label("Find Property Assessment");
        paLabel.setFont(roboto);
        Label accNoLabel = new Label("Account Number:");
        accNoLabel.setFont(smallboto);
        Label addressLabel = new Label("Address:");
        addressLabel.setFont(smallboto);
        Label neighbourhoodLabel = new Label("Neighbourhood:");
        neighbourhoodLabel.setFont(smallboto);
        Label assessClassLabel = new Label("Assessment Class:");
        assessClassLabel.setFont(smallboto);
        Label assessValueLabel = new Label("Assessed Value Range:");
        assessValueLabel.setFont(smallboto);

        // Create combo boxes
        String[] sources = { "CSV File", "Edmonton's Open Data Portal" };
        ComboBox<String> sourceSelectBox = new ComboBox<>(FXCollections.observableArrayList(sources));
        sourceSelectBox.setPrefWidth(200);
        //automatically fills after source is loaded
        ComboBox<String> classSelectBox = new ComboBox<>();
        classSelectBox.setPrefWidth(150);

        //create input boxes
        TextField acctNoField = new TextField();
        TextField addressField = new TextField();
        addressField.setPromptText("(#suite #house street)");
        TextField neighbourhoodField = new TextField();
        TextField minValueField = new TextField();
        minValueField.setPromptText("Min Value");
        minValueField.setPrefWidth(95);
        TextField maxValueField = new TextField();
        maxValueField.setPromptText("Max value");
        maxValueField.setPrefWidth(95);

        //create buttons
        Button readDataButton = new Button("Read Data");
        readDataButton.setPrefWidth(200);
        Button searchButton = new Button("Search");
        searchButton.setPrefWidth(95);
        Button resetButton = new Button("Reset");
        resetButton.setPrefWidth(95);

        //create flow pane for data source
        VBox dataSource = new VBox();
        dataSource.setPrefWidth(180);
        dataSource.setSpacing(5.0);
        dataSource.setPadding(new Insets(1,1,1,1));
        dataSource.setBorder(new Border(new BorderStroke(Color.rgb(200,200,200), BorderStrokeStyle.SOLID, null, null)));
        dataSource.getChildren().addAll(sourceLabel, sourceSelectBox, readDataButton);

        //create flow pane for the simple/advanced search
        VBox searchPane = new VBox();
        searchPane.setPrefWidth(180);
        searchPane.setSpacing(5.0);
        searchPane.setPadding(new Insets(1,1,1,1));
        //searchPane.setBorder(new Border(new BorderStroke(Color.rgb(200, 200, 200), BorderStrokeStyle.SOLID, null, null)));
        searchPane.getChildren().addAll(paLabel, accNoLabel, acctNoField,
                addressLabel, addressField, neighbourhoodLabel,
                neighbourhoodField, assessClassLabel, classSelectBox);

        //create flow pane for the assessed value search
        FlowPane valueSearchPane = new FlowPane();
        valueSearchPane.setHgap(10);
        valueSearchPane.setPrefWidth(180);
        valueSearchPane.getChildren().addAll(assessValueLabel, minValueField, maxValueField, searchButton, resetButton);
        //valueSearchPane.setBorder(new Border(new BorderStroke(Color.rgb(200, 200, 200), BorderStrokeStyle.SOLID, null, null)));
        valueSearchPane.setPadding(new Insets(1,1,1,1));

        //add everything to the left menu
        menu.getChildren().addAll(dataSource, separator, searchPane, valueSearchPane);
        rootNode.setLeft(menu);

        //create list to display property assessments
        ObservableList<PropertyAssessment> propertyDisplay = observableArrayList();

        VBox center = new VBox();
        Label title = new Label("Edmonton Property Assessments");
        title.setFont(roboto);
        center.getChildren().add(title);

        TableView<PropertyAssessment> table = makeTable(primaryStage, propertyDisplay);
        center.getChildren().add(table);
        rootNode.setCenter(center);

        //READ BUTTON ACTION
        readDataButton.setOnAction(actionEvent -> {
            if (!sourceSelectBox.getSelectionModel().isEmpty()) {
                String choice = sourceSelectBox.getValue();
                if (choice.equals(sources[0])) {
                    //set source global variable to CSV if they chose csv
                    source = "CSV";
                    try {
                        //then set dao to use CsvDAO
                        dao = new CsvPropertyAssessmentDAO("Property_Assessment_Data_2022.csv");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    List<PropertyAssessment> properties = dao.getProperties();
                    propertyDisplay.clear();
                    propertyDisplay.addAll(properties);

                } else if (choice.equals(sources[1])) {
                    //set source global variable to API if they chose edmonton open data portal
                    source = "API";
                    //otherwise set DAO to use ApiDao
                    dao = new ApiPropertyAssessmentDAO();
                    List<PropertyAssessment> properties = dao.getProperties();
                    propertyDisplay.clear();
                    propertyDisplay.addAll(properties);
                }
                //get assessment classes to add to the dropdown
                List<String> list = dao.getAssessmentClasses();
                classSelectBox.setItems(FXCollections.observableList(list));
                //NOTE: the blank item is intended so that users can select no assessment class without needing to reset
            } else {
                showAlert("No source selected!");
            }
        });

        //reset button event
        resetButton.setOnAction(actionEvent -> {
            //empty all fields
            acctNoField.setText("");
            addressField.setText("");
            neighbourhoodField.setText("");
            classSelectBox.getSelectionModel().clearSelection();
            minValueField.setText("");
            maxValueField.setText("");
            //reset properties in table
            List<PropertyAssessment> properties = dao.getProperties();
            propertyDisplay.clear();
            propertyDisplay.addAll(properties);
        });

        //search button event
        searchButton.setDefaultButton(true); //allows search by clicking enter
        searchButton.setOnAction(actionEvent -> {
            List<PropertyAssessment> properties = new ArrayList<>();
            String acctNo = acctNoField.getText();
            String address = addressField.getText();
            String neighbourhood = neighbourhoodField.getText();
            String assessment = classSelectBox.getValue();
            //if value is null set it to an empty string so I can use it the same as the other fields
            if(assessment == null){
                assessment = "";
            }
            String minVal = minValueField.getText();
            String maxVal = maxValueField.getText();

            //if a dao has not been selected yet
            if(source.equals("n/a")){
                showAlert("Select data source first!");
                return;
            }

            //if there is nothing in any field
            if(acctNo.length() == 0 && address.length() == 0 && neighbourhood.length() == 0 && assessment.length() == 0 && (minVal.length() == 0 && maxVal.length() == 0) ){
                //get all properties
                properties = dao.getProperties();
            }

            //if there is anything in the account number field
            if(!acctNo.equals("")){
                //just search by that only, regardless of if there is something in other fields (makes more sense to me)
                try {
                    PropertyAssessment prop = dao.getByAccountNumber(Integer.parseInt(acctNo));
                    if (prop != null){
                        properties.add(prop);
                    }
                } catch (NumberFormatException e) {
                    properties = new ArrayList<>();
                }
            }
            //if only address is filled
            else if (address.length() > 0 && neighbourhood.length() == 0 && assessment.length() == 0 && (minVal.length() == 0 && maxVal.length() == 0) ){
                properties = dao.getByAddress(address);
            }
            //else if only neighbourhood is filled
            else if (address.length() == 0 && neighbourhood.length() > 0 && assessment.length() == 0 && (minVal.length() == 0 && maxVal.length() == 0) ){
                properties = dao.getByNeighbourhood(neighbourhood);
            }
            //else if only assessment class is filled
            else if (address.length() == 0 && neighbourhood.length() == 0 && assessment.length() > 0 && (minVal.length() == 0 && maxVal.length() == 0) ){
                properties = dao.getByAssessmentClass(assessment);
            }
            //else if only one of the value ranges are filled
            else if (address.length() == 0 && neighbourhood.length() == 0 && assessment.length() == 0 && (minVal.length() > 0 || maxVal.length() > 0) ){
                properties = dao.getByValue(minVal, maxVal);
            } else if (address.length() == 0 && neighbourhood.length() == 0 && assessment.length() == 0) {
                resetButton.fire();
            } else {
                //this means that there are multiple search terms filled
                //I realize that I can use this for every search now that I made it, but I made it last
                //The other ones are still good to have for when there is only one search criteria, as they are more
                //optimized, no reason to do an advanced search if we don't need to...
                //make a hashmap of search criteria to send to advanced search method
                HashMap<String, String> searchCriteria = new HashMap<>();
                searchCriteria.put("address", address);
                searchCriteria.put("neighbourhood", neighbourhood);
                searchCriteria.put("assessment", assessment);
                searchCriteria.put("min", minVal);
                searchCriteria.put("max", maxVal);

                properties = dao.advancedSearch(searchCriteria);
            }

            //clear the previous properties out of the table
            propertyDisplay.clear();
            //check if any properties were returned by the searches
            if(properties.size()==0){
                showAlert("No properties fit the search criteria.");
            } else {
                //otherwise add the found properties to the table
                propertyDisplay.addAll(properties);
            }

        });

    }

    public void showAlert(String message) {
        //Adapted from REF: https://stackoverflow.com/a/36137669
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private TableView<PropertyAssessment> makeTable(Stage primaryStage, ObservableList<PropertyAssessment> propertyDisplay){
        TableView<PropertyAssessment> table = new TableView<>();
        table.prefWidthProperty().bind(primaryStage.widthProperty());
        table.prefHeightProperty().bind(primaryStage.heightProperty());
        table.setItems(propertyDisplay);

        TableColumn<PropertyAssessment, Integer> idCol = new TableColumn<>("Account");
        idCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        idCol.setPrefWidth(60);
        table.getColumns().add(idCol);

        TableColumn<PropertyAssessment, Address> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(175);
        table.getColumns().add(addressCol);

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        currency.setMaximumFractionDigits(0);
        TableColumn<PropertyAssessment, String> valueCol = new TableColumn<>("Assessed Value");
        //REF: https://edencoding.com/tableview-customization-cellfactory/
        valueCol.setCellValueFactory(cellData -> {
            String formattedCost = currency.format(cellData.getValue().getAssessedValue());
            return new SimpleStringProperty(formattedCost);
        });
        valueCol.setPrefWidth(90);
        table.getColumns().add(valueCol);

        TableColumn<PropertyAssessment, AssessmentClass> classCol = new TableColumn<>("Assessment Class");
        classCol.setCellValueFactory(new PropertyValueFactory<>("assessmentClass"));
        classCol.setPrefWidth(150);
        table.getColumns().add(classCol);

        TableColumn<PropertyAssessment, Neighbourhood> neighbourhoodCol = new TableColumn<>("Neighbourhood");
        neighbourhoodCol.setCellValueFactory(new PropertyValueFactory<>("neighbourhood"));
        neighbourhoodCol.setPrefWidth(200);
        table.getColumns().add(neighbourhoodCol);

        TableColumn<PropertyAssessment, Neighbourhood> locCol = new TableColumn<>("(Latitude, Longitude)");
        locCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locCol.setPrefWidth(200);
        table.getColumns().add(locCol);

        return table;

    }
}
