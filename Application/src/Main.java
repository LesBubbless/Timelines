
import com.sun.javafx.css.Stylesheet;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Main back-end class, is parent to both Timeline and Event,
 * used due to polymorphism to easily store Elements on front-end
 */

class Element {
    String name;
    boolean visible;

    Element (String name) {
        this.name = name;
        visible = false;
    }
}

/**
 * Object that contains all data about the timeline, as well as all events in it (using a LinkedList)
 */

class Timeline extends Element {

    LinkedList<Event> events;

    /**
     * Creates a new Timeline
     *
     * @param name  name of the Timeline
     */

    Timeline (String name) {
        super(name);
        visible = false;
        events = new LinkedList<>();
    }

    /**
     * Adds a new Event into the LinkedList events, sorted based on startDate
     *
     * @param name      name of the new Event
     * @param startDate start of the new Event (three-element array [day, month, year])
     * @param endDate   end of the new Event (three-element array [day, month, year])
     * @param visible   whether the new Event is drawn
     * @param notes     the new Event's notes
     */

    void addEvent(String name, int[] startDate, int[] endDate, boolean visible, String notes) {

//        index of the current node (event)
        int current = 0;

//        loops through Events until it has an earlier date than the next Event, adds new Event
        for (Event event : events) {
            if (wasSooner(startDate, event.startDate)) {
                events.add(current, new Event(this, name, startDate, endDate, visible, notes));
                return;
            }
            current++;
        }
        events.add(current, new Event(this, name, startDate, endDate, visible, notes));
    }

    /**
     * Checks whether date1 was sooner than date2
     *
     * @param date1 first date
     * @param date2 second date
     * @return      true if date1 was sooner than date2
     */

    boolean wasSooner (int[] date1, int[] date2) {

//        loops through year, then month, then day if previous value is the same
        for (int i = 2; i > -1; i--) {
            if (date1[i] > date2[i])
                return false;
            if (date1[i] < date2[i])
                return true;
        }
        return true;
    }
}

/**
 * Object that contains all data about an event
 */

class Event extends Element {

    Timeline timeline;
    int[] startDate, endDate;
    String notes;

    /**
     * Creates a new Event
     *
     * @param timeline  the Timeline the Event belongs to
     * @param name      name of the Event
     * @param startDate start of the Event (three-element array [day, month, year])
     * @param endDate   end of the Event (three-element array [day, month, year])
     * @param visible   whether the Event is drawn
     * @param notes     the Event's notes
     */

    Event (Timeline timeline, String name, int[] startDate, int[] endDate, boolean visible, String notes) {
        super(name);
        this.timeline = timeline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visible = visible;
        this.notes = notes;
    }
}

/**
 * Handles all the application data (back-end)
 */

class App {

    /**
     * Custom BufferedReader -- has functions to retrieve parameters from file
     */

    class Reader extends BufferedReader {

        Reader(java.io.Reader in) {
            super(in);
        }

        /**
         * Function used to get Element name
         *
         * @return  the whole line after the argument as String
         * @throws  Exception
         */

        String getStringArgument() throws Exception {
            return this.readLine().split("\\s+", 2)[1];
        }

        /**
         * Function to get Integer arguments, used for getting Element visibility
         * return as Integer instead of boolean to make expansion easier
         *
         * @return  first space-separated value as Integer
         * @throws  Exception
         */

        int getIntArgument() throws Exception {
            return Integer.parseInt(this.readLine().split("\\s+", 2)[1]);
        }

        /**
         * Function to get a date as a three-integer array ([day, month, year])
         *
         * @return  second, third, and fourth value as a three-integer array
         * @throws  Exception
         */

        int[] getDateArgument() throws Exception {
            return Arrays.stream(Arrays.copyOfRange(this.readLine().split("\\s+"), 1, 4)).mapToInt(Integer::parseInt).toArray();
        }

        /**
         * Function to get notes, reads until the [/event] tag is reached (non-inclusive),
         * supports multi-line notes
         *
         * @return  Multi-line String
         * @throws  Exception
         */

        String getNotesArgument() throws Exception {
            String output = "";
            String line = this.readLine().trim();

            while (!line.contains("[/event]")) {
                output += line;
                line = this.readLine().trim();
            }

            return output;
        }
    }

    /**
     * Custom BufferedWriter -- has functions to write into file
     */

    class Writer extends BufferedWriter {

        Writer(java.io.Writer out) {
            super(out);
        }

        /**
         * Write function for a Timeline, writes the [timeline] tag followed by name,
         * includes the flush() method to save file after every Element is loaded
         *
         * @param   timeline    Timeline that is to be written
         * @throws  Exception
         */

        void writeTimeline (Timeline timeline) throws Exception {
            this.write("[timeline]\n");
            this.write("name " + timeline.name + "\n");
            this.newLine();
            this.flush();
        }

        /**
         * Write function for an Event, writes the [event] tag followed by:
         * the Timeline it belongs to, its name, startDate, endDate, visible and notes
         * (in that order), followed by the [/event] tag
         *
         * @param   event   Event that is to be written
         * @throws  Exception
         */

        void writeEvent (Event event) throws Exception {
            this.write("[event]\n");
            this.write("timeline " + event.timeline.name + "\n");
            this.write("name " + event.name + "\n");
            this.write("startDate " + event.startDate[0] + " " + event.startDate[1] + " " + event.startDate[2] + "\n");
            this.write("endDate " + event.endDate[0] + " " + event.endDate[1] + " " + event.endDate[2] + "\n");
            this.write("visible " + (event.visible ? 1 : 0) + "\n");
            this.write(event.notes + "\n[/event]\n");
            this.newLine();
            this.flush();
        }
    }

    LinkedList<Timeline> timelines;         // stores all Timelines currently loaded
    LinkedList<Element> deleteElements;     // stores all Elements to be deleted, used in Delete mode

    File file = new File("../data.txt");

    /**
     * Constructor -- creates a new instance of the App class,
     * attempts to open file, creates a new one if attempt fails
     *
     * @throws Exception
     */

    App () throws Exception {
        timelines = new LinkedList<>();
        deleteElements = new LinkedList<>();
        file.createNewFile();
    }

