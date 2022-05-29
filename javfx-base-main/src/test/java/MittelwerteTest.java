

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Double.parseDouble;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThatThrownBy;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class MittelwerteTest extends FxTest {
    private static final String PATH_TO_FXML = "/layout/FXMLKILIAN.fxml";
    private ListView<Double> listView;
    private TextField tfNewValue;
    private Button btnAddValue;
    private Button btnRemoveSelected;
    private Button btnClear;
    private CheckBox cbArithmetic;
    private CheckBox cbGeometric;
    private CheckBox cbHarmonic;
    private Slider slider;
    private Text txtArithmetic;
    private Text txtGeometric;
    private Text txtHarmonic;

    @Start
    void initializeGUI(Stage primaryStage) throws IOException {
        startApplication(primaryStage, PATH_TO_FXML);
        listView = getById("listView");
        tfNewValue = getById("tfNewValue");
        btnAddValue = getById("btnAddValue");
        btnRemoveSelected = getById("btnRemoveSelected");
        btnClear = getById("btnClear");
        cbArithmetic = getById("cbArithmetic");
        cbGeometric = getById("cbGeometric");
        cbHarmonic = getById("cbHarmonic");
        slider = getById("slider");
        txtArithmetic = getById("txtArithmetic");
        txtGeometric = getById("txtGeometric");
        txtHarmonic = getById("txtHarmonic");
    }

    @Test
    void initialState_checkBoxesSelected() {
        assertThat(cbArithmetic.isSelected()).isTrue();
        assertThat(cbGeometric.isSelected()).isTrue();
        assertThat(cbHarmonic.isSelected()).isTrue();
    }

    @Test
    void initialState_textFieldEmpty() {
        assertThat(tfNewValue).hasText("");
    }

    @Test
    void initialState_listViewEmpty() {
        assertThat(listView).isEmpty();
    }

    @Test
    void initialState_slider0() {
        assertThat(slider.getValue()).isEqualTo(0);
    }

    @Test
    void addViaTextFieldAction_itemAppended(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        robot.interact(tfNewValue::requestFocus)
                .write("4.0");

        robot.press(KeyCode.ENTER);

        assertThat(listView.getItems()).containsExactly(1.0, 2.0, 3.0, 4.0);
    }

    @Test
    void addViaButtonAdd_itemAppended(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        robot.interact(tfNewValue::requestFocus)
                .write("4.0");

        robot.interact(btnAddValue::fire);

        assertThat(listView.getItems()).containsExactly(1.0, 2.0, 3.0, 4.0);
    }

    @Test
    void add_textFieldCleared(FxRobot robot) {
        robot.interact(tfNewValue::requestFocus)
                .write("1.0");

        robot.interact(btnAddValue::fire);

        assertThat(tfNewValue.getText()).isEmpty();
    }

    @Test
    void addWithtextEntered_errorMessageDisplayed(FxRobot robot) throws InterruptedException {
        robot.interact(tfNewValue::requestFocus)
                .write("NaN");

        Platform.runLater(btnAddValue::fire);

        Thread.sleep(20);
        List<Window> windows = robot.listWindows();
        Assertions.assertThat(windows).hasSizeGreaterThan(1);
    }

    @Test
    void negativeValueAdded_geometricMeanUndefined(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        robot.interact(tfNewValue::requestFocus);
        robot.write("-1.0");

        robot.interact(btnAddValue::fire);

        assertThat(geometricMeanTextIsNumber()).isFalse();
    }

    @Test
    void value0Added_geometricMeanUndefined(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        robot.interact(tfNewValue::requestFocus);
        robot.write("0");

        robot.interact(btnAddValue::fire);

        assertThat(geometricMeanTextIsNumber()).isFalse();
    }

    private boolean geometricMeanTextIsNumber() {
        try {
            parseDouble(txtGeometric.getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Test
    void lastIllegalValueRemoved_geometricMeanDefined(FxRobot robot) {
        addValue(robot, -1);
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        addValue(robot, 0.0);
        robot.interact(() -> listView.getSelectionModel().select(-1.0));
        robot.interact(btnRemoveSelected::fire);
        assumeThatThrownBy(() -> parseDouble(txtGeometric.getText()));
        robot.interact(() -> listView.getSelectionModel().select(0.0));
        robot.interact(btnRemoveSelected::fire);
        assertThat(txtGeometric).hasText("2");
    }

    private void addValue(FxRobot robot, double d) {
        robot.interact(tfNewValue::requestFocus);
        robot.write(String.valueOf(d));
        robot.interact(btnAddValue::fire);
    }

    @Test
    void illegalValueRemovedSomeRemaining_geometricMeanUndefined(FxRobot robot) {
        addValue(robot, -2);
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        addValue(robot, 0);

        robot.interact(() -> listView.getSelectionModel().select(0.0));
        robot.interact(btnRemoveSelected::fire);

        assertThat(geometricMeanTextIsNumber()).isFalse();
    }

    @Test
    void clearButton_listCleared(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));

        robot.interact(btnClear::fire);

        assertThat(listView.getItems()).isEmpty();
    }

    @Test
    void deleteButtonNothingSelected_noOp(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));
        robot.interact(() -> listView.getSelectionModel().clearSelection());

        assertThatCode(() -> robot.interact(btnRemoveSelected::fire))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteButtonItemSelected_selectedItemRemoved(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.0, 2.0, 3.0));

        robot.interact(() -> listView.getSelectionModel().select(1));
        robot.interact(btnRemoveSelected::fire);

        assertThat(listView.getItems()).containsExactly(1.0, 3.0);
    }

    @TestFactory
    Stream<DynamicTest>
    checkBoxUnchecked_noResultText(FxRobot robot) {
        return Map.of(this.cbArithmetic, txtArithmetic, cbGeometric, txtGeometric, cbHarmonic, txtHarmonic)
                .entrySet().stream()
                .map(e -> DynamicTest.dynamicTest(e.getKey().getText(), () -> {
                    robot.interact(e.getKey()::fire);

                    assertThat(e.getValue()).isInvisible();
                }));
    }

    @TestFactory
    Stream<DynamicTest> checkBoxUncheckedAndRechecked_resultText(FxRobot robot) {
        addValue(robot, 1);
        return Map.of(this.cbArithmetic, txtArithmetic, cbGeometric, txtGeometric, cbHarmonic, txtHarmonic)
                .entrySet().stream()
                .map(e -> DynamicTest.dynamicTest(e.getKey().getText(), () -> {
                    robot.interact(e.getKey()::fire);

                    robot.interact(e.getKey()::fire);

                    assertThat(e.getValue()).hasText("1");
                }));
    }

    @Test
    void listContainsValues_arithmeticMeanCorrect(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(-1.666, 3.0, 4.7, 0.0));

        robot.interact(() -> slider.setValue(5));

        assertThat(txtArithmetic).hasText("1.50850");
    }

    @Test
    void listContainsPositiveValues_geometricMeanCorrect(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.666, 3.0, 4.7));

        robot.interact(() -> slider.setValue(5));

        assertThat(txtGeometric).hasText("2.86395");
    }

    @Test
    void listContainsNo0_harmonicMeanCorrect(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.666, 3.0, 4.7));

        robot.interact(() -> slider.setValue(5));

        assertThat(txtHarmonic).hasText("2.61703");
    }

    @Test
    void listContains0_harmonicMean0(FxRobot robot) {
        robot.interact(() -> listView.getItems().addAll(1.666, 3.0, 4.7, 0.0));

        robot.interact(() -> slider.setValue(5));
        assertThat(txtHarmonic).hasText("0.00000");
    }

    @Test
    void itemAdded_meansUpdated(FxRobot robot) {
        robot.interact(() -> slider.setValue(5));
        addValue(robot, 3);

        addValue(robot, 5);

        assertThat(txtArithmetic).hasText("4.00000");
        assertThat(txtGeometric).hasText("3.87298");
        assertThat(txtHarmonic).hasText("3.75000");
    }

    @Test
    void itemRemoved_meansUpdated(FxRobot robot) {
        robot.interact(() -> slider.setValue(5));
        robot.interact(() -> listView.getItems().addAll(3.0, 4.0, 5.0));

        robot.interact(() -> listView.getSelectionModel().select(4.0));
        robot.interact(btnRemoveSelected::fire);

        assertThat(txtArithmetic).hasText("4.00000");
        assertThat(txtGeometric).hasText("3.87298");
        assertThat(txtHarmonic).hasText("3.75000");
    }

    @TestFactory
    Stream<DynamicTest> sliderMoved_arithmeticMeanFractionDigitsUpdated(FxRobot robot) {
        robot.interact(() -> slider.setValue(Double.NaN));
        double[] expected = {2, 2.1, 2.10, 2.097, 2.0967, 2.09667};
        return IntStream.range(0, 6).mapToObj(i -> DynamicTest.dynamicTest(String.format("%d Fractiondigit", i), () -> {
            robot.interact(listView.getItems()::clear);
            robot.interact(() -> listView.getItems().addAll(-1.87, 3.34, 4.82));
            robot.interact(() -> slider.setValue(i));
            assertThat(txtArithmetic).hasText(format(expected[i], i));
        }));
    }

    private String format(double mean, int fractionDigits) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMinimumFractionDigits(fractionDigits);
        formatter.setMaximumFractionDigits(fractionDigits);
        return formatter.format(mean);
    }

    @TestFactory
    Stream<DynamicTest> sliderMoved_geometricMeanFractionDigitsUpdated(FxRobot robot) {
        robot.interact(() -> slider.setValue(Double.NaN));
        double[] expected = {3, 2.9, 2.86, 2.864, 2.8639, 2.86395};
        return IntStream.range(0, 6).mapToObj(i -> DynamicTest.dynamicTest(String.format("%d Fractiondigit", i), () -> {
            robot.interact(listView.getItems()::clear);
            robot.interact(() -> listView.getItems().addAll(1.666, 3.0, 4.7));

            robot.interact(() -> slider.setValue(i));

            assertThat(txtGeometric).hasText(format(expected[i], i));
        }));
    }

    @TestFactory
    Stream<DynamicTest> sliderMoved_harmonicMeanFractionDigitsUpdated(FxRobot robot) {
        robot.interact(() -> slider.setValue(Double.NaN));
        double[] expected = {3, 2.6, 2.62, 2.617, 2.6170, 2.61703};
        return IntStream.range(0, 6).mapToObj(i -> DynamicTest.dynamicTest(String.format("%d Fractiondigit", i), () -> {
            robot.interact(listView.getItems()::clear);
            robot.interact(() -> listView.getItems().addAll(1.666, 3.0, 4.7));

            robot.interact(() -> slider.setValue(i));

            assertThat(txtHarmonic).hasText(format(expected[i], i));
        }));
    }
}
