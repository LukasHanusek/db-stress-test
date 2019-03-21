package dbstresstest.gui;

import dbstresstest.gui.mainwindow.Tabs;
import dbstresstest.data.objects.DbCon;
import dbstresstest.data.objects.SQLSet;
import dbstresstest.data.objects.Task;
import dbstresstest.data.objects.managers.CharEnumManager;
import dbstresstest.data.objects.managers.ConManager;
import dbstresstest.data.objects.managers.ManagerListener;
import dbstresstest.data.objects.managers.SetManager;
import dbstresstest.data.objects.managers.TaskManager;
import dbstresstest.logic.variables.VariableParser;
import dbstresstest.gui.mainwindow.ContextMenus;
import dbstresstest.gui.mainwindow.DatabaseTypeConverter;
import dbstresstest.gui.mainwindow.DragDropDB;
import dbstresstest.gui.mainwindow.DragDropSQL;
import dbstresstest.gui.mainwindow.KeyShortcuts;
import dbstresstest.gui.mainwindow.WindowResizeListener;
import dbstresstest.gui.utils.GUI;
import dbstresstest.plugins.DatabasePlugin;
import dbstresstest.plugins.DbPool;
import dbstresstest.plugins.ParseError;
import dbstresstest.plugins.PluginLoader;
import dbstresstest.logic.runnable.RunnableTask;
import dbstresstest.logic.runnable.RunnableTaskCreator;
import dbstresstest.util.exceptions.ConnectionNameNotUniqueException;
import dbstresstest.util.exceptions.ConnectionNotFoundException;
import dbstresstest.util.exceptions.SetNameNotUniqueException;
import dbstresstest.util.exceptions.SetNotFoundException;
import dbstresstest.util.exceptions.TaskNameNotUniqueException;
import dbstresstest.util.exceptions.VariableException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 *
 * @author Lukas Hanusek
 */
public class MainWindowController implements Initializable {

    private Stage stage;
    private GUI gui; //only for easyUML
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab dbTab, taskTab, sqlTab;

    @FXML
    private SplitPane setPane, dbPane, taskPane;

    /*
    Database connection GUI elements
     */
    @FXML
    private TextField customName, dbAddress, port, user, password, databaseName;
    @FXML
    private ComboBox databaseType;
    @FXML
    private ListView connectionList;
    @FXML
    private Button saveConnection, cancelConnection;
    protected DbCon editedCon;
    @FXML
    private Label conWarning;
    
    
    /*
    SQL Sets GUI elements
    */
    @FXML
    private ComboBox databaseTypeSets;
    @FXML
    private TextArea variables;
    @FXML
    private CheckBox parametrized;
    @FXML
    private RadioButton query, call, update;
    @FXML
    private Button saveSet, clearSet;
    @FXML
    private ListView setList;
    @FXML
    private TextField setName, paramDelimiter, lineDelimiter, timeout;
    @FXML
    private TextFlow errorlog;
    @FXML
    private BorderPane codeRoot;
    @FXML
    private Label setWarning;
    @FXML
    private CodeArea sql;
    protected SQLSet editedSet;
    
    
    /*
    Tasks elements
    */
    @FXML
    private ListView taskList, taskSets, availableSets, availableDbs, taskCons;
    @FXML
    private TextField taskName, poolSize, repeatCount, interval;
    @FXML
    private Button saveTask, runTask;
    @FXML
    private Label taskWarning;
    public Task editedTask;
    
    
    
    
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //load connections into ListView
        this.updateConnectionList(ConManager.getInstance().getConnections());
        //load Sets into ListView
        this.updateSetList(SetManager.getInstance().getSets());
        //load tasks into ListView
        this.updateTaskList(TaskManager.getInstance().getTasks());
        
        sql.setParagraphGraphicFactory(LineNumberFactory.get(sql));
        
        availableSets.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableDbs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        taskCons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        taskSets.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    @FXML
    public void querySelected() {
        call.setSelected(false);
        update.setSelected(false);
    }
    
    @FXML
    public void callSelected() {
        query.setSelected(false);
        update.setSelected(false);
    }
    
    @FXML
    public void updateSelected() {
        query.setSelected(false);
        call.setSelected(false);
    }

