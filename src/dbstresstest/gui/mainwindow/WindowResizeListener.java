/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.gui.mainwindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

/**
 *
 * @author Lukas Hanusek
 */
public class WindowResizeListener {

    /**
     * SplitPanes keep the same % of screen accupatin during resize
     */
    public void setup(Stage stage, SplitPane... panes) {
        //on maximize listener - change the sidebar splitpane size, because when resizing the window, the % size it not kept
        stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                for (SplitPane sp : panes) {
                    sp.setDividerPositions(0.2291);
                }
            }
        });
        //on minimize listener - change the sidebar splitpane size, because when resizing the window, the % size it not kept
        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                for (SplitPane sp : panes) {
                    sp.setDividerPositions(0.2291);
                }
            }
        });
    }

}
