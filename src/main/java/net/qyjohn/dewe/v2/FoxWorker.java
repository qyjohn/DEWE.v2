package net.qyjohn.dewe.v2;

import java.io.*;
import org.dom4j.*;
import java.net.Inet4Address;
import java.lang.management.*;
import java.util.HashSet;
import java.util.Properties;

public class FoxWorker extends Thread
{
	PushMQ amq;	// ACK MQ
	PullMQ jmq;	// JOB MQ
	WorkerPullJob wpj;

	HashSet<String> runningJobs;
	String server, worker;
	int max_cpu, max_thread;
	
	public FoxWorker()
	{
		try
		{
                        // Load configuration from config.properties
                        Properties prop = new Properties();
                        String propFileName = "config.properties";
                        InputStream inputStream = new FileInputStream(propFileName);
                        prop.load(inputStream);

                        // Get the property value and print it out
                        String master = prop.getProperty("master");
                        int cpu_factor = Integer.parseInt(prop.getProperty("cpu_factor"));
                        String secret = prop.getProperty("secret");
                        
                        // Determine the maximum number of task threads
                        OperatingSystemMXBean mb = ManagementFactory.getOperatingSystemMXBean();
                        max_cpu = mb.getAvailableProcessors();
                        max_thread = cpu_factor * max_cpu;
                        
			worker = Inet4Address.getLocalHost().getHostAddress();			
			amq = new PushMQ(master, FoxParam.SIMPLE_WORKFLOW_ACK_MQ);
			wpj = new WorkerPullJob(master, worker, FoxParam.SIMPLE_WORKFLOW_JOB_MQ, amq);
	
			runningJobs = new HashSet<String>();		
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	

//	public synchronized void execTask()
	public void execTask()
	{
		try
		{
			Element job = wpj.pullJob();	
			String project = job.attribute("project").getValue();
			String path = job.attribute("path").getValue();
			String jobId = job.attribute("id").getValue();
			String command = job.element("command").getText().trim();
	
			// Add the current running task to local runningJobs HashSet
			addJob(project+jobId);
//			runningJobs.add(jobId);
			// Start a new thread to run the task
			new WorkerThread(amq, worker, runningJobs, max_cpu, job).start();	
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized void addJob(String jobId)
	{
		runningJobs.add(jobId);
	}
	
	public void run()
//	public static void main(String[] args)
	{
		try
		{
			while (true)
			{
				try
				{
					while (runningJobs.size() >= max_thread)
					{
						Thread.sleep(10);
					}					
					// Get next task and execute
					execTask();					
				} catch (Exception e1)
				{
					System.out.println(e1.getMessage());
					e1.printStackTrace();					
				}
			}			
		} catch (Exception e2)
		{
			System.out.println(e2.getMessage());
			e2.printStackTrace();
		}
	}
}
