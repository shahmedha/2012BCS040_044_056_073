//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.inf.usi.ch/carzaniga/siena/
//
//  Author: Giovanni Toffetti Carughi
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 2007, 2011 Giovanni Toffetti Carughi
//
//  Siena is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//  
//  Siena is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with Siena.  If not, see <http://www.gnu.org/licenses/>.
//
package siena;

import java.util.TreeMap;

/**
 * implementation of a heartbeat thread for queuing distance vector messages to
 * neighbor nodes.
 * 
 * 
 */
public class SFFHeartbeat implements Runnable {

    public static int DEFAULT_CHOKE_PERIOD = 250; // in milliseconds

    protected int chokePeriod = DEFAULT_CHOKE_PERIOD;

    protected volatile boolean keepRunning;

    private volatile HierarchicalDispatcher server = null;

    protected TaskQueue queue = new TaskQueue();

    public static final byte UpdateFWTable = 0;

    protected Thread localThread;

    public SFFHeartbeat(HierarchicalDispatcher server, int chokePeriod) {
	super();
	this.server = server;
	this.chokePeriod = chokePeriod;
    }

    public SFFHeartbeat(HierarchicalDispatcher server) {
	super();
	this.server = server;
    }

    public void startHeartbeat() {
	keepRunning = true;
	localThread = new Thread(this);
	localThread.start();
    }

    public void run() {

	while (keepRunning) {
	    // Logging.prlnlog(new String(server.my_identity)
	    // + " SFFheartbeat run queue: " + queue, Logging.DEBUG);
	    if (!queue.isEmpty()) {
		// check what is the next task
		long nextTaskTime = (queue.keySet().iterator().next()).longValue();
		try {
		    long sleepTime = nextTaskTime - System.currentTimeMillis();
		    if (sleepTime > 0) {
			Thread.sleep(sleepTime);
		    }
		} catch (InterruptedException e) {
		    /*
		     * we get awakened whether
		     * there's a new task in the
		     * queue or whether we shut
		     * down in the latter case the
		     * queue should have been
		     * emptied and we return
		     */
		    if (queue.isEmpty() && !keepRunning) {
			return;
		    }
		    // otherwise we go back restarting the cycle
		    continue;
		}
		long taskTime = (queue.keySet().iterator().next()).longValue();
		// here we wake up from the sleep and have to execute a task
		int task = (queue.get(Long.valueOf(taskTime))).intValue();
		// Logging.prlnlog(new String(server.my_identity)
		// + " heartbeat executing task " + task, Logging.DEBUG);
		switch (task) {
		case (SFFHeartbeat.UpdateFWTable):
		    // switch DistanceVector with ForwardingTable
		    synchronized (server) {
			server.updateFWTable();
		    }
		    break;
		}
	    } else {
		try {
		    synchronized (queue) {
			queue.wait();
		    }
		} catch (InterruptedException e) {
		    // a UDV was added to the queue
		    // restart the cycle
		    Logging.prlnlog(new String(server.getIdentity()) + " heartbeat interrupted while waiting on empty queue", Logging.DEBUG);
		    continue;
		}
	    }
	}
    }

    public void queueSubscriptionUpdate() {
	// first we check if another UDV was scheduled to happen within this
	// choke period
	synchronized (queue) {
	    if (queue.containsValue(Integer.valueOf(SFFHeartbeat.UpdateFWTable))) {
		if (Logging.severity == Logging.DEBUG) {
		    Logging.prlnlog(new String(server.my_identity) + " heartbeat not queuing SubUpdate because already queued", Logging.DEBUG);
		}
		return;
	    } else {
		if (Logging.severity == Logging.DEBUG) {
		    Logging.prlnlog(new String(server.my_identity)
				    + " heartbeat queueing SubUpdate in " 
				    + chokePeriod
				    + " millis", Logging.DEBUG);
		}
		// otherwise we queue it
		queue.put(Long.valueOf(System.currentTimeMillis() + chokePeriod), Integer.valueOf(SFFHeartbeat.UpdateFWTable));
		// Logging.prlnlog(new String(server.my_identity)
		// + " heartbeat queue: " + queue, Logging.DEBUG);
		// then we wake up any waiting thread
		queue.notifyAll();
		localThread.interrupt();
	    }
	}
    }

    public void stopHeartbeat() {
	keepRunning = false;
	synchronized (queue) {
	    queue.clear();
	}
	localThread.interrupt();
    }

    public static long randPoissonDelta(long mean) {
	double rn = 1 - Math.random() / (1 + 1E-300);
	return (long) (-(1.0 * mean) * Math.log(rn));
    }

    public int getChokePeriod() {
	return chokePeriod;
    }

    public void setChokePeriod(int chokePeriod) {
	this.chokePeriod = chokePeriod;
    }

    public void queueUpdate(byte method) {
	// does nothing for SFFHeartbeat

    }

}

/**
 * This is simply a wrapper to a map, but we want to avoid task timestamps from
 * getting overwritten
 */
class TaskQueue extends TreeMap<Long, Integer> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public synchronized Integer put(Long time, Integer task) {
	    // avoid timestamp collision
	    while (containsKey(time)) {
		time++;
	    }
	    return super.put(time, task);
	}

}