    /**
     * Loads all Timelines and Events from data.txt file
     *
     * @return  success message as String
     * @throws  Exception
     */

    String loadFromFile () throws Exception {

        Reader reader = new Reader(new FileReader(file));
        String line;

//        reads all lines of the file
        while ((line = reader.readLine()) != null) {

//            depending on the object tag [element], it reads relevant parameters and creates the Element
            if (line.contains("[timeline]"))
                timelines.add(new Timeline(reader.getStringArgument()));

            else if (line.contains("[event]")) {
                String timelineName = reader.getStringArgument();
                for (Timeline timeline : timelines) {
                    if (timelineName.equals(timeline.name)) {
                        timeline.addEvent(
                                reader.getStringArgument(),
                                reader.getDateArgument(),
                                reader.getDateArgument(),
                                reader.getIntArgument() != 0,
                                reader.getNotesArgument());
                        break;
                    }
                }
            }
        }
        reader.close();
        return ("Data loaded successfully");
    }

    /**
     * Saves all Timelines and Events to data.txt file, implements dataBackup.txt
     * to always retain at least 1 written copy of all data
     *
     * 1) creates a backup file (dataBackup.txt)
     * 2) attempts to delete it
     * 3) renames data.txt to dataBackup.txt
     *
     * if any of these operations fail, save is terminated
     *
     * @return  success message as String
     * @throws  Exception
     */

    String saveToFile () throws Exception {

        File backupFile = new File("../dataBackup.txt");

//        checks if backup file exists, if yes, it attempts to delete it
        if (backupFile.exists() && !backupFile.delete())
            return ("Save terminated: backup could not be deleted");
//        attempts to rename file to backup file
        if (!file.renameTo(backupFile))
            return ("Save terminated: backup could not be created");

//        creates custom writer
        Writer writer = new Writer(new FileWriter(file));

//        cycles through all timelines and all their events, writing all into the same file
        for (Timeline timeline : timelines) {
            writer.writeTimeline(timeline);
            for (Event event : timeline.events) {
                writer.writeEvent(event);
            }
        }

        writer.close();
        return ("Save finished successfully");
    }
}

/**
 * Main function, acts as the JavaFX controller class (handles front-end)
 * and launches App (back-end)
 */

public class Main extends Application {

    /**
     * Custom JavaFX CheckBox, retains the Element it represents.
     * Used in Menu, can send its Element as parameter to MouseEvents
     */

    class MenuBox extends CheckBox {

        Element element;

        /**
         * Constructor -- creates a CheckBox with the Element's name as text
         * @param element the Element the CkechBox represents
         */

        MenuBox (Element element) {
            this.element = element;
            this.setText(element.name);

//            sets the CheckBox CSS style
            this.getStyleClass().add(stylesheet.toString());
            this.getStyleClass().add("menuCheckBox");

//            sets toggleVisible to run when the CheckBox is checked
            this.setOnAction(this::toggleVisible);

//            sets expandElement to run when the CheckBox is right-clicked
//            passes element as argument (it can be accessed directly)
            this.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    expandElement(element);
            });

//            checks the CheckBox if Element is visible
            if (element.visible)
                this.setSelected(true);
        }

        /**
         * Alternative Constructor -- creates a CheckBox with the Element's name as text;
         * used for Delete mode (bound to toggleDeleteVisible instead of toggleVisible, different style)
         *
         * @param element   the Element the CheckBox represents
         * @param delete    if this parameter exists, this constructor is called. Value is arbitrary
         */

        MenuBox (Element element, boolean delete) {
            this.element = element;
            this.setText(element.name);

//            sets the CheckBox CSS style
            this.getStyleClass().add(stylesheet.toString());
            this.getStyleClass().add("menuCheckBox");
            this.getStyleClass().add("deleteCheckBox");

//            sets toggleVisible to run when the CheckBox is checked
            this.setOnAction(this::toggleDeleteVisible);
        }

        /**
         * Changes Element visibility based on whether the CheckBox is checked,
         * called when the CheckBox changes state
         *
         * @param   actionEvent   ActionEvent as required part of the Lambda call
         */

        void toggleVisible (ActionEvent actionEvent) {
            element.visible = this.isSelected();
        }

        /**
         * Adds/removes Element from the to-be-deleted LinkedList,
         * based on whether the CheckBox is checked.
         *
         * Called when the CheckBox changes state in Delete mode
         *
         * @param   actionEvent ActionEvent as required part of the Lambda call
         */

        void toggleDeleteVisible (ActionEvent actionEvent) {
            if (this.isSelected())
                app.deleteElements.add(element);
            else
                app.deleteElements.remove(element);
        }
    }

    /**
     * Custom JavaFX Label, consists of a Label, Line, and Event,
     * used to draw Events on a Timeline Axis
     */

    class Marker extends Label {

        Event event;
        double start, end, width;
        Line line;

        /**
         * Constructor -- creates all Nodes to draw the Event
         *
         * @param   event   the Event it represents
         * @param   y       common y value for one Axis
         */

        Marker (Event event, double y) {

//            assigns an Event to the Marker
            this.event = event;

//            defines the start and end of the Line and Label
//            based on the beginning and end of the Event, the zoom (stretch) and global edge of all Nodes (shift)
            start = (event.startDate[2] * stretch) - shift;
            end = (event.endDate[2] * stretch) - shift;
            width = end - start;

//            creates the label
            this.setLayoutX(start + width/2 - width()/2);
            this.setLayoutY(y + 16);
            this.setWidth(width);
            this.setTextAlignment(TextAlignment.CENTER);

//            first line contains Event name
//            second line is either 1 (if start year and end year are the same) or 2 dates
            if (event.startDate[2] == event.endDate[2])
                this.setText(event.name + "\n" + event.startDate[2]);
            else
                this.setText(event.name + "\n" + event.startDate[2] + "–" + event.endDate[2]);

//            creates the Line
            line = new Line(start, y, end, y);

//            applies CSS to the Label and Line
            this.setStyle(stylesheet.toString());
            this.getStyleClass().add("markerLabel");
            line.setStyle(stylesheet.toString());
            line.getStyleClass().add("markerLine");

//            sets right click on Label or Line to expand the Event
            this.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    expandElement(event);
            });
            line.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    expandElement(event);
            });
        }

        /**
         * Custom width function, since JavaFX getWidth() can only be
         * called after the Node is drawn
         *
         * @return  width of the Label based on String width
         */

        double width () {
            Text text = new Text(event.name);
            text.setStyle(this.getStyle());
            return text.getBoundsInLocal().getWidth() * 1.5;
        }
    }

    /**
     * Custom JavaFX Label, consists of 2 Labels, Line, and Timeline,
     * used to draw a Timeline Axis
     */

    class Axis extends Label {

        Timeline timeline;
        double start, end, y;
        Line line;
//        similar to Axis (Label), fixed in place, shows if Axis is not visible
        Label persistentLabel;

        /**
         * Constructor -- creates all Nodes necessary to calculate
         *                the position of the Axis
         *
         * @param   timeline    the Timeline it represents
         */

        Axis (Timeline timeline) {

//            assigns a Timeline to the Axis
            this.timeline = timeline;

//            creates a common y value for all Elements of the Axis
//            also used by all Markers on this Axis
            y = (axes.size() + 1) * 120;

//            places and styles the Axis
            this.setText(timeline.name.toUpperCase());
            this.setLayoutY(y - 12);
            this.setStyle(stylesheet.toString());
            this.getStyleClass().add("marker");

//            places and styles the persistentLabel
            persistentLabel = new Label(timeline.name.toUpperCase());
            persistentLabel.setLayoutY(y - 13);
            persistentLabel.setStyle(stylesheet.toString());
            persistentLabel.getStyleClass().add("marker");
            persistentLabel.setStyle("-fx-text-fill: #cccccc");
            persistentLabel.setLayoutX(20);
            persistentLabel.setVisible(false);

//            sets start as the startDate of the first Event
            start = timeline.events.peekFirst().startDate[2];

//            sets right click on the Label and persistentLabel to expand Timeline
            this.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    expandElement(timeline);
            });
            persistentLabel.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    expandElement(timeline);
            });
        }

        /**
         * draws the Axis on a ScrollPane (mainPane) and all its Markers
         *
         * @param   objects             mainPane children
         * @param   persistentObjects   persistentPane children
         */

        void draw (ObservableList<Node> objects, ObservableList<Node> persistentObjects) {

//            defines the start and end of the Line based on the first and last Event
//            of the Timeline, the zoom (stretch) and global edge of all Nodes (shift)
            start = (timeline.events.peekFirst().startDate[2] * stretch) - shift;
            end = (timeline.events.peekLast().startDate[2] * stretch) - shift;

//            defines the X coordinates of the Label before the first Event
            this.setLayoutX(start - this.width() - 10);

//            places and styles the accompanying Line
            line = new Line(start, y, end, y);
            line.setStyle(stylesheet.toString());
            line.getStyleClass().add("axisLine");

//            sets right click on the Line to expand Timeline as well
            line.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY)
                    expandElement(timeline);
            });

