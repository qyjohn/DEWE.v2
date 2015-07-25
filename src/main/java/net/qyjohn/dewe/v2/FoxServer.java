package net.qyjohn.dewe.v2;

import java.io.*;
import java.util.UUID;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class FoxServer extends Thread
{
	public void run()
//	public static void main(String args[])
	{
		try
		{
			// Load configuration from config.properties
			Properties prop = new Properties();
			String propFileName = "config.properties";
			InputStream inputStream = new FileInputStream(propFileName);
			prop.load(inputStream);

			// Get the properties
			int cpu_factor  = Integer.parseInt(prop.getProperty("cpu_factor"));
			int job_timeout = Integer.parseInt(prop.getProperty("job_timeout"));
			String secret = prop.getProperty("secret");
			String db_hostname = prop.getProperty("db_hostname");
			String db_username = prop.getProperty("db_username");
			String db_password = prop.getProperty("db_password");
			String db_database = prop.getProperty("db_database");
			
			// Create a HashMap to hold all the workflow schedulers, each workflow scheduler handles one workflow
			ConcurrentHashMap<String, WorkflowScheduler> allWorkflows = new ConcurrentHashMap<String, WorkflowScheduler>();
			
//			HashMap<String, WorkflowScheduler> allWorkflows = new HashMap<String, WorkflowScheduler>();
			
			// Create an AckMQ
			// The HashMap for all workflow schedulers is needed, because the AckMQ needs to pass the messages received
			// to the workflow schedulers. The ACK message contains the UUID of the workflow scheduler, so that AckMQ
			// knows which workflow scheduler should be called for a particular ACK message.
			AckMQ ack = new AckMQ(allWorkflows);
			ack.start();
			// Create a PushMQ to publish jobs			
	    	PushMQ jmq = new PushMQ("localhost", FoxParam.SIMPLE_WORKFLOW_JOB_MQ);
	    	
	    	
	    	// Establish database connection
	    	FoxDB db = new FoxDB(db_hostname, db_username, db_password, db_database);
	    	
	    	// Waiting for projects to be submitted through PrjMQ
	    	PrjMQ prj = new PrjMQ(allWorkflows, db, jmq, job_timeout);
	    	prj.start();
	    	
	    	// Checking job timeouts
	    	WorkflowTimeoutChecking timeout_checking = new WorkflowTimeoutChecking(allWorkflows, job_timeout);
	    	timeout_checking.start();

			while (true)
			{
				Thread.sleep(1000);
			}
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();			
		}
	}
}