    /**
     * Called from Main (need to pass Stage object)
     * @param stage
     * @param gui 
     */
    public void setupListeners(Stage stage) {
        this.stage = stage;
        setupContextMenuForDBList();
        WindowResizeListener resizer = new WindowResizeListener();
        resizer.setup(stage, dbPane, setPane, taskPane);
        setupActionListeners();
        setupDatabasePluginsDropdown();
        //setup drag & drop menu for sets-tasks
        new DragDropSQL(this, availableSets, taskSets); 
        new DragDropDB(this, availableDbs, taskCons);
    }

    /**
     * Action listeners
     */
    private void setupActionListeners() {
        ConManager.getInstance().addListener(new ManagerListener<DbCon>() {
            @Override
            public void onListChanged(LinkedList<DbCon> connections) {
                updateConnectionList(connections);
            }
        });
        SetManager.getInstance().addListener(new ManagerListener<SQLSet>() {
            @Override
            public void onListChanged(LinkedList<SQLSet> sets) {
                updateSetList(sets);
            }
        });
        TaskManager.getInstance().addListener(new ManagerListener<Task>() {
            @Override
            public void onListChanged(LinkedList<Task> tasks) {
                updateTaskList(tasks);
            }
        });
    }

    private void updateConnectionList(LinkedList<DbCon> connections) {
        connectionList.getItems().clear();
        availableDbs.getItems().clear();
        for (DbCon con : connections) {
            connectionList.getItems().add(con.getCustomName());
            availableDbs.getItems().add(con.getCustomName());
        }
    }

    private void updateSetList(LinkedList<SQLSet> sets) {
        setList.getItems().clear();
        availableSets.getItems().clear();
        for (SQLSet set : sets) {
            setList.getItems().add(set.getName());
            availableSets.getItems().add(set.getName());
        }
    }
    
    private void updateTaskList(LinkedList<Task> tasks) {
        taskList.getItems().clear();
        for (Task task : tasks) {
            taskList.getItems().add(task.getTaskName());
        }
    }

    /**
     * Based on loaded plugins setup dropdown menu for databse connection editor
     */
    private void setupDatabasePluginsDropdown() {
        DatabaseTypeConverter dbc = new DatabaseTypeConverter();
        databaseTypeSets.setItems(FXCollections.observableList(PluginLoader.getInstance().getPlugins()));
        databaseType.setItems(FXCollections.observableList(PluginLoader.getInstance().getPlugins()));
        databaseType.setConverter(dbc);
        databaseTypeSets.setConverter(dbc);
    }

    /**
     * Setups "Edit" and "Delete" context menu for saved connections
     */
    private void setupContextMenuForDBList() {
        ContextMenus cm = new ContextMenus();
        cm.setupConnectionMenu(this, connectionList);
        cm.setupSetMenu(this, setList);
        cm.setupTaskMenu(this, taskList, taskSets, taskCons);
    }

    public void editConnection(String item) {
        DbCon selected = ConManager.getInstance().getConByName(item);
        if (selected == null) {
            GUI.getInstance().openWarningWindow("Item cannot be edited!", "Not found!");
            return;
        }
        this.customName.setText(selected.getCustomName());
        try {
            DatabasePlugin dbpl = PluginLoader.getInstance().getPluginByName(selected.getDatabaseType());
            if (dbpl == null) {
                GUI.getInstance().openWarningWindow("Database plugin '" + selected.getDatabaseType() + "' not found!", "This connection type is no longer supported!");
                return;
            }
            this.databaseType.setValue(dbpl);
        } catch (Exception e) {
            GUI.getInstance().openWarningWindow("Database plugin '" + selected.getDatabaseType() + "' not found!", "This connection type is no longer supported!");
            return;
        }
        editedCon = selected;
        this.port.setText(selected.getPort() + "");
        this.password.setText(selected.getDecodedPassword());
        this.user.setText(selected.getUser());
        this.dbAddress.setText(selected.getAddress());
        this.databaseName.setText(selected.getDatabaseName()); 
    }
    