//            draws the Label and Line on mainPane
            objects.addAll(this, line);

//            cycles through all Timeline Events
//            creates Marker and draws it on mainPane
            for (Event event : timeline.events) {
                if (event.visible) {
                    Marker marker = new Marker(event, y);
                    objects.addAll(marker, marker.line);
                }
            }

//            draws persistentLabel on persistentPane
            persistentObjects.add(persistentLabel);
        }

        /**
         * Toggles persistentLabel visibility after scrolling
         */

        void onScroll () {

//            if main Label is not visible, but the Line is, persistentLabel is set to visible
            if (!visibleNodes.contains(this) && visibleNodes.contains(line))
                persistentLabel.setVisible(true);

//            in all other cases, persistentLabel is set to hidden
            else
                persistentLabel.setVisible(false);
        }

        /**
         * Custom width function, since JavaFX getWidth() can only be
         * called after the Node is drawn
         *
         * @return  width of the Label based on String width
         */

        double width () {
            Text text = new Text(this.getText());
            text.setStyle(this.getStyle());
            return text.getBoundsInLocal().getWidth() * 1.5;
        }
    }

//    JavaFX Nodes which are accessed in the code
    public static Image logo;
    public static Scene baseScene, mainScene, menuScene, addEventScene, addTimelineScene;
    public static Stage window, errorWindow;
    public static Button mainBarClose, mainBarMaximise, mainBarMinimise, menuBarClose, menuBarMaximise, menuBarMinimise;
    public static Button exitButton, addTimelineButton, addEventButton, saveButton, deleteButton;
    public static Button cancelTimelineButton, cancelEventButton, confirmDeleteButton, cancelDeleteButton;
    public static Label messageLabel;
    public static ScrollPane mainPane, menuPane, persistentPane;
    public static Stylesheet stylesheet;

//    add Timeline window Nodes
    public static Button timeline_addButton, timeline_editButton;
    public static TextField timeline_nameField;
    public static Label timeline_addLabel, timeline_editLabel;

//    add Event window Nodes
    public static Button event_addButton, event_editButton;
    public static ChoiceBox timelineChoice;
    public static TextField event_nameField, event_notesField;
    public static TextField event_startDayField, event_startMonthField, event_startYearField, event_endDayField, event_endMonthField, event_endYearField;
    public static Label event_addLabel, event_editLabel;

