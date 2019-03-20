package dbstresstest.gui.mainwindow;

import dbstresstest.data.objects.DbCon;
import dbstresstest.data.objects.SQLSet;
import dbstresstest.data.objects.Task;
import dbstresstest.data.objects.managers.ConManager;
import dbstresstest.data.objects.managers.SetManager;
import dbstresstest.data.objects.managers.TaskManager;
import dbstresstest.gui.MainWindowController;
import dbstresstest.gui.utils.GUI;
import dbstresstest.util.exceptions.SetNotFoundException;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Lukas Hanusek
 */
public class ContextMenus {
    
    /**
     * 
     * @param gui
     * @param item 
     */
    private void connectionDeleteConfirm(String item) {
        DbCon con = ConManager.getInstance().getConByName(item);
        if (GUI.getInstance().openConfrmWindow("Do you really want to remove this connection ?", item)) {
            ConManager.getInstance().removeConnection(con);
        }
    }
    
    /**
     * Menus for connect ListView
     * @param mw
     * @param connectionList
     * @param gui 
     */
    public void setupConnectionMenu(MainWindowController mw, ListView connectionList) {
        //On Delete key press - craft confirm delete menu
        connectionList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {
                final String item = (String)connectionList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                        connectionDeleteConfirm(item);
                    }
                }
            }
        });
        
        //right click context menus
        connectionList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            //RIGHT CLICK MENU EDIT CONNECTION
            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Edit", cell.itemProperty()));
            editItem.setOnAction(event -> {
                mw.editConnection(cell.getItem());
            });

            //RIGHT CLICK MENU DELETE CONNECTION
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                String item = cell.getItem();
                connectionDeleteConfirm(item);
            });
            
            //ADD MENUS
            contextMenu.getItems().addAll(editItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }
    
    /**
     * 
     * @param gui
     * @param item 
     */
    private void setDeleteConfirm(String item) {
        SQLSet set;
        try {
            set = SetManager.getInstance().getSetByName(item);
        } catch (SetNotFoundException ex) {
            GUI.getInstance().openExceptionWindow(ex);
            return;
        }
        if (GUI.getInstance().openConfrmWindow("Do you really want to remove this set ?", item)) {
            if (!SetManager.getInstance().removeSet(set)) {
                GUI.getInstance().openWarningWindow("Could not find the set file.", "Something went wrong!");
            }
        }
    }
    
    /**
     * Menus for Sets ListView
     * @param mw
     * @param setList
     * @param gui 
     */
    public void setupSetMenu(MainWindowController mw, ListView setList) {
        //On Delete key press - craft confirm delete menu
        setList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {
                final String item = (String)setList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                        setDeleteConfirm(item);
                    }
                }
            }
        });
        //right click context menu for sets
        setList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            //RIGHT CLICK MENU EDIT SET
            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Edit", cell.itemProperty()));
            editItem.setOnAction(event -> {
                mw.editSet(cell.getItem());
            });

            //RIGHT CLICK MENU DELETE SET
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                String item = cell.getItem();
                setDeleteConfirm(item);
            });
            
            //ADD MENUS
            contextMenu.getItems().addAll(editItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }
    
    /**
     * 
     * @param gui
     * @param item 
     */
    private void taskDeleteConfirm(String item) {
        Task task = TaskManager.getInstance().getTaskByName(item);
        if (GUI.getInstance().openConfrmWindow("Do you really want to remove this task ?", item)) {
            if (!TaskManager.getInstance().removeTask(task)) {
                GUI.getInstance().openWarningWindow("Could not find the task file.", "Something went wrong!");
            }
        }
    }
    
    /**
     * 
     * @param mw
     * @param taskList
     * @param gui 
     */
    public void setupTaskMenu(MainWindowController mw, ListView taskList, ListView taskSets, ListView taskCons) {
        //delete connection from currently edited task
        taskCons.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {
                if (taskCons.getSelectionModel().getSelectedItem() != null) {
                    if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                        taskCons.getItems().removeAll(taskCons.getSelectionModel().getSelectedItems());
                        mw.saveTaskToMemory();
                    }
                }
            }
        });
        //delete set from currently edited task
        taskSets.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {
                if (taskSets.getSelectionModel().getSelectedItem() != null) {
                    if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                        taskSets.getItems().removeAll(taskSets.getSelectionModel().getSelectedItems());
                        mw.saveTaskToMemory();
                    }
                }
            }
        });
        //On delete key press - craft task delete confirm menu
        taskList.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {
                final String item = (String)taskList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                        taskDeleteConfirm(item);
                    }
                }
            }
        });
        //right click context menus
        taskList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            //RIGHT CLICK MENU EDIT SET
            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Edit", cell.itemProperty()));
            editItem.setOnAction(event -> {
                mw.editTask(cell.getItem());
            });

            //RIGHT CLICK MENU DELETE SET
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                String item = cell.getItem();
                taskDeleteConfirm(item);
            });
            
            //ADD MENUS
            contextMenu.getItems().addAll(editItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }
    
}