    public void editSet(String item) {
        SQLSet selected;
        try {
            selected = SetManager.getInstance().getSetByName(item);
        } catch (SetNotFoundException ex) {
            GUI.getInstance().openWarningWindow("Item cannot be edited!", "Not found!");
            return;
        }
        
        this.setName.setText(selected.getName());
        DatabasePlugin dbpl = PluginLoader.getInstance().getPluginByName(selected.getDatabaseType());
        if (dbpl == null) {
            GUI.getInstance().openWarningWindow("Database plugin '" + selected.getDatabaseType() + "' not found!", "This connection type is no longer supported!");
            cancelConnectionEdit();
            return;
        }
        editedSet = selected;
        this.databaseTypeSets.setValue(dbpl);
        this.sql.clear();
        this.sql.appendText(selected.getSql());
        this.variables.setText(VariableParser.getVariablesTextFormat(selected));
        this.parametrized.setSelected(selected.isParametrized());
        this.query.setSelected(selected.isQuery());
        this.call.setSelected(selected.isCall());
        this.update.setSelected(selected.isUpdate());
        this.lineDelimiter.setText(selected.getLineDelimiter() + "");
        this.paramDelimiter.setText(selected.getParamDelimiter() + "");
        this.timeout.setText(selected.getTimeout() + "");
        onSqlInput();
    }
    
    public void editTask(String item) {
        Task selected = TaskManager.getInstance().getTaskByName(item);
        if (selected == null) {
            GUI.getInstance().openWarningWindow("Item cannot be edited!", "Not found!");
            return;
        }
        this.taskName.setText(selected.getTaskName());
        this.interval.setText(selected.getInterval() + "");
        this.poolSize.setText(selected.getPoolSize() + "");
        this.repeatCount.setText(selected.getRepeatCount() + "");
        this.taskSets.getItems().clear();
        for (long set : selected.getSets()) {
            try {
                this.taskSets.getItems().add(SetManager.getInstance().getSetById(set).getName());
            } catch (SetNotFoundException ex) {
                GUI.getInstance().openWarningWindow("Task contains items that no longer exist", "Invalid task!");
            }
        }
        this.taskCons.getItems().clear();
        for (long con : selected.getDatabases()) {
            try {
                this.taskCons.getItems().add(ConManager.getInstance().getConById(con).getCustomName());
            } catch (ConnectionNotFoundException ex) {
                GUI.getInstance().openWarningWindow("Task contains items that no longer exist", "Invalid task!");
            }
        }
        editedTask = selected;
    }
    
    @FXML
    private void cancelSetEdit() {
        errorlog.getChildren().clear();
        this.sql.clear();
        this.variables.setText("");
        this.setName.setText("");
        this.parametrized.setSelected(false);
        this.query.setSelected(false);
        this.call.setSelected(false);
        this.update.setSelected(false);
        this.lineDelimiter.setText(";");
        this.paramDelimiter.setText(",");
        this.timeout.setText("-1");
        editedSet = null;
    }
    
    @FXML
    private void cancelConnectionEdit() {
        this.customName.setText("");
        this.port.setText("0");
        this.password.setText("");
        this.user.setText("");
        this.dbAddress.setText("");
        this.databaseName.setText("");
        editedCon = null;
    }
    
    @FXML
    private void cancelTaskEdit() {
        this.taskName.setText("");
        this.interval.setText("");
        this.poolSize.setText("");
        this.repeatCount.setText("");
        this.taskSets.setItems(FXCollections.observableArrayList());
        this.taskCons.setItems(FXCollections.observableArrayList());
        editedTask = null;
    }
    