//    the back-end variables
    public static App app;
    public static LinkedList<Node> visibleNodes;        // contains all Nodes currently visible to the user (≠ all drawn nodes!)
    public static LinkedList<Axis> axes;                // contains all visible (drawn) Axis Nodes
    public static double stretch, shift;                // stretch = zoom factor; shift = shift of all Nodes

    /**
     * Custom JavaFX Application constructor, starts the window
     *
     * @param   startStage  proprietary JavaFX initial Stage
     */

    @Override
    public void start (Stage startStage) {

//        loads all Scenes from FXML files, shows error if unsuccessful
        try {
            baseScene = new Scene(FXMLLoader.load(getClass().getResource("windows/baseWindow.fxml")));
            baseScene.setFill(Color.TRANSPARENT);
            mainScene = new Scene(FXMLLoader.load(getClass().getResource("windows/mainWindow.fxml")));
            mainScene.setFill(Color.TRANSPARENT);
            menuScene = new Scene(FXMLLoader.load(getClass().getResource("windows/menuWindow.fxml")));
            menuScene.setFill(Color.TRANSPARENT);
            addEventScene = new Scene(FXMLLoader.load(getClass().getResource("windows/addEventWindow.fxml")));
            addTimelineScene = new Scene(FXMLLoader.load(getClass().getResource("windows/addTimelineWindow.fxml")));
        } catch (Exception exception) {
            showError("loading GUI", exception.toString());
        }

//        loads visuals (CSS stylesheet and logo)
        stylesheet = new Stylesheet("stylesheet.css");
        logo = new Image("/images/icon-main.png");

//        links all JavaFX Nodes defined as static variables, binds Listeners
        loadFXMLElements();
        createListeners();

//        starts the main Stage (window) on menu (menuScene)
        window = startStage;
//        window.setTitle("Timelines: Menu");
        window.setScene(menuScene);
//        window.getIcons().add(logo);
        window.initStyle(StageStyle.TRANSPARENT);

        window.show();

//        creates a new App (back-end), attempts to load data from file, creates success / error user feedback
        try {
            app = new App();
            setMessage(app.loadFromFile() + ": " + app.timelines.size() + " Timelines loaded", false);
        } catch (Exception exception) {
            showError("loading data from file", exception.toString() + "\n(Expected file data.txt in the same directory)");
        }

//        draws all Elements in menu
        drawMenuElements(false);

//        creates all default back-end variables
        visibleNodes = new LinkedList<>();
        axes = new LinkedList<>();
        shift = 0;
        stretch = 1;
    }

    /**
     * Loads all JavaFX Nodes defined as variables created in FXML files
     */

    void loadFXMLElements ()  {
        Parent menuRoot = menuScene.getRoot();
        Parent mainRoot = mainScene.getRoot();

//        all ScrollPanes
        mainPane = (ScrollPane) mainScene.getRoot().lookup("#main_scrollPane");
        menuPane = (ScrollPane) menuScene.getRoot().lookup("#menu_scrollPane");
        persistentPane = (ScrollPane) mainScene.getRoot().lookup("#main_persistentScrollPane");

//        top bar controls
        mainBarClose = (Button) mainRoot.lookup("#main_barClose");
        mainBarMaximise = (Button) mainRoot.lookup("#main_barMaximise");
        mainBarMinimise = (Button) mainRoot.lookup("#main_barMinimise");

        menuBarClose = (Button) menuRoot.lookup("#menu_barClose");
        menuBarMaximise = (Button) menuRoot.lookup("#menu_barMaximise");
        menuBarMinimise = (Button) menuRoot.lookup("#menu_barMinimise");

//        all Nodes in Menu
        messageLabel = (Label) menuRoot.lookup("#menu_messageLabel");
        exitButton = (Button) menuRoot.lookup("#menu_goToMain");
        addTimelineButton = (Button) menuRoot.lookup("#menu_addTimeline");
        addEventButton = (Button) menuRoot.lookup("#menu_addEvent");
        saveButton = (Button) menuRoot.lookup("#menu_save");
        deleteButton = (Button) menuRoot.lookup("#menu_delete");

        cancelTimelineButton = (Button) menuRoot.lookup("#menu_exitAddTimeline");
        cancelEventButton = (Button) menuRoot.lookup("#menu_exitAddEvent");
        confirmDeleteButton = (Button) menuRoot.lookup("#menu_confirmDelete");
        cancelDeleteButton = (Button) menuRoot.lookup("#menu_cancelDelete");

//        new Timeline Nodes
        timeline_nameField = (TextField) addTimelineScene.lookup("#timeline_nameField");
        timeline_addButton = (Button) addTimelineScene.lookup("#timeline_createButton");
        timeline_editButton = (Button) addTimelineScene.lookup("#timeline_editButton");
        timeline_addLabel = (Label) addTimelineScene.lookup("#timeline_addLabel");
        timeline_editLabel = (Label) addTimelineScene.lookup("#timeline_editLabel");

//        new Event Nodes
        timelineChoice = (ChoiceBox) addEventScene.lookup("#event_timelineChoice");
        event_nameField = (TextField) addEventScene.lookup("#event_nameField");
        event_startDayField = (TextField) addEventScene.lookup("#event_startDayField");
        event_startMonthField = (TextField) addEventScene.lookup("#event_startMonthField");
        event_startYearField = (TextField) addEventScene.lookup("#event_startYearField");
        event_endDayField = (TextField) addEventScene.lookup("#event_endDayField");
        event_endMonthField = (TextField) addEventScene.lookup("#event_endMonthField");
        event_endYearField = (TextField) addEventScene.lookup("#event_endYearField");
        event_notesField = (TextField) addEventScene.lookup("#event_notesField");
        event_addButton = (Button) addEventScene.lookup("#event_createButton");
        event_editButton = (Button) addEventScene.lookup("#event_editButton");
        event_addLabel = (Label) addEventScene.lookup("#event_addLabel");
        event_editLabel = (Label) addEventScene.lookup("#event_editLabel");
    }

    /**
     * Creates all Listeners for Nodes
     */

    void createListeners () {

//        mainPane top bar controls
//        makes window closing conditional on saving
        mainBarClose.setOnAction(event -> {
            try {                               // if save is successful, window closes
                app.saveToFile();
                System.exit(0);
            } catch (Exception exception) {     // if save fails, window stays open, the user is warned ...
                event.consume();
                showError("closing the application", exception.toString());
                setMessage("Save-and-close disabled, closing the application will not save data.", true);
            } finally {                         // ... and the save-on-close feature is disabled
                window.setOnCloseRequest(event1 -> System.exit(0));
            }
        });
        menuBarClose.setOnAction(mainBarClose.getOnAction());

        mainBarMaximise.setOnAction(event -> {
            window.setMaximized(!window.isMaximized());
        });
        menuBarMaximise.setOnAction(mainBarMaximise.getOnAction());

        mainBarMinimise.setOnAction(event -> {
            window.setIconified(true);
        });
        menuBarMinimise.setOnAction(mainBarMinimise.getOnAction());

//        binds mainPane and persistentPane vertical scrolls -- they both scroll like one
        persistentPane.vvalueProperty().bind(mainPane.vvalueProperty());

//        listener for horizontal scrolling in Main window
        mainPane.hvalueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

//                calls persistentLabel check for all visible axes
            for (Axis axis : axes)
                axis.onScroll();

//                creates a list of all currently visible Nodes
//                cycles through all Nodes, adds Nodes currently within visible part of mainPane (ScrollPane)
            visibleNodes.clear();

            Bounds paneBounds = mainPane.localToScene(mainPane.getBoundsInParent());
            for (Node node : ((Parent) mainPane.getContent()).getChildrenUnmodifiable()) {
                Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
                if (paneBounds.intersects(nodeBounds))
                    visibleNodes.add(node);
            }
        });

