package net.qyjohn.dewe.v2;

import java.util.HashMap;
import java.util.HashSet;

public class WorkflowTimeoutChecking extends Thread
{
	HashMap<String, WorkflowScheduler> allWorkflows;
	int timeout;
	
	public WorkflowTimeoutChecking(HashMap<String, WorkflowScheduler> wf, int t)
	{
		allWorkflows = wf;
		timeout = t;
	}
	
	
	public void run()
	{
		while (true)
		{
			try
			{	
				long current = System.currentTimeMillis() / 1000L;
				// Traverse through all workflows
				for (String key1 : allWorkflows.keySet()) 
				{
					// Get the current workflow scheduler
					WorkflowScheduler wfc = allWorkflows.get(key1);
					if (!wfc.completed)
					{
						// Get the running jobs in the workflow
						HashSet<String> runningJobs = wfc.runningSet;
						HashSet<String> offending_jobs = new HashSet<String>();
						// Traverse through all running jobs
						for (String key2 : runningJobs)
						{
							WorkflowJob job = wfc.wf.initialJobs.get(key2);
							// Check how long this has been running
							int job_timeout = job.timeout;
							int job_runtime = (int) (current - job.start_time);
							// Check if there is a timeout
							if (job_runtime > job_timeout)
							{
								System.out.println(job.jobId + "\t" + job_timeout + "\t" + job_runtime + "\t timeout....");
								offending_jobs.add(job.jobId);
							}
						}
						
						for (String jobId : offending_jobs)
						{
							wfc.handleJobTimeout(jobId);
						}
						
					}
/*					
					// Get the running jobs in the workflow
					HashMap<String, WorkflowJob> runningJobs = wfc.wf.runningJobs;
					HashSet<String> offending_jobs = new HashSet<String>();
					// Traverse through all running jobs
					for (String key2 : runningJobs.keySet())
					{
						WorkflowJob job = runningJobs.get(key2);
						// Check how long this has been running
						int job_timeout = job.timeout;
						int job_runtime = (int) (current - job.start_time);
						// Check if there is a timeout
						if (job_runtime > job_timeout)
						{
							System.out.println(job.jobId + "\t" + job_timeout + "\t" + job_runtime + "\t timeout....");
							offending_jobs.add(job.jobId);
						}
					}
					
					for (String jobId : offending_jobs)
					{
						wfc.handleJobTimeout(jobId);
					}
*/

										
				}				
				sleep(10000);	// sleep 10 seconds
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
