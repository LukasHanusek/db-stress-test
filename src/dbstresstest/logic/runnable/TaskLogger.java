package dbstresstest.logic.runnable;

import dbstresstest.util.TimeUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Lukas Hanusek
 */
public class TaskLogger {
    
    public static final int console_lenght = 100;

    CopyOnWriteArrayList<String> console;
    
    //RunnableTask instance
    private RunnableTask rt;
    
    //stats
    private long start = 0, end = 0;
    
    private AtomicLong totalQueryTime = new AtomicLong(); //total time spent in actual queriing of db
    private AtomicInteger runs = new AtomicInteger(); //number of times this task was executed
    private AtomicInteger totalCommandsExecuted = new AtomicInteger();
    private AtomicInteger failedQueries = new AtomicInteger(); //number of queries that failed
    
    private ConcurrentHashMap<String, QueryLogger> queryLog = new ConcurrentHashMap<String, QueryLogger>();
    
    //logging
    private File logs;
    private File tasklogs;
    private File logFolder;
    private File logFile;
    
    public TaskLogger(RunnableTask rt) {
        this.rt = rt;
        logFolder = new File("logs/" + rt.getName() + "/" + TimeUtil.getDateFormatFilePath(System.currentTimeMillis()));
        console = new CopyOnWriteArrayList<String>();
        
        //logs
        try {
        logs = new File("logs");
        tasklogs = new File("logs/" + rt.getName());
        logFile = new File(logFolder.getPath() + "/error.log");
        if (!logs.exists()) logs.mkdir();
        if (!tasklogs.exists()) tasklogs.mkdir();
        if (!logFolder.exists()) logFolder.mkdir();
        logFile.createNewFile();
        } catch (IOException ex) {
            this.logWarning("Could not create a log file " + logFile.getPath() + "(" + ex.getMessage() + ")");
        }
        //logs end
    }

    /**
     * Get log folder so we can show it in gui for users where logs are stored
     * @return 
     */
    public File getLogFolder() {
        return logFolder;
    }
    
    /**
     * Exports executin times of each query into CSV file next to the log file
     * @throws IOException 
     */
    public void exportToCSV() throws IOException {
        File csv = new File(logFolder.getPath() + "/query_statistics.csv");
        
        QueryLogger exampleQueryLog = null;
        
        LinkedList<String> queryOrder = new LinkedList(); //keep queries in the same order in case concurrent hash map is reordered
        
        //print column names
        StringBuilder csvbuilder = new StringBuilder();
        for (String s : queryLog.keySet()) {
            queryOrder.add(s);
            if (exampleQueryLog == null) exampleQueryLog = queryLog.get(s);
            csvbuilder.append(s);
            csvbuilder.append(",");
        }
        csvbuilder.append(System.lineSeparator());
        
        if (exampleQueryLog == null) throw new NullPointerException("Could not find any query to export!");
        
        if (exampleQueryLog.getExecTimes().size() == 0) throw new IndexOutOfBoundsException("No data to export at the moment!");
        
        int valueCount = exampleQueryLog.getExecTimes().size(); //export the number before loop because poll is removing the value from the queue
        
        //export times
        for (int i = 0; i < valueCount; i++) {
            for (String query : queryOrder) {
                Long l = queryLog.get(query).getExecTimes().poll();
                if (l != null) {
                    csvbuilder.append(l + "");
                } else {
                    csvbuilder.append("0");
                }
                csvbuilder.append(",");
            }
            csvbuilder.append(System.lineSeparator());
        }
        //finally write to file
        Files.write(Paths.get(csv.getAbsolutePath()), csvbuilder.toString().getBytes(), StandardOpenOption.CREATE_NEW);
    }
    
    private void addToConsole(String msg) {
        console.add(msg);
        if (console.size() > console_lenght) {
            console.remove(0);
        }
    }
    
    private void addToLog(String message) {
        try {
            String formated = "[" + TimeUtil.getConsoleDateFormat(System.currentTimeMillis()) + "]: " + message + "\n";
            Files.write(Paths.get(logFile.getAbsolutePath()), formated.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            addToConsole("Cannot write a log file: " + ex.getMessage());
        }
    }
    
    public void log(String message) {
        String formated = "[" + TimeUtil.getConsoleDateFormat(System.currentTimeMillis()) + "]: " + message;
        addToConsole(formated);
        for (TaskEventListener tel : rt.getListeners()) tel.onConsoleLog(formated); //call event
    }
    
    public void logWarning(String message) {
        String formated = "[" + TimeUtil.getConsoleDateFormat(System.currentTimeMillis()) + "]: [WARNING]: " + message;
        addToConsole(formated);
        for (TaskEventListener tel : rt.getListeners()) tel.onConsoleLog(formated); //call event
    }

    public CopyOnWriteArrayList<String> getConsole() {
        return console;
    }

    public long getTotalQueryTime() {
        return totalQueryTime.get();
    }

    public ConcurrentHashMap<String, QueryLogger> getQueryLogs() {
        return queryLog;
    }
    
    public QueryLogger getQueryLogger(String query) {
        if (queryLog.containsKey(query)) return queryLog.get(query);
        QueryLogger qr = new QueryLogger(query);
        queryLog.put(query, qr);
        return qr;
    }

    public void addTotalQueryTime(String query, long millis) {
        this.totalQueryTime.addAndGet(millis);
        getQueryLogger(query).addSuccessfulQuery(millis);
    }
    
    public void logRun() {
        this.runs.addAndGet(1);
        for (TaskEventListener tel : rt.getListeners()) tel.onLoop();//call event
    }
    
    public void logStart() {
        if (this.start == 0) {
            this.start = System.currentTimeMillis();
            addToLog("Task started at " + TimeUtil.getDateFormat(this.start));
            for (TaskEventListener tel : rt.getListeners()) tel.onStart(); //call event
        }
    }
    
    public void logEnd() {
        this.end = System.currentTimeMillis();
        for (TaskEventListener tel : rt.getListeners()) tel.onStop(); //call event
    }
    
    public void logQuery() {
        this.totalCommandsExecuted.addAndGet(1);
        for (TaskEventListener tel : rt.getListeners()) tel.onQuery(); //call event
    }

    public void logError(String query, Exception e) {
        getQueryLogger(query).addFailedQuery();
        this.failedQueries.addAndGet(1);
        if (e instanceof SQLException) {
            log(e.getClass().getName() + ": " + e.getMessage() + " [Vendor code: " + ((SQLException)e).getErrorCode() + "]");
            addToLog(e.getClass().getName() + ": " + e.getMessage() + " [Vendor code: " + ((SQLException)e).getErrorCode() + "]");
        } else {
            addToLog(e.getClass().getName() + ": " + e.getMessage());
        }
        
        for (TaskEventListener tel : rt.getListeners()) tel.onError(); //call event
    }
    
    /**
     * Total duration of this task in ms
     * @return 
     */
    public long getDuration() {
        if (start == 0) return 0;
        if (this.end == 0)
        return System.currentTimeMillis() - start;
        else 
        return end - start;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getLoops() {
        return runs.get();
    }

    public int getTotalCommandsExecuted() {
        return totalCommandsExecuted.get();
    }

    public int getFailedQueries() {
        return failedQueries.get();
    }
    
}
