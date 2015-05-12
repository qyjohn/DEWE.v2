package net.qyjohn.dewe.v2;

import java.io.*;
import org.dom4j.*;
import java.util.HashSet;


public class WorkerThread extends Thread
{
	String jobString, server, worker, project, projectPath, jobId, jobCommand;
	String fullCommand, workDir;		
	HashSet<String> runningJobs;
	int max_omp_thread;
	PushMQ amq;
	
	
	public WorkerThread(PushMQ mq, String w, HashSet<String> running, int max_thread, Element job)
	{
		amq = mq;
		worker = w;
		runningJobs = running;
		max_omp_thread = max_thread;
		
		project = job.attribute("project").getValue();
		projectPath = job.attribute("path").getValue();
		jobId = job.attribute("id").getValue();
		jobCommand = job.element("command").getText().trim();
		fullCommand = projectPath + "/bin/" + jobCommand;
		workDir = projectPath + "/workdir";
	}
	
	/**
	 *
	 * Method to execute a task.
	 * @param path	    The path to the project directory. Under the project directory there are two sub-directories. <path>/bin 
	 *                  contains all the binary executable programs, and <paht>/workdir contains all the input / output files.
	 * @param command   The command with all the parameters
	 *
	 */
	 
//	public synchronized void exec(String path, String command)
	public void exec(String path, String command)
	{
		try
		{
			String env_path = "PATH=" + path + "/bin:$PATH";
			String env_lib  = "LD_LIBRARY_PATH=" + path + "/lib:$LD_LIBRARY_PATH";
			String env_openmp = "OMP_NUM_THREADS=" + max_omp_thread;
			String[] env = {env_path, env_lib, env_openmp};
			String result = "";
			
			Process p = Runtime.getRuntime().exec(fullCommand, env, new File(workDir));

			// Capture the output
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) 
			{
				result = result + line + "\n";
			}       
			in.close();

			p.waitFor();
			removeJob(jobId);
//			runningJobs.remove(jobId);
			p=null;
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized void removeJob(String jobId)
	{
		runningJobs.remove(jobId);		
	}
	
	
	public void run()
	{
		String ackInfo;
		
		try
		{			
//			System.out.println(project + ":\t" + jobId);
//			System.out.println(projectPath + "/bin/" + jobCommand);
			exec(projectPath, jobCommand);

			// Send out ACK message, indicating that this job is complete.
			ackInfo = "<ack project='" + project +"' id='" + jobId + "' status='complete' worker='" + worker + "'/>";
			amq.pushMQ(ackInfo);	
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
