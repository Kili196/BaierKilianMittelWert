package presentation;

import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SampleController implements Initializable {

    @FXML
    private Button btnAddValue;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnRemoveSelected;
    @FXML
    private CheckBox cbArithmetic;
    @FXML
    private CheckBox cbGeometric;

    @FXML
    private CheckBox cbHarmonic;

    @FXML
    private ListView<Double> listView;

    @FXML
    private Slider slider;
    @FXML
    private TextField tfNewValue;
    @FXML
    private Text txtArithmetic;
    @FXML
    private Text txtGeometric;
    @FXML
    private Text txtHarmonic;
    private final Alert alert = new Alert(Alert.AlertType.ERROR);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        alert.setTitle("Achtung");
        alert.setHeaderText("Fehler!!");
        alert.setContentText("Liste kann nur Zahlen enthalten!!");
        slider.setSnapToTicks(true);
        btnAddValue.setOnAction(actionEvent -> {
            checkDouble(tfNewValue);
        });
        tfNewValue.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                checkDouble(tfNewValue);
            }
        });
        btnRemoveSelected.setOnAction(actionEvent -> listView.getItems().remove(listView.getSelectionModel().getSelectedItem()));
        btnClear.setOnAction(actionEvent -> listView.getItems().clear());

        listView.getItems().addListener(this::calcArithmetic);
        cbArithmetic.selectedProperty().addListener(this::calcArithmetic);
        slider.valueProperty().addListener(this::calcArithmetic);
        listView.getItems().addListener(this::calcGeo);
        cbGeometric.selectedProperty().addListener(this::calcGeo);
        slider.valueProperty().addListener(this::calcGeo);
        listView.getItems().addListener(this::calcHarmonisch);
        cbHarmonic.selectedProperty().addListener(this::calcHarmonisch);
        slider.valueProperty().addListener(this::calcHarmonisch);

    }

    public void checkDouble(TextField textView){
        if (!textView.getText().isEmpty()) {
            try {
                Double.parseDouble(textView.getText());
                listView.getItems().add(Double.valueOf(textView.getText()));
                textView.clear();
            } catch (NumberFormatException e) {
                alert.show();
                textView.clear();
            }
        }
    }
    public void calcArithmetic(Observable observable) {
        if(listView.getItems().size() == 0){
            txtArithmetic.setText("Keine Zahlen in der Liste");
        }

        double countOfListView = 0;
        double sizeOfListView = listView.getItems().size();
        for (double i : listView.getItems()) {
            countOfListView += i;
        }
        double Aritmetic = (countOfListView / sizeOfListView);
        if (cbArithmetic.isSelected()) {
            txtArithmetic.setText(format(Aritmetic, (int) slider.getValue()));
        } else {
            txtArithmetic.setText("");
        }
    }
    public void calcGeo(Observable observable) {
        if(listView.getItems().size() == 0){
            txtGeometric.setText("Keine Zahlen in der Liste");
        }
        double multiplyListView = 0;
        boolean b = true;
        boolean minus = false;
        for (double i : listView.getItems()) {
            if (i <= 0) {
                minus = true;
            }
            if (b) {
                multiplyListView += i;
                b = false;
            } else {
                multiplyListView *= i;
            }

        }
        if (cbGeometric.isSelected()) {
            if (minus) {
                txtGeometric.setText("Minus Zahl oder Null!!");
            } else {
                txtGeometric.setText(format((Math.pow(multiplyListView, 1.0 / listView.getItems().size())), (int) slider.getValue()));
            }
        } else {
            txtGeometric.setText("");
        }
    }

    public void calcHarmonisch(Observable observable){
        if(listView.getItems().size() == 0){
            txtHarmonic.setText("Keine Zahlen in der Liste");
            return;
        }
        double summUnten = 0;
        for(double i: listView.getItems()){
            if(i == 0){
                txtHarmonic.setText("0");
                return;
            }
            summUnten += 1 / i;
        }
        if(cbHarmonic.isSelected()){
            txtHarmonic.setVisible(true);
            txtHarmonic.setText(format(listView.getItems().size() / summUnten, (int) slider.getValue()));

        }
        else{
            txtHarmonic.setVisible(false);
        }
    }
    public String format(double zahl, int nachkomma){
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMaximumFractionDigits(nachkomma);
        numberFormat.setMinimumFractionDigits(nachkomma);
        return numberFormat.format(zahl);
    }
}