    @FXML
    public void saveTaskToMemory() {
        Task task = new Task();
        //when editing mode is on edit currently selected task to edit
        if (editedTask != null) {
            task = editedTask;
        }
        
        /**USER INPUT CHECK**/
        //task name
        if (this.taskName.getText().length() == 0) {
            this.saveTask.requestFocus();
            taskWarning.setText("Name is requiered!");
            return;
        }
        if (!TaskManager.getInstance().isNameUnique(this.taskName.getText(), task.getId())) {
            this.saveTask.requestFocus();
            GUI.getInstance().openWarningWindow("Name is not unique, changes cannot be saved!", "Invalid name!");
            return;
        }
        if (task.getTaskName() != null && !task.getTaskName().equals(this.taskName.getText())) task.modified = true;
        task.setTaskName(this.taskName.getText());
        //task interval (delay)
        if (this.interval.getText().length() == 0) {
            this.saveTask.requestFocus();
            GUI.getInstance().openWarningWindow("Please fill interval (delay) between cycles (0 = no delay between cycles)", "Interval is requiered!");
            return;
        }
        try {
            if (task.getInterval() != Long.valueOf(this.interval.getText())) task.modified = true;
            task.setInterval(Long.valueOf(this.interval.getText()));
        } catch (NumberFormatException e) {
            this.saveTask.requestFocus();
            GUI.getInstance().openWarningWindow("Interval must be long number!", "Invalid interval number!");
            return;
        }
        //task pool size (concurrent connections)
        if (this.poolSize.getText().length() == 0) {
            GUI.getInstance().openWarningWindow("Pool size is required!", "Invalid pool size!");
            return;
        }
        try {
            if (task.getPoolSize() != Integer.valueOf(this.poolSize.getText())) task.modified = true;
            task.setPoolSize(Integer.valueOf(this.poolSize.getText()));
        } catch (NumberFormatException e) {
           this.saveTask.requestFocus();
           GUI.getInstance().openWarningWindow("Pool size must be integer number!", "Pool size must be a number!"); 
           return;
        }
        //repeat count
        if (this.repeatCount.getText().length() == 0) {
            this.saveTask.requestFocus();
            GUI.getInstance().openWarningWindow("Repeat count is required!", "Invalid repeat count!");
            return;
        }
        try {
            if (task.getRepeatCount() != Integer.valueOf(this.repeatCount.getText())) task.modified = true;
            task.setRepeatCount(Integer.valueOf(this.repeatCount.getText()));
        } catch (NumberFormatException e) {
            this.saveTask.requestFocus();
            GUI.getInstance().openWarningWindow("Repeat count must be integer number!", "Invalid repeat count!"); 
            return;
        }
        
        //if we are editing we need to remove all tasks first
        if (editedTask != null) {
            task.setSets(new LinkedList());
        }
        
        //sets in this task
        for (Object set : taskSets.getItems()) {
            String setname = set.toString();
            SQLSet savedset;
            try {
                savedset = SetManager.getInstance().getSetByName(setname);
            } catch (SetNotFoundException ex) {
                GUI.getInstance().openWarningWindow("Saved SQL set " + setname + " not found!", "Task cannot be saved!");
                return;
            }
            task.getSets().add(savedset.getId());
        }
        
        //if we are in edit mode we need to remove all dbs first
        if (editedTask != null) {
            task.setDatabases(new LinkedList());
        }
        
        //databases the task will run on
        for (Object db : taskCons.getItems()) {
            String conname = db.toString();
            DbCon con = ConManager.getInstance().getConByName(conname);
            if (con == null) {
                GUI.getInstance().openWarningWindow("Saved connection " + conname + " not found!", "Task cannot be saved!");
                return;
            }
            task.getDatabases().add(con.getId());
        }
        /**USER INPUT CHECK END**/
        
        try {
            //if no edit mode, then create a new Task in TaskManager
            if (editedTask == null) {
                TaskManager.getInstance().addTask(task);
                saveTask.setDisable(true);
                editedTask = task; //select the newly created task
            }
        } catch (TaskNameNotUniqueException e) {
            this.saveTask.requestFocus();
            GUI.getInstance().openWarningWindow("Task name is not unique!", "Name must be unique!");
            taskWarning.setText("Name is not unique!");
            return;
        }
        if (task.modified) {
            this.tabPane.getTabs().get(Tabs.TASKS).setStyle("-fx-font-weight:bold;");
        }
        saveTask.setStyle("-fx-font-weight: normal;");
        updateTaskList(TaskManager.getInstance().getTasks());
    }

