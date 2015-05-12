package net.qyjohn.dewe.v2;

import java.util.HashSet;
import java.util.LinkedList;

public class WorkflowScheduler
{
	FoxDB database;
	Workflow wf;
	PushMQ mq;
	int timeout;
	String uuid, projectPath;
	HashSet<String> initialSet, pendingSet, runningSet;
	String jobInfo;
	boolean completed;
	
	public WorkflowScheduler(FoxDB db, String id, PushMQ m, String path, int t)
	{
		database = db;
		uuid = id;
		projectPath = path;
		mq = m;
		timeout = t;
		
		completed  = false;
		initialSet = new HashSet<String>();
		pendingSet = new HashSet<String>();
		runningSet = new HashSet<String>();
		
		wf = new Workflow(path, timeout);
/*		for (WorkflowJob job : wf.initialJobs.values()) 
		{
			// Register the job to the database
			database.add_job(uuid, job.jobId, job.jobName);
		}		
*/
	}
	
	
	public void initialDispatch()
	{
		HashSet<String> queueSet = new HashSet<String>();
		LinkedList<String>	pendingPush = new LinkedList<String>();

		for (WorkflowJob job : wf.initialJobs.values())	
		{
			if (job.ready)
			{
				pendingSet.add(job.jobId);
				jobInfo = createJobInfo(job.jobId, job.jobCommand);
				mq.pushMQ(jobInfo);
			}
			else
			{
				initialSet.add(job.jobId);
			}
		}	
	}
	
	
	/**
	 *
	 * Dispatch a single job from the initialJobs HashSet to the queueJobs HashSet. Jobs in the queueJobs HashSet will be
	 * pulled by the worker nodes for execution.
	 *
	 */
	 
	public synchronized void dispatchJob(String id)
	{
		if (initialSet.contains(id))
		{
			WorkflowJob job = wf.initialJobs.get(id);
			jobInfo = createJobInfo(job.jobId, job.jobCommand);
			mq.pushMQ(jobInfo);	
			initialSet.remove(id);
			pendingSet.add(id);
		}		
	}
	
	
	/**
	 *
	 * The worker node sends an ACK message to the AckMQ, indicating a particular job is now running. 
	 * Move the job from queueJobs HashMap to runningJobs HashMap
	 *
	 */
	 
	public synchronized void setJobAsRunning(String id, String worker)
	{
		if (pendingSet.contains(id))
		{
//			System.out.println(uuid + ":\t" + id + " is running on worker " + worker + ".");
			long current = System.currentTimeMillis() / 1000L;
			WorkflowJob job = wf.initialJobs.get(id);
			job.start_time = current;

			pendingSet.remove(id);
			runningSet.add(id);
//			database.update_job_running(uuid, id, worker);
		}		
	}
	
	
	/**
	 *
	 * The worker node sends an ACK message to the AckMQ, indicating a particular job is now complete.
	 * There are several things to process, including:
	 * (1) obtain a list of the output files of this particular job
	 * (2) for each output file, find the jobs that depend on this output file
	 * (3) for each job that depends on this output file, check if it is now ready to run, and dispatch it if it is ready
	 * (4) move the job from runningJobs HashMap to completeJobs HashMap.
	 *
	 */
	 
	public synchronized void setJobAsComplete(String id, String worker)
	{		
		if (runningSet.contains(id))
		{
			runningSet.remove(id);
//			database.update_job_completed(uuid, id);
			
			WorkflowJob job = wf.initialJobs.get(id);
			
			// Get a list of the children jobs
			for (String child_id : job.childrenJobs) 
			{
				// Get a list of the jobs depending on a particular output file
				WorkflowJob childJob = wf.initialJobs.get(child_id);
				// Remove this depending parent job
				childJob.removeParent(id);
				if (childJob.ready)
				{
					// No more pending input files, this job is now ready to go
//					System.out.println(uuid + ":\t" + childJob.jobId + " is now ready to go. Dispatching...");
					dispatchJob(childJob.jobId);
				}
			}
			
			// Delete this job from initialJobs
			wf.initialJobs.remove(id);
			
			
			// Check if the workflow is completed
			if ((initialSet.size() == 0) && (pendingSet.size() == 0) && (runningSet.size() == 0))
			{				
				completed = true;
				initialSet = null;
				pendingSet = null;
				runningSet = null;
				wf = null;
				
//				System.out.println(uuid + ":\t" +  "[COMPLETED]");
				database.update_workflow(uuid, "completed");
			}						
		}		

		
	}
	
	
	/**
	 *
	 * After a worker takes a particular job for a certain time, but does not ACK this job as complete, the
	 * job is considered as "timeout". Need to dispatch the job again so that another worker node can process
	 * it a second time.
	 *
	 * Remove the job from the runningJobs HashMap, need to pushMQ, and put it back to the queueJobs HashMap.
	 *
	 */
	 
	public synchronized void handleJobTimeout(String id)
	{
		if (runningSet.contains(id))
		{
			runningSet.remove(id);
			pendingSet.add(id);
			
			WorkflowJob job = wf.initialJobs.get(id);
			jobInfo = createJobInfo(job.jobId, job.jobCommand);
			mq.pushMQ(jobInfo);						
			System.out.println(uuid + ":\t" + id + " is now re-submit for execution.");
		}		
	}
	
	
	/**
	 *
	 * Create an MQ message to be pushed to the job queue
	 *
	 */
	 
	public String createJobInfo(String id, String command)
	{
		String info = "<job project='" + uuid + "' id='" + id + "' path='" + projectPath + "'>\n";
		info = info + "<command>\n";
		info = info + command + "\n";
		info = info + "</command>\n";
		info = info + "</job>";

		return info;		
	}
}
