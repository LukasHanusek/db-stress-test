package dbstresstest.logic.runnable;

import dbstresstest.data.objects.DbCon;
import dbstresstest.data.objects.SQLSet;
import dbstresstest.data.objects.Task;
import dbstresstest.data.objects.managers.SetManager;
import dbstresstest.gui.utils.GUI;
import dbstresstest.plugins.DbPool;
import dbstresstest.util.exceptions.ConnectionPoolSetupException;
import dbstresstest.logic.runnable.work.ExecutableQuery;
import dbstresstest.logic.runnable.work.ParametrizedQuery;
import dbstresstest.logic.runnable.work.UnparametrizedQuery;
import dbstresstest.util.exceptions.InvalidFunctionException;
import dbstresstest.util.exceptions.SetNotFoundException;
import dbstresstest.util.exceptions.TaskException;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Lukas Hanusek
 */
public class RunnableTask extends TimerTask {
    
    //event listeners
    private CopyOnWriteArrayList<TaskEventListener> listeners; //events are called from TaskLogger class
    
    private Task task;
    private DbCon con; //connection this task will run on (no important to the process, as all db info is handled in DbPool, this is just for monitoring purposes)
    private DbPool pool;
    ExecutorService executorService;
    private TaskLogger logger;
    private boolean isRunning = false;
    private boolean isCancelled = false;
    private Thread t;
    
    private long interval;
    
    private int repeatCount;
    
    private ConcurrentLinkedQueue<ExecutableQuery> queries = new ConcurrentLinkedQueue<ExecutableQuery>();
    
    private String name; //custom task name to identify it in the monitor (normally assigned database name this task is running on)
    
    //calculated values
    long totalQueries = 0;

    public RunnableTask(Task task, DbCon con, String name) throws InvalidFunctionException {
        this.task = task;
        this.con = con;
        this.name = name;
        this.interval = task.getInterval();
        this.repeatCount = task.getRepeatCount();
        listeners = new CopyOnWriteArrayList();
        logger = new TaskLogger(this);
        calculateTotalQueries();
        //prepare executable objects
        for (long setid : task.getSets()) {
            try {
                SQLSet set = SetManager.getInstance().getSetById(setid);
                if (set.isParametrized() || set.isCall()) queries.add(new ParametrizedQuery(set)); //call (execute stored procedure) is always handled by ParametrizedQuery impl.
                else queries.add(new UnparametrizedQuery(set));
            } catch (SetNotFoundException ex) {
                GUI.getInstance().openExceptionWindow(ex);
            }
        }
    }

    /**
     * Start this task
     */
    public void start() throws TaskException, ConnectionPoolSetupException, ClassNotFoundException {
        if (this.isRunning) {
            throw new TaskException("Already running!");
        }
        
        logger.log("Starting task on " + this.con.getCustomName() + " connection...");
        
        //if restarting or for some other reason the pool exists, make sure to close it before creating new one to prevent resource leaks
        if (this.pool != null) {
            try {
                this.pool.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        this.pool = RunnableTaskCreator.createPool(this.con, this.task.getPoolSize());
        
        //prevent multiple runs in the same time
        if (executorService != null) {
            executorService.shutdownNow();
        }

       //we need new thread in order to wait for all tasks to finish
       this.t = new Thread() {
            public void run() {
                logger.log("Creating a thread pool...");
                executorService = Executors.newFixedThreadPool(pool.getPoolMaxActive());
                logger.log("Thread pool (" + pool.getPoolMaxActive() +  ") created!)");
                RunnableTask.this.isRunning = true;
                logger.logStart();

                try {
                    //infinite repeat
                    if (RunnableTask.this.repeatCount == -1) {
                        while (!isCancelled) {
                            executorService.submit(RunnableTask.this);
                            Thread.sleep(interval);
                        }
                    }
                    //specified repeat count
                    if (RunnableTask.this.repeatCount >= 0) {
                        for (int i = 0; i < RunnableTask.this.repeatCount; i++) {
                            if (isCancelled) {
                                break;
                            }
                            executorService.submit(RunnableTask.this);
                            Thread.sleep(interval);
                        }
                    }
                    executorService.shutdown(); //wait for all tasks to finish and then shutdown executor
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); //wait till shutdown finished
                    RunnableTask.this.cancel(); //cancel and mark this task as finished
                } catch (InterruptedException ex) {
                    logger.log("Task could not finish, process interrupted!");
                    RunnableTask.this.cancel();
                }
            }
        };
       this.t.start();
    }

    /**
     * Never call this manually, to start this task call start() method instead
     */
    @Override
    public void run() {
        logger.logRun();
        for (ExecutableQuery q : this.queries) {
            try {
                q.execute(this.pool, this.logger);
            } catch (Exception ex) {
                logger.log(ex.getMessage());
            }
        }
        for (TaskEventListener tel : getListeners()) tel.onLoopEnd(); //call event
    }
    
    @Override
    public boolean cancel() {
        Thread cancelThread = new Thread() {
            public void run() {
                if (isRunning()) {
                    RunnableTask.this.t.interrupt(); //must be interupted before closing pool
                    executorService.shutdownNow(); //shutdown now will cancel any tasks pending, we need this to be able to cancel the task at any stage
                    try {
                        pool.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    RunnableTask.this.isCancelled = true; //must be called AFTER THREAD INTERUPT otherwise method executor.shutdown() will wait for all threads to finish, we need shutdown now
                    logger.logEnd();
                    logger.log("Task finished in " + logger.getDuration() + "ms, total commands: " + logger.getTotalCommandsExecuted() + ", total errors: " + logger.getFailedQueries());
                    RunnableTask.this.isRunning = false;
                }
            }
        };
        cancelThread.start();
        return true;
    }

    public String getName() {
        return name;
    }
    
    public boolean isRunning() {
        return this.isRunning;
    }
    
    public DbPool getPool() {
        return pool;
    }
    
    public void registerListener(TaskEventListener tel) {
        this.listeners.add(tel);
    }
    
    public void removeListener(TaskEventListener tel) {
        this.listeners.remove(tel);
    }
    
    public void removeAllListeners() {
        this.listeners.clear();
    }

    public Task getTask() {
        return task;
    }

    public TaskLogger getLogger() {
        return logger;
    }

    public CopyOnWriteArrayList<TaskEventListener> getListeners() {
        return listeners;
    }
    
    public boolean isIsCancelled() {
        return isCancelled;
    }
    
    public long getTotalQueries() {
        return totalQueries;
    }

    private void calculateTotalQueries() {
        if (task.getRepeatCount() == -1) {
            totalQueries = -1;
        } else {
            int total = 0;
            for (long set : this.task.getSets()) {
                try {
                    SQLSet ss = SetManager.getInstance().getSetById(set);
                    if (ss.getValues() == null) {
                        total += 1 * task.getRepeatCount(); //query without values
                        continue;
                    }
                    total += ss.getValues().size() * task.getRepeatCount();
                } catch (SetNotFoundException ex) {
                    GUI.getInstance().openExceptionWindow(ex);
                }
            }
            totalQueries = total;
        }
    }

    public DbCon getConnectionInfo() {
        return con;
    }
    
}
