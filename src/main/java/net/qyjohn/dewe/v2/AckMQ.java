/**
 *
 * AckMQ
 *
 * This class listens for ACK messages from all participating worker nodes. There are two types of ACK messages, as follows:
 *
 * (1) a job has been pulled from the job MQ, and should be move from queueJobs to runningJobs
 * (2) a job has completed execution on the worker node, and should be moved from runningJobs to completeJobs
 *
 * This class is only responsible for listening. All the messages received are passed to the corresponding WorkflowScheduler for processing.
 *
 * The format of the ACK message is:
 *
 * workflow UUID, job ID, running / complete, worker node IP
 *
 * The ACK message does not contain a timestamp. Rather, the timestamp is determined by this class when the message is received.
 *
 */
 

package net.qyjohn.dewe.v2;

import java.util.concurrent.ConcurrentHashMap;
import org.dom4j.*;
 
public class AckMQ extends Thread
{
	PullMQ amq;
	ConcurrentHashMap<String, WorkflowScheduler> allWorkflows;
	
	/**
	 *
	 * Constructor
	 *
	 * This constructor is used to create an AckMQ listening for ACK message.
	 *
	 * @param	port	the port number to listen for ACK messages
	 * @param	wf		the HashMap containing all the workflows
	 *
	 */
	 
	public AckMQ(ConcurrentHashMap<String, WorkflowScheduler> wf)
	{
		allWorkflows = wf;
		amq = new PullMQ("localhost", FoxParam.SIMPLE_WORKFLOW_ACK_MQ);
	}

	
	
	/**
	 *
	 * run() method of the thread.
	 * It receives ACK message from the AckMQ, and set the appropriate job as either running or complete. 
	 *
	 */
	 
	public void run()
	{
		String ackString;
		String project, jobId, status, worker;
		Element ack;
		long   unixTime;
		
		while (true)
		{
			try
			{
				// Receive ACK message 
				ackString = amq.pullMQ();
				unixTime = System.currentTimeMillis() / 1000L;
				System.out.println(unixTime + "\t" + ackString);
				ack = DocumentHelper.parseText(ackString).getRootElement();
				project = ack.attribute("project").getValue();
				jobId   = ack.attribute("id").getValue();
				status  = ack.attribute("status").getValue();
				worker  = ack.attribute("worker").getValue();
				
				// An ACK message indicates either the "running" or "complete" status of a particular job
				WorkflowScheduler scheduler = allWorkflows.get(project);
				if (status.equals("running"))
				{
					scheduler.setJobAsRunning(jobId, worker);
				}
				else if (status.equals("complete"))
				{
					scheduler.setJobAsComplete(jobId, worker);
				}
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();	
			}
		}		
	}
}
