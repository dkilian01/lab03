package myBeans;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;


import java.beans.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


public class Terminarz extends VBox {

    @FXML
    private Label labelSchedule;
    @FXML
    private TextArea textAreaNote;
    @FXML
    private DatePicker dataPicker;
    @FXML
    private TextField textFieldTitle;

    public Terminarz(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Terminarz.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        hookupChangeListener();
        dataPicker.valueProperty().set(day);
    }

    public String getTitile(){
        return labelSchedule.textProperty().get();
    }

    public void setTitile(String title){
        labelSchedule.textProperty().set(title);
    }

    public StringProperty titleProperty(){
        return labelSchedule.textProperty();
    }


    private IntegerProperty maxSizeTextArea = new SimpleIntegerProperty(this, "maxSizeTextArea", 300);

    public void setMaxSizeTextArea(int maxSizeTextArea) {
        this.maxSizeTextArea.set(maxSizeTextArea);

    }

    public int getMaxSizeTextArea(){
        return this.maxSizeTextArea.get();
    }

    public IntegerProperty maxSizeTextAreaProperty(){
        return maxSizeTextArea;
    }

    private PropertyChangeSupport maxSizeTextAreaChanges = new PropertyChangeSupport(this);


    private LocalDate day = LocalDate.now();

    public String getDay() {
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return day.format(dt);
    }

    public void setDay(String value) throws PropertyVetoException {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = this.day;
        try {
            date = LocalDate.parse(value, dt);
        }
        catch (java.time.format.DateTimeParseException e){
        }
        LocalDate oldDay = this.day;
        vetoes.fireVetoableChange("day", oldDay, date);
        this.day = date;
    }

    public void setDay(LocalDate date) throws PropertyVetoException {
        LocalDate oldDay = this.day;
        vetoes.fireVetoableChange("day", oldDay, date);
        this.day = date;
    }
    private VetoableChangeSupport vetoes = new VetoableChangeSupport(this);

    public void addDayListener(VetoableChangeListener v){
        vetoes.addVetoableChangeListener(v);
    }

    public void removeDayListener(VetoableChangeListener v){
        vetoes.removeVetoableChangeListener(v);
    }



    private LocalDate upperLimit = LocalDate.now().plusYears(1);

    public String getUpperLimit() {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return upperLimit.format(dt);
    }

    public void setUpperLimit(String value) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = this.upperLimit;
        try {
            date = LocalDate.parse(value, dt);
        }
        catch (java.time.format.DateTimeParseException e){
        }
        this.upperLimit = date;
    }

    private LocalDate lowerLimit = LocalDate.now().minusYears(1);

    public String getLowerLimit() {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return lowerLimit.format(dt);
    }

    public void setLowerLimit(String value) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = this.lowerLimit;
        try {
            date = LocalDate.parse(value, dt);
        }
        catch (java.time.format.DateTimeParseException e){
        }
        this.lowerLimit = date;
    }

    public void hookupChangeListener(){
        maxSizeTextArea.addListener((observable, oldValue, newValue) -> {
            if(textAreaNote.textProperty().get().length() > newValue.intValue()){
                textAreaNote.textProperty().set(textAreaNote.textProperty().get().substring(0, newValue.intValue() - 1));
            }
        });
        dataPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                setDay(newValue);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            if(memory.containsKey(newValue)){
                Note n = memory.get(newValue);
                textFieldTitle.textProperty().set(n.title);
                textAreaNote.textProperty().set(n.note);
            }else {
                textAreaNote.clear();
                textFieldTitle.clear();
            }
        });

        addDayListener(new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                LocalDate newValue = (LocalDate)evt.getNewValue();
                LocalDate oldValue = (LocalDate)evt.getOldValue();
                if(newValue.isAfter(upperLimit) || newValue.isBefore(lowerLimit)){
                    dataPicker.valueProperty().set(oldValue);
                    throw new PropertyVetoException("Date out off range(lower or upper limit)", evt);
                }else {
                    dataPicker.valueProperty().set(newValue);
                }
            }
        });
    }


    HashMap<LocalDate, Note> memory = new HashMap<>();

    @FXML
    public void btnDeleteClicked(MouseEvent mouseEvent) {
        memory.remove(day);
        textAreaNote.clear();
        textFieldTitle.clear();
    }
    @FXML
    public void btnSaveClicked(MouseEvent mouseEvent) {
        if(textAreaNote.textProperty().get().length() > 0 || textFieldTitle.textProperty().get().length() > 0){
            memory.put(day, new Note(textFieldTitle.textProperty().get(), textAreaNote.textProperty().get()));
        }
    }

    @FXML
    public void noteKeyRelased(KeyEvent keyEvent) {
        if(textAreaNote.textProperty().get().length() >= getMaxSizeTextArea())
            textAreaNote.textProperty().set(textAreaNote.textProperty().get().substring(0, getMaxSizeTextArea()));
            textAreaNote.positionCaret(textAreaNote.textProperty().get().length() );
    }
    @FXML
    public void titleKeyRelased(KeyEvent keyEvent) {
        if(textFieldTitle.textProperty().get().length() >= 32)
            textFieldTitle.textProperty().set(textFieldTitle.textProperty().get().substring(0, 32));
        textFieldTitle.positionCaret(textFieldTitle.textProperty().get().length() );
    }

    @FXML
    public void btnPrevDayClicked(MouseEvent mouseEvent) {
        try {
            setDay(day.minusDays(1));
        } catch (PropertyVetoException e) {

        }
    }
    @FXML
    public void btnNextDayClicked(MouseEvent mouseEvent) {
        try {
            setDay(day.plusDays(1));
        } catch (PropertyVetoException e) {

        }
    }
   }