    @FXML
    private void saveSetToMemory() {
        SQLSet set = new SQLSet();
        
        //when editing mode is on edit currently selected con to edit
        if (editedSet != null) {
            set = editedSet;
        }
        
        /**USER INPUT CHECK**/
        if (this.setName.getText().length() == 0) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Please fill set name!", "Name is requiered!");
            return;
        }
        if (set.getName() == null || !set.getName().equals(this.setName.getText())) set.modified = true;
        set.setName(this.setName.getText());
        try {
            set.setDatabaseType(((DatabasePlugin) this.databaseTypeSets.getSelectionModel().getSelectedItem()).getName());
        } catch (NullPointerException e) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("No database type selected, if no options are given, it means that you have no database plugins loaded.", "Database type is requiered!");
            return;
        }
        if (set.isParametrized() != this.parametrized.isSelected()) set.modified = true;
        set.setParametrized(this.parametrized.isSelected());
        
        if (!this.query.isSelected() && !this.call.isSelected() && !this.update.isSelected()) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Please select Call, Query or Update type of statement!", "Statement type is requiered!");
            return;
        }
        
        if (set.isQuery() != this.query.isSelected()) set.modified = true;
        set.setQuery(this.query.isSelected());
        
        if (set.isCall() != this.call.isSelected()) set.modified = true;
        set.setCall(this.call.isSelected());
        
        if (set.isUpdate() != this.update.isSelected()) set.modified = true;
        set.setUpdate(this.update.isSelected());
        
        try {
            if (set.getTimeout() != Integer.valueOf(this.timeout.getText())) set.modified = true;
            set.setTimeout(Integer.valueOf(this.timeout.getText()));
        } catch (Exception e) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Timeout must be a number!", "Timeout is requiered!");
            return;
        }
        
        if (this.paramDelimiter.getText().equals(this.lineDelimiter.getText())) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Line and param delimiter cannot be the same!", "Invalid delimiter!");
            return;
        }
        
        if (this.paramDelimiter.getText().length() > 1) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Delimiter must be a single character!", "Invalid delimiter!");
            return;
        }
        if (set.getParamDelimiter() != this.paramDelimiter.getText().charAt(0)) set.modified = true;
        set.setParamDelimiter(this.paramDelimiter.getText().charAt(0));
        
        if (this.lineDelimiter.getText().length() > 1) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Delimiter must be a single character!", "Invalid delimiter!");
            return;
        }
        if (set.getLineDelimiter() != this.lineDelimiter.getText().charAt(0)) set.modified = true;
        set.setLineDelimiter(this.lineDelimiter.getText().charAt(0));
        
        if (this.sql.getText().length() == 0) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("SQL Area is empty!", "SQL is requiered!");
            return;
        }
        if (set.getSql() == null || !set.getSql().equals(this.sql.getText())) set.modified = true;
        set.setSql(this.sql.getText());
        try {
            if (set.getValues() == null || !set.getValues().equals(VariableParser.parseValues(this.variables.getText(), set.getLineDelimiter(), set.getParamDelimiter()))) set.modified = true;
            set.setValues(VariableParser.parseValues(this.variables.getText(), set.getLineDelimiter(), set.getParamDelimiter()));
        } catch (VariableException e) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow(e.getMessage(), "Invalid variables format!");
        } catch (Exception e) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Please make sure every line is ended properly by line delimiter char.", "Invalid variables format!");
            e.printStackTrace();
        }
        /**USER INPUT CHECK END**/

        try {
            //if no edit mode, then create a new set in SetManager
            if (editedSet == null) {
                SetManager.getInstance().addSet(set);
                saveSet.setDisable(true);
                editedSet = set;
            }
        } catch (SetNameNotUniqueException e) {
            this.saveSet.requestFocus();
            GUI.getInstance().openWarningWindow("Set name must be unique!", "Invalid set name");
            return;
        }
        saveSet.setStyle("-fx-font-weight: normal;");
        if (set.modified) {
            this.tabPane.getTabs().get(Tabs.SETS).setStyle("-fx-font-weight:bold;");
        }
        updateSetList(SetManager.getInstance().getSets());
    }

    @FXML
    private void saveConnectionToMemory() {
        DbCon con = new DbCon();
        //if editing mode is on edit currently selected con
        if (editedCon != null) {
            con = editedCon;
        }
        
        /**USER INPUT CHECK**/
        try {
            if (con.getPort() != Integer.valueOf(this.port.getText())) con.modified = true;
            con.setPort(Integer.valueOf(this.port.getText()));
        } catch (Exception e) {
            this.saveConnection.requestFocus();
            GUI.getInstance().openWarningWindow("Value '" + this.port.getText() + "' is invalid for port number!", "Port must be a number!");
            return;
        }
        if (this.dbAddress.getText().contains("\\") && Integer.valueOf(this.port.getText()) > 1) {
            this.saveConnection.requestFocus();
            if (!GUI.getInstance().openConfrmWindow("Are you sure ?", "It seems like you have specified instance name and instance port as well. Is it correct ?")) {
                return;
            }
        }
        
        if (con.getAddress() == null || !con.getAddress().equals(this.dbAddress.getText())) con.modified = true;
        con.setAddress(dbAddress.getText());
        if (this.customName.getText().length() == 0) {
            this.saveConnection.requestFocus();
            GUI.getInstance().openWarningWindow("Please fill the custom name!", "Name is requiered!");
            return;
        }
        if ( con.getCustomName() == null || !con.getCustomName().equals(this.customName.getText())) con.modified = true;
        con.setCustomName(this.customName.getText());
        try {
            con.setDatabaseType(((DatabasePlugin) this.databaseType.getSelectionModel().getSelectedItem()).getName());
        } catch (NullPointerException e) {
            GUI.getInstance().openWarningWindow("No database type selected, if no options are given, it means that you have no database plugins loaded.", "Database type is requiered!");
            return;
        }
        if (con.getDecodedPassword() == null || !con.getDecodedPassword().equals(this.password.getText())) con.modified = true;
        con.setDecodedPassword(this.password.getText());
        
        if (con.getDatabaseName() == null || !con.getDatabaseName().equals(this.databaseName.getText())) con.modified = true;
        con.setDatabaseName(this.databaseName.getText());

        if (con.getUser() == null || !con.getUser().equals(this.user.getText())) con.modified = true;
        con.setUser(this.user.getText());
        /**USER INPUT CHECK END**/
        
        
        try {
            //if no edit mode, then create a new connection in ConManager
            if (editedCon == null) {
                ConManager.getInstance().addConnection(con); 
                saveConnection.setDisable(true);
                editedCon = con;
            }
        } catch (ConnectionNameNotUniqueException ex) {
            GUI.getInstance().openWarningWindow("Connection name must be unique!", "Invalid connection name");
            return;
        }
        saveConnection.setStyle("-fx-font-weight: normal;");
        if (con.modified) {
            this.tabPane.getTabs().get(Tabs.CONNECTIONS).setStyle("-fx-font-weight:bold;");
        }
        updateConnectionList(ConManager.getInstance().getConnections());
    }
    
    @FXML
    public void runTask() {
        try {
            if (editedTask == null) {
                GUI.getInstance().openWarningWindow("Please select a task to run, if you are creating a new task, please save the task before running it.", "No task selected!");
                return;
            }
            LinkedList<RunnableTask> rts = RunnableTaskCreator.createRunnable(editedTask);
            GUI.getInstance().openRunningTaskWindow(editedTask.getTaskName()).setCurrentTaskList(rts);
        } catch (Exception ex) {
            ex.printStackTrace();
            GUI.getInstance().openExceptionWindow(ex);
        }
    }
    
    @FXML
    public void addAllSQL() {
        removeAllSQL();
        for (Object item : availableSets.getItems()) {
            taskSets.getItems().add(item.toString());
        }
        if (this.editedTask != null) {
            this.editedTask.modified = true;
            saveTaskToMemory();
        }
    }
    
    @FXML
    public void removeAllSQL() {
        taskSets.getItems().clear();
        if (this.editedTask != null) {
            this.editedTask.modified = true;
            saveTaskToMemory();
        }
    }
    
    @FXML
    public void addAllDbs() {
        removeAllDbs();
         for (Object item : availableDbs.getItems()) {
            taskCons.getItems().add(item.toString());
        }
        if (this.editedTask != null) {
            this.editedTask.modified = true;
            saveTaskToMemory();
        }
    }
    
    @FXML
    public void removeAllDbs() {
        taskCons.getItems().clear();
        if (this.editedTask != null) {
            this.editedTask.modified = true;
            saveTaskToMemory();
        }
    }
    
    @FXML
    public void testConnection() {
        if (this.editedCon != null) {
            this.saveConnectionToMemory();
        } else {
            if (GUI.getInstance().openConfrmWindow("Connection is not saved", "Do you want to create a new connection before testing ?")) {
                this.saveConnectionToMemory();
            } else {
                GUI.getInstance().openInfoWindow("Cannot test the connection!", "Connection must be saved into the application before testing it!");
                return;
            }
        }
        Thread t = new Thread() {
            public void run() {
                try {
                    DbPool pool = RunnableTaskCreator.createPool(editedCon, 1);
                    if (!pool.getConnection().isValid(1)) {
                        throw new Exception("Connection not valid!");
                    }
                    pool.close();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getInstance().openInfoWindow("Success!", "Successfully connected to the SQL server!");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GUI.getInstance().openExceptionWindow(ex);
                        }
                    });
                }
            }
        };
        t.start();
    }
    
    @FXML
    public void databaseSelected() {
        try {
            if (this.connectionList.getSelectionModel().getSelectedItem() != null) {
                conWarning.setText("");
                this.editConnection(this.connectionList.getSelectionModel().getSelectedItem().toString());
            }
        } catch (IndexOutOfBoundsException e) {
            GUI.getInstance().openExceptionWindow(e);
        }
    }
    
    @FXML
    public void setSelected() {
        try {
            if (this.setList.getSelectionModel().getSelectedItem() != null) {
                setWarning.setText("");
                this.editSet(this.setList.getSelectionModel().getSelectedItem().toString());
            }
        } catch (IndexOutOfBoundsException e) {
            GUI.getInstance().openExceptionWindow(e);
        }
    }
    
    @FXML
    public void taskSelected() {
        try {
            if (this.taskList.getSelectionModel().getSelectedItem() != null) {
                taskWarning.setText("");
                this.editTask(this.taskList.getSelectionModel().getSelectedItem().toString());
            }
        } catch (IndexOutOfBoundsException e) {
            GUI.getInstance().openExceptionWindow(e);
        }
    }
    
    @FXML
    public void closeWindow() {
        if (GUI.getInstance().openConfrmWindow("Are you sure ?", "Closing this window will stop all running tasks and any unsaved data will be lost!")) {
            System.exit(0);
        }
    }
    
    @FXML
    public void openHelp() {
        GUI.getInstance().openHelpWindow();
    }
    
    @FXML
    public void reloadEnums() {
        try {
            CharEnumManager.getInstance().loadEnums();
            GUI.getInstance().openInfoWindow("Enums successfuly reloaded!", "Loaded " + CharEnumManager.getInstance().getCharEnums().size() + " character enumerations!");
        } catch (IOException ex) {
            GUI.getInstance().openExceptionWindow(ex);
        }
    }
    
    @FXML
    public void openCharEnumFile() {
        try {
            File f = new File("data/charenums.txt");
            if (!f.exists()) f.createNewFile();
            java.awt.Desktop.getDesktop().edit(f);
        } catch (IOException ex) {
            GUI.getInstance().openExceptionWindow(ex);
        }
    }

    @FXML
    public void reloadConnections() {
        ConManager.reload();
        updateConnectionList(ConManager.getInstance().getConnections());
        GUI.getInstance().openInfoWindow("Connections reloaded!", "Loaded " + ConManager.getInstance().getConnections().size() + " database connections!");
    }
    
    @FXML
    public void editConnections() {
        try {
            File f = new File("data/connections.xml");
            if (!f.exists()) f.createNewFile();
            java.awt.Desktop.getDesktop().edit(f);
        } catch (IOException ex) {
            GUI.getInstance().openExceptionWindow(ex);
        }
    }
    
    @FXML
    public void newConnection() {
        saveConnection.setDisable(false);
        tabPane.getSelectionModel().select(Tabs.CONNECTIONS);
        this.cancelConnectionEdit();
        customName.setText("MyDatabase1");
        customName.requestFocus();
        customName.selectAll();
        saveConnection.setStyle("-fx-font-weight: bold");
    }
    
    @FXML
    public void newSQL() {
        saveSet.setDisable(false);
        tabPane.getSelectionModel().select(Tabs.SETS);
        this.cancelSetEdit();
        setName.setText("MyNewSQL1");
        setName.requestFocus();
        setName.selectAll();
        this.sql.clear();
        this.sql.appendText("Some SQL here...");
        saveSet.setStyle("-fx-font-weight: bold");
    }
    
    @FXML
    public void newTask() {
        saveTask.setDisable(false);
        tabPane.getSelectionModel().select(Tabs.TASKS);
        this.cancelTaskEdit();
        taskName.setText("MyNewTask1");
        taskName.requestFocus();
        taskName.selectAll();
        interval.setText("1000");
        poolSize.setText("1");
        repeatCount.setText("-1");
        saveTask.setStyle("-fx-font-weight: bold");
    }
    
    @FXML
    public void onSqlInput() {
        errorlog.getChildren().clear();
        try {
            DatabasePlugin pl = ((DatabasePlugin) this.databaseTypeSets.getSelectionModel().getSelectedItem());
            ParseError pe = pl.parse(sql.getText());
            if (pe == null) {
                Text t1 = new Text();
                t1.setStyle("-fx-fill: #129906;-fx-font-weight:bold;");
                t1.setText("SQL syntax is correct!");
                errorlog.getChildren().addAll(t1);
            } else {
                Text t1 = new Text();
                t1.setStyle("-fx-fill: #a51303;-fx-font-weight:bold;");
                t1.setText(pe.getMessage());
                errorlog.getChildren().addAll(t1);
            }
        } catch (NullPointerException e) {
            Text t1 = new Text();
            t1.setStyle("-fx-fill: #f79722;-fx-font-weight:bold;");
            t1.setText("No database type selected. SQL syntax checking is disabled.");
            errorlog.getChildren().addAll(t1);
        }
    }
    
    @FXML
    public void handleShortcut(KeyEvent event) {
        if (KeyShortcuts.save.match(event)) {
            saveAll();
        }
    }
    
    @FXML
    public void saveAll() {
        TaskManager.getInstance().save();
        SetManager.getInstance().save();
        ConManager.getInstance().save();
        for (Tab tab : this.tabPane.getTabs()) {
            tab.setStyle("-fx-font-weight:normal;");
        }
    }

    @FXML
    public void importConnections() {
        GUI.getInstance().openImportWindow();
    }
    
    @FXML
    public void taskEdited() {
        taskWarning.setText("");
        if (this.editedTask != null) {
            saveTaskToMemory();
        }
    }
    
    @FXML
    public void conEdited() {
        if (this.editedCon != null) {
            saveConnectionToMemory();
        }
    }
    
    @FXML
    public void setEdited() {
        if (this.editedSet != null) {
            saveSetToMemory();
        }
    }
    
    @FXML
    public void addSelectedSets() {
        for (Object selected : availableSets.getSelectionModel().getSelectedItems()) {
            String set = selected.toString();
            taskSets.getItems().add(set);
        }
        if (this.editedTask != null) this.editedTask.modified = true;
        saveTaskToMemory();
    }
    
    @FXML
    public void addSelectedCons() {
        for (Object selected : availableDbs.getSelectionModel().getSelectedItems()) {
            String con = selected.toString();
            if (taskCons.getItems().contains(con)) {
                GUI.getInstance().openWarningWindow("Task already contains '" + con + "' connection!", "Duplicate entry!");
                return;
            }
            taskCons.getItems().add(con);
        }
        if (this.editedTask != null) this.editedTask.modified = true;
        saveTaskToMemory();
    }
    
    @FXML
    public void removeSelectedSql() {
        taskSets.getItems().removeAll(taskSets.getSelectionModel().getSelectedItems());
        if (this.editedTask != null) this.editedTask.modified = true;
        saveTaskToMemory();
    }
    
    @FXML
    public void removeSelectedCons() {
        taskCons.getItems().removeAll(taskCons.getSelectionModel().getSelectedItems());
        if (this.editedTask != null) this.editedTask.modified = true;
        saveTaskToMemory();
    }
}
