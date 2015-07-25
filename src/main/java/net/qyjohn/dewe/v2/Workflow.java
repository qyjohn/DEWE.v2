package net.qyjohn.dewe.v2;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;


public class Workflow
{

	ConcurrentHashMap<String, WorkflowJob> initialJobs, queueJobs, runningJobs;
//	completeJobs;
	ConcurrentHashMap<String, Integer> timeoutMap;
	String projectDir;
	int timeout;
	
	SAXReader reader;
	Document document;
	URL url;

	
	/**
	 *
	 * Constructor
	 *
	 */
	 
	public Workflow(String dir, int t)
	{
		timeout = t;
		reader = new SAXReader();
		
		try
		{
			// Initialize the HashMap for workflow jobs
			initialJobs = new ConcurrentHashMap<String, WorkflowJob>();
			queueJobs = new ConcurrentHashMap<String, WorkflowJob>();
			runningJobs = new ConcurrentHashMap<String, WorkflowJob>();
//			completeJobs = new HashMap<String, WorkflowJob>();

			projectDir = dir;
			
			timeoutMap = new ConcurrentHashMap<String, Integer>();
			String timeoutFile = projectDir + "/timeout.xml";	// timeout definition for jobs
			File f = new File(timeoutFile);
			if (f.exists())
			{
				String timeout_definition_file_path = "file:///" + projectDir + "/timeout.xml";		// workflow DAG
				parseDocument(timeout_definition_file_path);
//				parseTimeout(document);
				parseTimeout();
			}
			
			String fullPath = "file:///" + projectDir + "/dag.xml";		// workflow DAG
			parseDocument(fullPath);
//			parseWorkflow(document);	
			parseWorkflow();	
//			job_doc = null;		
		} catch (Exception e)
		{
			System.out.println(e.getMessage());	
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 * Parse the work flow from dag.xml.
	 *
	 */
         
//	public Document parseDocument(String fullPath) throws Exception 
	public void parseDocument(String fullPath) throws Exception 
	{
//		SAXReader reader = new SAXReader();
		url = new URL(fullPath);
		document = reader.read(url);
//		return document;
	}
	
	/**
	 *
	 * parse job timeout definitions
	 *
	 */
	 
//	public void parseTimeout(Document doc)
	public void parseTimeout()
	{
		List<Element> jobs = document.getRootElement().elements("job");
		for (Element job : jobs)
		{
			try
			{
				String name = job.attribute("name").getValue();
				Integer time = new Integer(job.attribute("timeout").getValue());
				timeoutMap.put(name, time);				
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		jobs = null;
	}
	
	/**
	 *
	 * Parse jobs and job dependencies
	 *
	 */
	 
//	public void parseWorkflow(Document doc)
	public void parseWorkflow()
	{
		List<Element> jobs = document.getRootElement().elements("job");
		List<Element> children = document.getRootElement().elements("child");

		for(Element job : jobs) 
		{
			prepareJob(job);
		}
		for(Element child : children) 
		{
			prepareChild(child);
		}
		jobs = null;
		children = null;
	}
	
	
	/**
	 *
	 * Parse the dependencies of a job
	 *
	 */
	 
	public void prepareChild(Element child)
	{
		String child_id = child.attribute("ref").getValue();
		List<Element> parents = child.elements("parent");
		
		for (Element parent: parents)
		{
			String parent_id = parent.attribute("ref").getValue();
			initialJobs.get(child_id).addParent(parent_id);
			initialJobs.get(parent_id).addChild(child_id);
		}
	}

	
	/**
	 *
	 * Parse a job, extract job name (command) and command line arguments
	 *
	 */
	 
	public void prepareJob(Element job)
	{
		String id = job.attribute("id").getValue();
		String name = job.attribute("name").getValue();
		
		WorkflowJob wlj = new WorkflowJob(id, name, timeout);
		if (timeoutMap.containsKey(name))
		{
			// Customer define timeout available for this particular job
			wlj.timeout = timeoutMap.get(name).intValue();
		}
//		WorkflowJob wlj = new WorkflowJob(id, name, timeout);
		Element args = job.element("argument");
		
		Node node;
		Element e;
		StringTokenizer st;
		for ( int i = 0, size = args.nodeCount(); i < size; i++ )
		{
			node = args.node(i);
			if ( node instanceof Element ) 
			{
                e = (Element) node;
                wlj.addArgument(e.attribute("file").getValue());
            }
            else
            {
	            st = new StringTokenizer(node.getText().trim());
				while (st.hasMoreTokens()) 
				{
					wlj.addArgument(st.nextToken());
				}
            }
		}
	
		initialJobs.put(id, wlj);
	}
}
