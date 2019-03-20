package dbstresstest.gui.utils;

import dbstresstest.data.objects.CharEnum;
import dbstresstest.data.objects.managers.CharEnumManager;
import dbstresstest.gui.HelpWindowController;
import dbstresstest.gui.ImportConController;
import dbstresstest.gui.RunningTaskController;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Lukas Hanusek
 */
public class GUI {
    
    private static GUI gui;
    
    public static GUI getInstance() {
        if (gui == null) gui = new GUI();
        return gui;
    }
        
    /**
     * Load FXML from the given file
     * @param fxml
     * @return
     * @throws Exception 
     */
    public Parent load(String fxml) throws Exception {
        return FXMLLoader.load(getClass().getResource("/dbstresstest/gui/fxml/" + fxml + ".fxml"));
    }
    
    /**
     * Creates a new windows from the given fxml source and titles it based on the "title" param
     * @param fxml FXML source
     * @param name Window title
     */
     public void openWindow(String fxml, String title) {
        Parent root;
        try {
            root = load(fxml);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     
    public RunningTaskController openRunningTaskWindow(String title) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dbstresstest/gui/fxml/RunningTask.fxml"));
            Parent root = (Parent) loader.load();
            RunningTaskController controller = (RunningTaskController) loader.getController();
            controller.setup(stage, this);
            Scene scene = new Scene(root);
            stage.setTitle("Task Monitor - " + title);
            stage.getIcons().add(new Image("file:res/img/ST.png"));
            stage.setScene(scene);
            stage.show();
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public ImportConController openImportWindow() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dbstresstest/gui/fxml/ImportConnections.fxml"));
            Parent root = (Parent) loader.load();
            ImportConController controller = (ImportConController) loader.getController();
            controller.setup(stage, this);
            Scene scene = new Scene(root);
            stage.setTitle("Import connections...");
            stage.getIcons().add(new Image("file:res/img/ST.png"));
            stage.setScene(scene);
            stage.show();
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public HelpWindowController openHelpWindow() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dbstresstest/gui/fxml/HelpWindow.fxml"));
            Parent root = (Parent) loader.load();
            HelpWindowController controller = (HelpWindowController) loader.getController();
            Scene scene = new Scene(root);
            stage.setTitle("Help pages");
            stage.getIcons().add(new Image("file:res/img/ST.png"));
            stage.setScene(scene);
            stage.show();
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openWarningWindow(String message, String title) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }
    
    public void openExceptionWindow(Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception!");
        alert.setHeaderText(e.getClass().getName());
        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }
    
    public void openInfoWindow(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Info");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }
    
    public boolean openConfrmWindow(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

}
