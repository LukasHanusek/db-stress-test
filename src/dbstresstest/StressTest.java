package dbstresstest;

import dbstresstest.data.objects.managers.CharEnumManager;
import dbstresstest.gui.MainWindowController;
import dbstresstest.gui.utils.GUI;
import dbstresstest.data.objects.managers.ConManager;
import dbstresstest.plugins.PluginLoader;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Lukas Hanusek
 */
public class StressTest extends Application {

    private Stage stage;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        PluginLoader.getInstance().loadPlugins();
        ConManager.getInstance(); //this must be loaded before any GUI
        CharEnumManager.getInstance();
        //main window
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dbstresstest/gui/fxml/MainWindow.fxml"));
        Parent root = (Parent)loader.load();
        MainWindowController controller = (MainWindowController)loader.getController();
        controller.setupListeners(stage);
        Scene scene = new Scene(root);
        stage.setTitle("Database Stress Tester");
        stage.getIcons().add(new Image("file:res/img/ST.png"));
        stage.setScene(scene);
        stage.show();
        
        //make sure all threads are closed on exit
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (GUI.getInstance().openConfrmWindow("Are you sure ?", "Closing this window will stop all running tasks and any unsaved data will be lost!")) {
                   System.exit(0); 
                } else {
                    event.consume();
                }
            }
        });
    }

    /**
     * DO NOT TOUCH THIS ALL LOADING OF RESOURCES NEEDS TO BE DONE IN START METHOD NOT HERE
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
