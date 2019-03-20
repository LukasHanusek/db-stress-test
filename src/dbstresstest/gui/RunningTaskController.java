package dbstresstest.gui;

import dbstresstest.gui.utils.GUI;
import dbstresstest.logic.runnable.QueryLogger;
import dbstresstest.logic.runnable.RunnableTask;
import dbstresstest.logic.runnable.TaskEventListener;
import dbstresstest.util.TimeUtil;
import dbstresstest.util.exceptions.TaskException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Lukas Hanusek
 */
public class RunningTaskController implements Initializable {

    private Stage stage;
    private GUI gui;

    @FXML
    private TextArea taskConsole;
    
    @FXML
    private Button startAll, stopAll, csvExportAll;

    @FXML
    private Label loops, queries, totalTime, poolStatus, queryTime;

    @FXML
    private Label selectedDb; //currently selected database

    @FXML
    private ListView databases; //databases this task is currently running on
    
    @FXML
    private TableView<QueryLogger> resultTable;
    
    @FXML
    private TableColumn query, avgTime, executed, errors, lastSuccess, lastError;
    

    private LinkedList<RunnableTask> tasks; //currently monitored task in this window

    private int selectedTask = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        query.setCellValueFactory(new PropertyValueFactory("queryName"));
        avgTime.setCellValueFactory(new PropertyValueFactory("avgExecTime"));
        executed.setCellValueFactory(new PropertyValueFactory("totalSuccessfulQueries"));
        errors.setCellValueFactory(new PropertyValueFactory("failedQueries"));
        lastSuccess.setCellValueFactory(new PropertyValueFactory("lastSuccessTimeString"));
        lastError.setCellValueFactory(new PropertyValueFactory("lastFailTimeString"));
    }

    public void setCurrentTaskList(LinkedList<RunnableTask> rts) {
        this.tasks = rts;
        databases.getItems().clear();
        for (RunnableTask rt : rts) {
            databases.getItems().add(rt.getName());
        }
        registerTaskListener();
    }

    public void selectMonitoredTask(int index) {
        try {
            if (index >= this.tasks.size()) {
                gui.openWarningWindow("You selected task index " + index + ", but there are only " + this.tasks.size() + " tasks in this monitor window!", "Invalid task");
                return;
            }
            selectedDb.setText(tasks.get(index).getConnectionInfo().getCustomName());
            this.selectedTask = index;
            registerTaskListener(); 
        } catch (IndexOutOfBoundsException e) {
            //when updating errors and generally text in the ListView it may occur that user clicks into "empty" list during the refreshing, do not need to solve it right there
        }
    }

    public void setup(Stage stage, GUI gui) {
        this.stage = stage;
        this.gui = gui;
        //when this window is closed, popup confirm window, cancel the current task on close so it does not get out of controls
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                if (gui.openConfrmWindow("Are you sure you want cancel this test task ?", "Closing this window will cancel any running task in this window. If the task is finished, unsaved results will be lost as well.")) {
                    //ok button
                    stopAll();
                } else {
                    //cancel button
                    t.consume();
                }
            }
        });
        //auto resize all columns to fit 100% width of the current table
        query.prefWidthProperty().bind(resultTable.widthProperty().divide(resultTable.getColumns().size()));
        avgTime.prefWidthProperty().bind(resultTable.widthProperty().divide(resultTable.getColumns().size()));
        executed.prefWidthProperty().bind(resultTable.widthProperty().divide(resultTable.getColumns().size()));
        errors.prefWidthProperty().bind(resultTable.widthProperty().divide(resultTable.getColumns().size()));
        lastSuccess.prefWidthProperty().bind(resultTable.widthProperty().divide(resultTable.getColumns().size()));
        lastError.prefWidthProperty().bind(resultTable.widthProperty().divide(resultTable.getColumns().size()));
        
    }
    
    @FXML
    public void selectDatatabaseToMonitor() {
        selectMonitoredTask(databases.getSelectionModel().getSelectedIndex());
    }

    @FXML
    public void startSelectedTask() {
        //TODO: implement  
    }

    @FXML
    public void stopSelectedTask() {
        //TODO: implement
    }
    
    @FXML
    public void exportTask() {
        RunnableTask selected = this.tasks.get(selectedTask);
        if (selected == null) {
            gui.openWarningWindow("Selected task not found!", "Invalid task!");
            return;
        }
        if (selected.isRunning()) {
            gui.openExceptionWindow(new TaskException("Task is still running!"));
            return;
        }
        try {
            selected.getLogger().exportToCSV();
            gui.openInfoWindow("Success", "Task results successfuly exported to: " + selected.getLogger().getLogFolder().getAbsolutePath());
        } catch (IOException ex) {
            gui.openExceptionWindow(ex);
            ex.printStackTrace();
        }
    }
    
    @FXML
    public void csvExportAll() {
        try {
            for (RunnableTask rt : tasks) {
                if (rt.isRunning()) {
                    gui.openExceptionWindow(new TaskException("Task for connection " + rt.getConnectionInfo().getCustomName() + " still running!"));
                    return;
                }
                rt.getLogger().exportToCSV();
            }
            gui.openInfoWindow("Success", "Task results successfuly exported to: " + tasks.get(selectedTask).getLogger().getLogFolder().getAbsolutePath());
        } catch (Exception ex) {
            gui.openExceptionWindow(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Stops all tasks controleld by this monitor
     */
    @FXML
    public void stopAll() {
        for (RunnableTask rt : tasks) {
            rt.cancel();
        }
        stopAll.disableProperty().set(true);
    }

    @FXML
    public void startAll() {
        for (RunnableTask rt : tasks) {
            try {
                rt.start();
            } catch (Exception ex) {
                gui.openExceptionWindow(ex);
                ex.printStackTrace();
            }
        }
       startAll.disableProperty().set(true);
       stopAll.disableProperty().set(false);
    }

    /**
     * Register listeners for currently selected task
     */
    private void registerTaskListener() {
        RunnableTask selected = this.tasks.get(selectedTask);
        if (selected == null) {
            return;
        }

        //update console text
        StringBuilder sb = new StringBuilder();
        for (String line : selected.getLogger().getConsole()) {
            sb.append("\n");
            sb.append(line);
        }
        taskConsole.setText(sb.toString());

        //reset existing listeners, we cannot have 2 listeners for 1 gui
        for (RunnableTask rt : this.tasks) {
            rt.removeAllListeners();
        }

        //reset current gui elements
        updatePoolStatus();
        updateTotalTime();
        updateQueries();
        updateQueryTime();
        
        updateTable(true);

        //register new listener
        selected.registerListener(new TaskEventListener() {
            @Override
            public void onConsoleLog(String message) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        taskConsole.setText(taskConsole.getText() + "\n" + message);
                        taskConsole.setScrollTop(Double.MAX_VALUE);
                    }
                });
            }

            @Override
            public void onQuery() {
                updateTotalTime();
                updateQueries();
                updateQueryTime();
            }

            @Override
            public void onLoop() {
                updateLoops();
                updatePoolStatus();
                printErrorsToListView();
            }

            //after first loop end populate data table with QueryLogger class instances
            @Override
            public void onLoopEnd() {
                updateTable(false);
            }

            @Override
            public void onStop() {
                updatePoolStatus();
                updateTotalTime();
                updateQueryTime();
            }
        });
    }

    private void printErrorsToListView() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                databases.getItems().clear();
                for (RunnableTask rt : tasks) {
                    if (rt.getLogger().getFailedQueries() == 0) {
                        databases.getItems().add(rt.getName());
                    } else {
                        databases.getItems().add(rt.getName() + " (errors: " + rt.getLogger().getFailedQueries() + ")");
                    }
                }
            }
        });
    }
    
    public void updateTable(boolean force) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (resultTable.getItems().isEmpty() || force) {
                    resultTable.getItems().clear();
                    RunnableTask task = tasks.get(selectedTask);
                    resultTable.getItems().addAll(task.getLogger().getQueryLogs().values());
                }
                resultTable.refresh(); //refresh data from objects
            }
        });
    }

    private Timer t = new Timer();
    
    private void updatePoolStatus() {
        TimerTask tt = new TimerTask() {
            public void run() {
                RunnableTask rt = tasks.get(selectedTask);
                if (rt != null && rt.getPool() != null) {
                    String text = rt.getPool().getPoolActive() + " / " + rt.getPool().getPoolMaxActive() + "(" + rt.getPool().getPoolIdle() + " idle)";
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (rt != null) {
                                if (rt.getPool() != null) {
                                    poolStatus.setText(text);
                                }
                            }
                        }
                    });
                }
            }
        };
        t.schedule(tt, 0);
    }

    private void updateTotalTime() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RunnableTask rt = tasks.get(selectedTask);
                if (rt != null) {
                    totalTime.setText(TimeUtil.getTimeFormat(rt.getLogger().getDuration()));
                }
            }
        });
    }

    private void updateLoops() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RunnableTask rt = tasks.get(selectedTask);
                if (rt != null) {
                    loops.setText(rt.getLogger().getLoops() + " / " + rt.getTask().getRepeatCount());
                }
            }
        });
    }

    private void updateQueries() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RunnableTask rt = tasks.get(selectedTask);
                if (rt != null) {
                    queries.setText(rt.getLogger().getTotalCommandsExecuted() + " / " + rt.getTotalQueries());
                }
            }
        });
    }

    private void updateQueryTime() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                RunnableTask rt = tasks.get(selectedTask);
                if (rt != null) {
                    queryTime.setText(rt.getLogger().getTotalQueryTime() + " ms");
                }
            }
        });
    }

}
