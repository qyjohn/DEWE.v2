package net.qyjohn.dewe.v2;

/**
 *
 * A simple utility to record timestamp and system load.
 *
 */
 
import java.lang.management.*;
import javax.management.*;
import java.io.*;
import java.util.StringTokenizer;

public class LoadMonitor extends Thread
{

	String dev = "xvdf";
	
	public LoadMonitor(String target)
	{
		dev = target;
	}
	
    public static double[] sysUsage (int seconds, String dev) throws Exception 
    {          
    	Runtime runtime = Runtime.getRuntime();
        BufferedReader reader = null;
        StringTokenizer st;
        String  command, infoLine, loadString = "0.00";
		double[] load = new double[5]; 	// concurrent threads, cpu load, iops, read, write

        try 
        {
			// Concurrent threads
			OperatingSystemMXBean mbean = ManagementFactory.getOperatingSystemMXBean();
			load[0] = mbean.getSystemLoadAverage();
        
			// Parsing mpstat
            command = "mpstat 1 1";
            reader = new BufferedReader(new InputStreamReader(runtime.exec(command).getInputStream()));
            reader.readLine();
            reader.readLine();
            reader.readLine();
            infoLine = reader.readLine().trim();
            st = new StringTokenizer(infoLine, " ");
            while (st.hasMoreTokens())
            {
	            loadString = st.nextToken();
            }
	        load[1] = 100 - Double.parseDouble(loadString);
			
			// Parsing iostat
            command = "iostat -d 1 2 "+ dev;
            reader = new BufferedReader(new InputStreamReader(runtime.exec(command).getInputStream()));
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            infoLine = reader.readLine().substring(8).trim();
            st = new StringTokenizer(infoLine, " ");
            loadString = st.nextToken();
	        load[2] = Double.parseDouble(loadString);
            loadString = st.nextToken();
	        load[3] = Double.parseDouble(loadString);
            loadString = st.nextToken();
	        load[4] = Double.parseDouble(loadString);
        } catch (Exception e)
        {
	        
        }    
        
        return load;
    }

//	public static void main(String[] args)
	public void run()
	{
		int	   period = 1; // default 1 second
		int    sleep = 1000;
		long   unixTime;
		double[] load;
		
		while (true)
		{
			try
			{
				unixTime = System.currentTimeMillis() / 1000L;
				load = LoadMonitor.sysUsage(period, dev);
				System.out.println(unixTime + "\t" + load[0] + "\t" + load[1] + "\t" + load[2] + "\t" + load[3] + "\t" + load[4]);
				Thread.sleep(sleep);
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
