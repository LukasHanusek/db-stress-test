/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.gui;

import dbstresstest.data.objects.DbCon;
import dbstresstest.data.objects.managers.ConManager;
import dbstresstest.gui.mainwindow.DatabaseTypeConverter;
import dbstresstest.gui.utils.GUI;
import dbstresstest.plugins.DatabasePlugin;
import dbstresstest.plugins.PluginLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Dakado
 */
public class ImportConController implements Initializable {
    
    private Stage stage;
    private GUI gui;
    
    
    public void setup(Stage stage, GUI gui) {
        this.stage = stage;
        this.gui = gui;
    }
    
    @FXML
    private PasswordField defaultPassword;
    
    @FXML
    private TextField filePath, defaultUser, instance, defaultDatabase;
    
    @FXML
    private ComboBox dbmsType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDatabasePluginsDropdown();
    }
    
    
    @FXML
    public void browse() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            filePath.setText(file.getAbsolutePath());
        } else {
            gui.openWarningWindow("File not found, please make sure that the application has access to the given directory / file.", "File not found!");
        }
    }
    
    
    @FXML
    public void cancel() {
        stage.close();
    }
    
    
    @FXML
    public void importCons() {
        if (defaultUser.getText().length() < 1) {
            gui.openWarningWindow("Please specify default user that will be used for all imported connections.", "Input error!");
            return;
        }
        if (defaultPassword.getText().length() < 1) {
            gui.openWarningWindow("Please specify default password that will be used for all imported connections.", "Input error!");
            return;
        }
        if (defaultDatabase.getText().length() < 1) {
            gui.openWarningWindow("Please specify default database that will be used for all imported connections.", "Input error!");
            return;
        }
        DatabasePlugin dbpl;
        try {
            dbpl = (DatabasePlugin) this.dbmsType.getSelectionModel().getSelectedItem();
        } catch (NullPointerException e) {
            gui.openWarningWindow("No database type selected, if no options are given, it means that you have no database plugins loaded.", "Database type is requiered!");
            return;
        }
        
        File file = new File(filePath.getText());
        if (!file.exists()) {
            gui.openWarningWindow("File not found, please make sure that the application has access to the given directory / file.", "Input error!");
            return;
        }
        
        int imported = 0;
        long time = System.currentTimeMillis();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) { 
                DbCon con = new DbCon();
                if (instance.getText().length() > 0) {
                    con.setAddress(line + "\\\\" + instance.getText());
                } else {
                   con.setAddress(line); 
                }
                //if con name is not unique make it unique
                if (ConManager.getInstance().getConByName(line) != null) {
                    con.setCustomName(line + time);
                } else {
                    con.setCustomName(line);
                }
                con.setId(time);
                con.setDatabaseName(defaultDatabase.getText());
                con.setDecodedPassword(defaultPassword.getText());
                con.setPort(0);
                con.setUser(defaultUser.getText());
                con.setDatabaseType(dbpl.getName());
                
                ConManager.getInstance().addConnection(con);
                time++;
                line = br.readLine();
                imported++;
            }
            ConManager.getInstance().save();
            ConManager.reload();
        } catch (Exception e) {
            gui.openExceptionWindow(e);
        }
        gui.openInfoWindow("Success!", "Successfully imported " + imported + " connections!");
        stage.close();
    }
    
    private void setupDatabasePluginsDropdown() {
        DatabaseTypeConverter dbc = new DatabaseTypeConverter();
        dbmsType.setItems(FXCollections.observableList(PluginLoader.getInstance().getPlugins()));
        dbmsType.setConverter(dbc);
    }
    
    
}
