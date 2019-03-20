/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.runnable;

import dbstresstest.util.TimeUtil;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Lukas Hanusek
 */
public class QueryLogger {
    
    private ConcurrentLinkedQueue<Long> execTimes; //list of execution times in ms
    
    private AtomicInteger failedQueries = new AtomicInteger(); //number of queries that failed
    
    private long lastFailTime = 0; 
    private long lastSuccessTime = 0; 
    
    long averageTime = 0;
    
    private String queryName;
    
    public QueryLogger(String name) {
        this.queryName = name;
        execTimes = new ConcurrentLinkedQueue<Long>();
    }

    public String getQueryName() {
        return queryName;
    }

    public ConcurrentLinkedQueue<Long> getExecTimes() {
        return execTimes;
    }
    
    public void addFailedQuery() {
        this.failedQueries.addAndGet(1);
        lastFailTime = System.currentTimeMillis();
    }
    
    public void addSuccessfulQuery(long execTime) {
        this.execTimes.add(execTime);
        lastSuccessTime = System.currentTimeMillis();
        averageTime = (long)execTimes.stream().mapToLong(val -> val).average().orElse(0); //recalculate avg times
    }
    
    public int getTotalSuccessfulQueries() {
        return execTimes.size();
    }
    
    public long getAvgExecTime() {
        return averageTime;
    }
    
    public AtomicInteger getFailedQueries() {
        return failedQueries;
    }

    public long getLastFailTime() {
        return lastFailTime;
    }
    
    public long getLastSuccessTime() {
        return lastSuccessTime;
    }
    
    public String getLastFailTimeString() {
        if (lastFailTime == 0) return "-";
        return TimeUtil.getDateFormat(lastFailTime);
    }
    
    public String getLastSuccessTimeString() {
        if (lastSuccessTime == 0) return "-";
        return TimeUtil.getDateFormat(lastSuccessTime);
    }

    public void setLastFailTime(long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }

    public void setLastSuccessTime(long lastSuccessTime) {
        this.lastSuccessTime = lastSuccessTime;
    }
    
}
