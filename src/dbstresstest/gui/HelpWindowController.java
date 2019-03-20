/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

/**
 *
 * @author Lukas Hanusek
 */
public class HelpWindowController implements Initializable {

    @FXML
    private WebView web;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            web.getEngine().load(Paths.get("help/index.html").toUri().toURL().toExternalForm());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public void setURL(String url) {
        web.getEngine().load(url);
    }

    /**
     * Taken from https://stackoverflow.com/questions/18928333/how-to-program-back-and-forward-buttons-in-javafx-with-webview-and-webengine
     */
    @FXML
    public void goBack() {
        final WebHistory history = web.getEngine().getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(()
                -> {
            history.go(entryList.size() > 1
                    && currentIndex > 0
                            ? -1
                            : 0);
        });
    }

    /**
     * Taken from https://stackoverflow.com/questions/18928333/how-to-program-back-and-forward-buttons-in-javafx-with-webview-and-webengine
     */
    @FXML
    public void goForward() {
        final WebHistory history = web.getEngine().getHistory();
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(()
                -> {
            history.go(entryList.size() > 1
                    && currentIndex < entryList.size() - 1
                    ? 1
                    : 0);
        });
    }

}