//        listener for vertical size change -- dynamic persistentPane resize
        mainPane.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            persistentPane.setHvalue(mainPane.getHvalue());
        });

//        listener for scrolling in Main window
        mainScene.setOnScroll(scrollEvent -> {

//            binds the CTRL+scroll to zoom in and out
            if (scrollEvent.isControlDown()) {
                if (scrollEvent.getDeltaY() > 0) {
                    stretch *= 1.2;
                    drawMainElements();
                }
                else if (scrollEvent.getDeltaY() < 0) {
                    stretch /= 1.2;
                    drawMainElements();
                }
                scrollEvent.consume();
            }
        });

//        listener for keys pressed in Main window
        mainScene.setOnKeyPressed(keyEvent -> {

//            binds I to zoom in
            if (keyEvent.getCode() == KeyCode.I) {
                stretch *= 1.2;
                drawMainElements();
            }

//            binds O to zoom out
            else if (keyEvent.getCode() == KeyCode.O) {
                stretch /= 1.2;
                drawMainElements();
            }

//            binds M to open Menu
            else if (keyEvent.getCode() == KeyCode.M)
                openMenuWindow();
        });

//        listener for keys pressed in Menu window
        menuScene.setOnKeyPressed(keyEvent -> {

//            binds X to cancel current action
            if (keyEvent.getCode() == KeyCode.X) {
                if (cancelTimelineButton.isVisible() || cancelEventButton.isVisible())
                    exitAddWindow();
                else if (confirmDeleteButton.isVisible())
                    exitDeleteWindow();
                else
                    openMainWindow();
            }

//            binds T to open new Timeline window or create Timeline (checks if Delete mode is on)
            else if (keyEvent.getCode() == KeyCode.T && exitButton.isVisible()) {
                if (cancelTimelineButton.isVisible())
                    addTimeline();
                else
                    openAddTimelineWindow();
            }

//            binds E to open new Event window or create Event (checks if Delete mode is on)
            else if (keyEvent.getCode() == KeyCode.E && exitButton.isVisible()) {
                if (cancelEventButton.isVisible())
                    addEvent();
                else
                    openAddEventWindow();
            }

//            binds D to toggle Delete mode or delete Elements
            else if (keyEvent.getCode() == KeyCode.D) {
                if (confirmDeleteButton.isVisible())
                    confirmDelete();
                else
                    openDeleteWindow();
            }

//            binds S to save
            else if (keyEvent.getCode() == KeyCode.S) {
                save();
            }
        });
    }

    /**
     * Switch from Menu to Main scene, changes messageLabel visibility
     * (used to check whether current Scene is Main or Menu)
     */

    @FXML
    void openMainWindow() {
        window.setTitle("Timelines");
        window.setScene(mainScene);
        window.show();
        window.requestFocus();
        drawMainElements();

        messageLabel.setVisible(false);
    }

    /**
     * Switch from Main to Menu scene, changes messageLabel visibility
     * (used to check whether current Scene is Main or Menu)
     */

    @FXML
    void openMenuWindow() {
        window.setTitle("Timelines: Menu");
        window.setScene(menuScene);
        window.show();
        window.requestFocus();

        messageLabel.setVisible(true);
    }

    /**
     * Draws all Timelines and Events in Main scene on ScrollPane (mainPane)
     */

    @FXML
    void drawMainElements() {

//        clears the current axes due to possible changes in Elements
        axes.clear();

//        temporary panes for Nodes, will be set as ScrollPane content
        Pane pane = new Pane();
        Pane pane1 = new Pane();
        ObservableList<Node> objects = pane.getChildren();
        ObservableList<Node> persistentObjects = pane1.getChildren();

//        values that shift position by biggest Label width and smallest Event date
        shift = 99999;

//        loop through all Timelines to create Axes
        for (Timeline timeline : app.timelines) {

//            checks if Timeline needs to be drawn
            if (!timeline.visible)
                continue;
            if (timeline.events.isEmpty())
                continue;

//            creates the Axis
            axes.add(new Axis(timeline));
        }

//        finds the screen shift (leftmost point of all visible Nodes)
        for (Axis axis : axes) {

//            if the edge of the Axis Label (its leftmost point) is smaller
//            than the current shift, it is replaced
            if ((axis.start * stretch) - axis.width() < shift)
                shift = (axis.start * stretch) - axis.width();
        }

        shift += mainPane.getLayoutX();
        shift -= 20;

//        draws all Axes and their persistentLabels
        for (Axis axis : axes) {
            axis.draw(objects, persistentObjects);
        }

//        adds all Nodes to their ScrollPanes
        mainPane.setContent(pane);
        persistentPane.setContent(pane1);
    }

    /**
     * Expands clicked Element
     *
     * in Main: creates a pop-up window with details
     * in Menu: creates the Edit screen
     *
     * @param   element Element that is expanded
     */

    @FXML
    void expandElement (Element element) {

//        expand request from Menu
        if (messageLabel.isVisible()) {

//            if Element is a Timeline, calls edit Timeline function
            if (element instanceof Timeline)
                openEditTimelineWindow((Timeline) element);

//            if Element is an Event, calls edit Event function
            else if (element instanceof Event)
                openEditEventWindow((Event) element);
        }

//        expand request from Main
        else {

//            create a new pop-up window
            Stage popUp = new Stage();
            popUp.setTitle("");
            popUp.getIcons().add(logo);

//            create a temporary VerticalBox to place all Nodes
            VBox content = new VBox();

//            Label with the name of the Element
            Label name = new Label(element.name);
            name.setStyle(stylesheet.toString());
            name.getStyleClass().add("nameLabel");

            content.getChildren().add(name);


//            if Element is a Timeline, list all Events
            if (element instanceof Timeline) {
                Label label;
                for (Event event : ((Timeline) element).events) {
                    label = new Label(event.name);
                    label.setStyle(stylesheet.toString());
                    label.getStyleClass().add("detailsLabel");
                    content.getChildren().add(label);
                }
            }

//            if Element is an Event, creates Labels for dates and notes
            else if (element instanceof Event) {
                Label date = new Label(
                        ((Event) element).startDate[0] + " " + getMonth(((Event) element).startDate[1]) + " " + ((Event) element).startDate[2] + " – " +
                        ((Event) element).endDate[0] + " " + getMonth(((Event) element).endDate[1]) + " " + ((Event) element).endDate[2]);
                date.setStyle(stylesheet.toString());
                date.getStyleClass().add("detailsLabel");

                Label notes = new Label(((Event) element).notes);
                notes.setStyle(stylesheet.toString());
                notes.getStyleClass().add("notesLabel");

                content.getChildren().addAll(date, notes);
            }

//            creates the pop-up
            Scene scene = new Scene(content);
            scene.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
            scene.getStylesheets().add(stylesheet.toString());

            popUp.setScene(scene);
            popUp.show();
        }
    }

    /**
     * Lists all elements in Menu as CheckBoxes
     *
     * @param deleteMode determines whether normal view or Delete mode is created
     */

    @FXML
    void drawMenuElements(boolean deleteMode) {

//        creates a temporary VerticalBox to place Nodes
        VBox timelineContent = new VBox();
        ObservableList<Node> timelineChildren = timelineContent.getChildren();
        Insets insets = new Insets(0, 0, 0, 40);

//        loops through all Timelines, creates a CheckBox for each Timeline
        for (Timeline timeline : app.timelines) {

            MenuBox timelineBox;
            if (deleteMode)
                timelineBox = new MenuBox(timeline, true);
            else
                timelineBox = new MenuBox(timeline);

//            creates a second-order VerticalBox for indented Event view
            VBox eventContent = new VBox();
            eventContent.setPadding(insets);
            ObservableList<Node> eventChildren = eventContent.getChildren();

//            loops through all Events of the current Timeline, creates Event CheckBoxes
            for (Event event : timeline.events) {

                MenuBox eventBox;
                if (deleteMode)
                    eventBox = new MenuBox(event, true);
                else
                    eventBox = new MenuBox(event);

                eventChildren.add(eventBox);
            }

//            adds the Timeline and Event box to the first-order VerticalBox
            timelineChildren.addAll(timelineBox, eventContent);
        }

//        overwrites the ScrollPane (menuPane) content with the new Elements
        menuPane.setContent(timelineContent);
    }

    /**
     * Creates a window to add a Timeline
     */

    @FXML
    void openAddTimelineWindow() {

//        replaces Element list with Add Timeline window
        menuPane.setContent(addTimelineScene.getRoot());

//        removes unnecessary buttons, shows cancel button
        toggleMenuButtonVisibility(false);
        cancelTimelineButton.setVisible(true);

        window.setTitle("Timelines: Add Timeline");
    }

    /**
     * Takes parameters from addTimelineWindow TextFields and creates a new Timeline
     */

    @FXML
    void addTimeline () {

//        loads data from TextField
        String name = timeline_nameField.getText().trim();

//        checks if the TextField has contents in it
        if (name.isEmpty()) {
            setMessage("Timeline creation failed: Invalid Timeline name", false);
            return;
        }

//        creates a Timeline
        app.timelines.add(new Timeline(name));

//        clears the TextFields
        timeline_nameField.setText("");

        setMessage("Successfully created Timeline: " + name, false);

//        re-lists the elements in Menu
        drawMenuElements(false);
        window.setTitle("Timelines: Menu");

        toggleMenuButtonVisibility(true);
        cancelTimelineButton.setVisible(false);
    }

    /**
     * Creates a window to edit a Timeline
     *
     * @param timeline infromation from Timeline is drawn in TextBoxes
     */

    @FXML
    void openEditTimelineWindow(Timeline timeline) {

//        shows the window for adding a Timeline
        openAddTimelineWindow();

//        fills the TextField with appropriate data
        timeline_nameField.setText(timeline.name);

//        switches add and edit Buttons and Labels
        timeline_addButton.setVisible(false);
        timeline_addLabel.setVisible(false);
        timeline_editButton.setVisible(true);
        timeline_editLabel.setVisible(true);

//        changes functions called when Buttons are clicked
        timeline_editButton.setOnAction(addEvent -> editTimeline(timeline));
        cancelTimelineButton.setOnAction(cancelEvent -> cancelTimelineEdit());

//        generates feedback for the user
        window.setTitle("Timelines: Edit Timelines");
        setMessage("Entered Timeline Edit mode.", false);
    }

    /**
     * Takes the parameters from addTimelineWindow, modifies the specified Timeline
     *
     * @param timeline  Timeline that will be modified
     */

    @FXML
    void editTimeline (Timeline timeline) {

//        changes the Timeline name
        timeline.name = timeline_nameField.getText().trim();

//        exits the add window
        cancelTimelineEdit();

        setMessage("Successfully edited Timeline.", false);
    }

    /**
     * Exits addTimelineWindow, reverts its Nodes from Edit to Add mode, draws Menu window
     */

    @FXML
    void cancelTimelineEdit () {

//        exits to Menu
        exitAddWindow();

//        changes Buttons and Labels from Edit to Add mode
        timeline_addButton.setVisible(true);
        timeline_addLabel.setVisible(true);
        timeline_editButton.setVisible(false);
        timeline_editLabel.setVisible(false);

//        re-binds the Button to Add window functionality
        cancelTimelineButton.setOnAction(cancelEvent -> exitAddWindow());

        setMessage("Exited Edit mode without any changes.", false);
    }

    /**
     * Creates a window to add an Event
     */

    @FXML
    void openAddEventWindow() {

//        replaces Element list with Add Timeline window
        menuPane.setContent(addEventScene.getRoot());

//        adds Timelines to timeline ChoiceBox
        timelineChoice.getItems().clear();
        for (Timeline timeline : app.timelines)
            timelineChoice.getItems().add(timeline.name);

//        removes unnecessary buttons, shows cancel button
        toggleMenuButtonVisibility(false);
        cancelEventButton.setVisible(true);

        window.setTitle("Timelines: Add Event");
    }

    /**
     * Takes parameters from addEventWindow TextFields and creates a new Event
     */

    @FXML
    void addEvent () {

//        checks if ChoiceBox has a selected Timeline
        if (timelineChoice.getSelectionModel().getSelectedItem() == null)
            return;

//        loads data from ChoiceBox and TextField(s)
        try {

            boolean singleDate = false;

            Timeline timeline = app.timelines.get(timelineChoice.getSelectionModel().getSelectedIndex());

            String name = event_nameField.getText().trim();
            String startDay = event_startDayField.getText().trim();
            String startMonth = event_startMonthField.getText().trim();
            String startYear = event_startYearField.getText().trim();
            String endDay = event_endDayField.getText().trim();
            String endMonth = event_endMonthField.getText().trim();
            String endYear = event_endYearField.getText().trim();
            String notes = event_notesField.getText().trim();

//        checks if all TextFields have correct contents in them
            if (name.isEmpty()) {
                setMessage("Event creation failed: Invalid Event name", false);
                return;
            }

            if (startDay.isEmpty() || startDay.length() > 2 || !startDay.matches("[0-9]+") || Integer.parseInt(startDay) > 31) {
                setMessage("Event creation failed: Invalid Event start day", false);
                return;
            }

            if (startMonth.isEmpty() || startMonth.length() > 2 || !startMonth.matches("[0-9]+") || Integer.parseInt(startMonth) > 12) {
                setMessage("Event creation failed: Invalid Event start month", false);
                return;
            }

            if (startYear.isEmpty() || !startYear.matches("-?\\d+")) {
                setMessage("Event creation failed: Invalid Event start year", false);
                return;
            }

//            if all end date fields are empty, start date values are copied
            if (endDay.isEmpty() && endMonth.isEmpty() && endYear.isEmpty()) {

                singleDate = true;

            } else {

                if (endDay.isEmpty() || endDay.length() > 2 || !endDay.matches("[0-9]+") || Integer.parseInt(endDay) > 31) {
                    setMessage("Event creation failed: Invalid Event end day", false);
                    return;
                }

                if (endMonth.isEmpty() || endMonth.length() > 2 || !endMonth.matches("[0-9]+") || Integer.parseInt(endMonth) > 12) {
                    setMessage("Event creation failed: Invalid Event end month", false);
                    return;
                }

                if (endYear.isEmpty() || !endYear.matches("-?\\d+")) {
                    setMessage("Event creation failed: Invalid Event end year", false);
                    return;
                }
            }

            //        creates start date
            int[] startDate = new int[]{
                    Integer.parseInt(startDay),
                    Integer.parseInt(startMonth),
                    Integer.parseInt(startYear)};

            if (singleDate) {

//                adds Event to selected Timeline with single-date mode
                timeline.addEvent(name, startDate, startDate, false, notes);

            } else {

//                creates end date
                int[] endDate = new int[]{
                        Integer.parseInt(endDay),
                        Integer.parseInt(endMonth),
                        Integer.parseInt(endYear)};

//                checks whether endDate is later than startDate
                if (!timeline.wasSooner(startDate, endDate)) {
                    setMessage("Event creation failed: End Date is sooner than Start Date", false);
                    return;
                }

//                adds Event to selected Timeline with both start- and end-date
                timeline.addEvent(name, startDate, endDate, false, notes);
            }

//            exits the Add window, loads Menu
            cancelEventEdit();

            setMessage("Successfully created Event: " + name, false);

        } catch (Exception e) {
            setMessage("Event creation failed: Invalid Timeline", false);
        }
    }

    /**
     * Creates a window to edit an Event
     *
     * @param event infromation from Event is drawn in TextBoxes and ChoiceBox
     */

    @FXML
    void openEditEventWindow(Event event) {

//        shows the window for adding an Event
        openAddEventWindow();

//        fills the TextFields and ChoiceBox with appropriate data
        timelineChoice.getSelectionModel().select(app.timelines.indexOf(event.timeline));
        event_nameField.setText(event.name);
        event_startDayField.setText(Integer.toString(event.startDate[0]));
        event_startMonthField.setText(Integer.toString(event.startDate[1]));
        event_startYearField.setText(Integer.toString(event.startDate[2]));
        event_endDayField.setText(Integer.toString(event.endDate[0]));
        event_endMonthField.setText(Integer.toString(event.endDate[1]));
        event_endYearField.setText(Integer.toString(event.endDate[2]));
        event_notesField.setText(event.notes);

//        switches add and edit Buttons and Labels
        event_addButton.setVisible(false);
        event_addLabel.setVisible(false);
        event_editButton.setVisible(true);
        event_editLabel.setVisible(true);

//        changes functions called when Buttons are clicked
        event_editButton.setOnAction(addEvent -> editEvent(event));
        cancelEventButton.setOnAction(cancelEvent -> cancelTimelineEdit());

//        generates feedback for the user
        window.setTitle("Timelines: Edit Event");
        setMessage("Entered Event Edit mode.", false);
    }

    /**
     * Takes the parameters from addEventWindow, modifies the specified Event
     *
     * @param event  Event that will be modified
     */

    @FXML
    void editEvent (Event event) {

//        removes the event and then creates a new one
//        (ensures Events stay sorted chronologically even if dates are changed)
        event.timeline.events.remove(event);
        addEvent();

//        exits the add window
        cancelEventEdit();

        setMessage("Successfully edited Event.", false);
    }

    /**
     * Exits addEventWindow, reverts its Nodes from Edit to Add mode, draws Menu window
     */

    @FXML
    void cancelEventEdit () {

//        exits to Menu
        exitAddWindow();

//        changes Buttons and Labels from Edit to Add mode
        event_addButton.setVisible(true);
        event_addLabel.setVisible(true);
        event_editButton.setVisible(false);
        event_editLabel.setVisible(false);

//        re-binds the Button to Add window functionality
        cancelEventButton.setOnAction(cancelEvent -> exitAddWindow());

        setMessage("Exited Edit mode without any changes.", false);
    }

    /**
     * Exits from any Add window into Menu, draws Menu elements,
     * reverts Buttons to Menu setup; does not create Elements
     */

    @FXML
    void exitAddWindow() {
        drawMenuElements(false);
        window.setTitle("Timelines: Menu");

        toggleMenuButtonVisibility(true);
        cancelTimelineButton.setVisible(false);
        cancelEventButton.setVisible(false);

        resetAddWindowFields();
    }

    /**
     * Resets all TextFields and ChoiceBoxes in addTimelineWindow and addEventWindow
     */

    @FXML
    void resetAddWindowFields() {
        timeline_nameField.setText("");

        timelineChoice.getSelectionModel().clearSelection();
        event_nameField.setText("");
        event_startDayField.setText("");
        event_startMonthField.setText("");
        event_startYearField.setText("");
        event_endDayField.setText("");
        event_endMonthField.setText("");
        event_endYearField.setText("");
        event_notesField.setText("");
    }

    /**
     * Saves all Timelines and Events by running the app.saveToFile() function
     */

    @FXML
    void save () {
        try {
            String message = app.saveToFile();
            setMessage(message, false);
        } catch (Exception exception) {
            showError("saving to file", exception.toString());
        }
    }

    /**
     * Toggles on Delete mode: lists all Elements in Delete mode,
     * changes Button set from Menu to Delete
     */

    @FXML
    void openDeleteWindow() {

//        clears the list of all Elements to be deleted
        app.deleteElements.clear();

//        lists all Elements in Delete Mode
        drawMenuElements(true);

//        removes all unnecessary buttons, adds confirm and cancel buttons for delete
        toggleMenuButtonVisibility(false);
        toggleDeleteButtonVisibility(true);

//        creates a message for the user
        setMessage("Entered Delete Mode", false);
        window.setTitle("Timelines: Delete Mode");
    }

    /**
     * Exits Delete mode and removes selected Elements (all Elements in app.deleteElements)
     */

    @FXML
    void confirmDelete () {

//        cycles through all Elements to be deleted
        for (Element element : app.deleteElements) {

//            if it is a Timeline, it removes it
            if (element instanceof Timeline) {
                app.timelines.remove(element);
            }

//            if it is an Event, it finds the relevant Timeline, then removes the Event
            if (element instanceof Event) {
                for (Timeline timeline : app.timelines) {
                    if (timeline == ((Event) element).timeline) {
                        timeline.events.remove(element);
                    }
                }
            }
        }

        exitDeleteWindow();

//        creates a message for the user
        setMessage("Exited Delete Mode: " + app.deleteElements.size() + " elements deleted", false);
    }

    /**
     * Exits Delete mode without removing any Elements
     */

    @FXML
    void exitDeleteWindow() {

//        lists Elements in default mode
        drawMenuElements(false);

//        adds all main buttons, removes confirm and cancel buttons for delete
        toggleMenuButtonVisibility(true);
        toggleDeleteButtonVisibility(false);

//        creates a message for the user
        setMessage("Exited Delete Mode without deleting elements", false);
        window.setTitle("Timelines: Menu");
    }

    /**
     * Changes the visibility of Menu Button set
     *
     * @param visible   new Visible value
     */

    void toggleMenuButtonVisibility(boolean visible) {
        exitButton.setVisible(visible);
        addTimelineButton.setVisible(visible);
        addEventButton.setVisible(visible);
        saveButton.setVisible(visible);
        deleteButton.setVisible(visible);
    }

    /**
     * Changes the visibility of Delete Button set
     *
     * @param visible   new Visible value
     */

    void toggleDeleteButtonVisibility (boolean visible) {
        confirmDeleteButton.setVisible(visible);
        cancelDeleteButton.setVisible(visible);
    }

    /**
     * Sets the message on messageLabel, primary feedback for user
     *
     * @param message   contents of the message
     */

    void setMessage (String message, boolean warning) {
        String time = String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis());
        if (warning) {
            messageLabel.setStyle("-fx-text-fill: rgba(160, 0, 0, 0.8);");
            messageLabel.setText("[" + time + "]\n" + "WARNING: " + message);
        }
        else {
            messageLabel.setStyle("-fx-text-fill: #777777");
            messageLabel.setText("[" + time + "]\n" + message);
        }
    }

    /**
     * Creates an error pop-up window
     *
     * @param occurredWhile     Describes when the error occured
     * @param error             The actual error message
     */

    void showError (String occurredWhile, String error) {

//        creates the VerticalBox for all elements
        VBox pane = new VBox();

//        creates all necessary labels, adds them to errPane
        Label genLabel = new Label("Error occured while " + occurredWhile + ":");
        Label errLabel = new Label(error);
        genLabel.setStyle("-fx-padding: 10, 10, 10, 10;");
        errLabel.setStyle("-fx-padding: 10, 10, 10, 10;");
        pane.getChildren().addAll(genLabel, errLabel);

//        creates a Scene, applies stylesheets
        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
        scene.getStylesheets().add(stylesheet.toString());

//        creates a new errorWindow
        errorWindow = new Stage();
        errorWindow.setTitle("Error");
        errorWindow.setResizable(false);
        errorWindow.setScene(scene);
        errorWindow.show();
    }

    /**
     * Formats months from Integers to String
     *
     * @param month     value of the month
     * @return          name of the month if (1 ≤ month ≤ 12), otherwise returns month
     */

    String getMonth (int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return Integer.toString(month);
        }
    }

    /**
     * Launches start() function on start-up
     */

    public static void main (String[] args) {
        launch(args);
    }
}