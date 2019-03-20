/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.gui.mainwindow;

import dbstresstest.data.objects.managers.ConManager;
import dbstresstest.gui.MainWindowController;
import dbstresstest.gui.utils.GUI;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

/**
 *
 * @author Lukas Hanusek
 */
public class DragDropDB extends MainWindowController {
    
    ListView from, to;
    
    public static final String DEFAULT_ITEM = "Drag and drop connections here...";

    public DragDropDB(MainWindowController mw, ListView from, ListView to) {
        this.from = from;
        this.to = to;
        setup(mw);
    }

    private void setup(MainWindowController mw) {
        //bug in JDK 8.0 if list is empty you cannot do drag and drop
        to.getItems().add(DEFAULT_ITEM);
        //start drag from available tasks
        from.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                ListCell<String> listCell = new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                        } else {
                            setText("");
                        }
                    }
                };
                //Drag start
                listCell.setOnDragDetected((MouseEvent event) -> {
                    //System.out.println("listcell setOnDragDetected");
                    if (to.getItems().isEmpty()) to.getItems().add(DEFAULT_ITEM);
                    Dragboard db = listCell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(listCell.getItem()); //put the item name in clipboard contents
                    db.setContent(content);
                    event.consume();
                });
                return listCell;
            }
        });
        to.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> stringTreeView) {
                ListCell<String> treeCell = new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                        } else {
                            setText("");
                        }
                    }
                };
                treeCell.setOnDragEntered((DragEvent event) -> {
                    treeCell.setStyle("-fx-background-color: aqua;");
                });

                treeCell.setOnDragExited((DragEvent event) -> {
                    treeCell.setStyle("");
                });
                treeCell.setOnDragOver((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                    event.consume();
                });
                //Dropped drag
                treeCell.setOnDragDropped((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        //System.out.println("Dropped: " + db.getString());
                        //remove default item that has to be in the ListView otherwise Drag n' Drop won't work
                        if (to.getItems().contains(DEFAULT_ITEM)) to.getItems().remove(DEFAULT_ITEM);
                        
                        String database = db.getString();
                        
                        if (ConManager.getInstance().getConByName(database) == null) {
                            GUI.getInstance().openWarningWindow("This item is not a connection!", "Invalid entry!");
                            return;
                        }
                        
                        if (to.getItems().contains(database)) {
                            GUI.getInstance().openWarningWindow("Task already contains this connection!", "Duplicate entry!");
                            return;
                        }
                        to.getItems().add(database);
                        success = true;
                        if (mw.editedTask != null) mw.editedTask.modified = true;
                        mw.saveTaskToMemory();
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                return treeCell;
            }
        });
    }

    
}
