package net.qyjohn.dewe.v2;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.text.*;

public class FoxDB
{
	String db_hostname, db_username, db_password, db_database;
	Connection db_connect;
	
	public FoxDB(String hostname, String username, String password, String database)
	{
		db_hostname = hostname;
		db_username = username;
		db_password = password;
		db_database = database;
		
		open_db_connection();
	}
	
	/**
	 *
	 * Create DB Connection
	 *
	 */
	
	public void open_db_connection()
	{
		if (db_connect == null)
		{
			try
			{
				// MySQL Driver
				Class.forName("com.mysql.jdbc.Driver");
				// DB URI
				String conn_string = "jdbc:mysql://" + db_hostname + "/" + db_database 
					+ "?user=" + db_username + "&password=" + db_password;
				// Create DB Connection
				db_connect = DriverManager.getConnection(conn_string);
			} catch (Exception e)
			{
				// Exception Handling
				db_connect = null;
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	public synchronized void execute_update(String sql)
	{
		// Check DB Connection 
		if (db_connect == null) 
		{
			open_db_connection();
		}
		
		// Query DB to make the update
		if (db_connect != null)
		{
			try
			{
				PreparedStatement stat = db_connect.prepareStatement(sql);
				stat.executeUpdate();
			} catch (Exception e)
			{
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}		
	}
	
	public synchronized void add_workflow(String uuid, String name, String directory)
	{
		// Assemble SQL statement
		String sql = "INSERT INTO workflow (uuid, name, directory, submitted, updated, status) VALUES ('"
			+ uuid + "', '" + name + "', '" + directory + "', now(), now(), 'pending')";

		// Update DB
		execute_update(sql);
		sql = null;
	}
	
	public synchronized void update_workflow(String uuid, String status)
	{
		// Assemble SQL statement
		String sql = "UPDATE workflow SET status = '" + status + "', updated = now() WHERE uuid = '" + uuid + "'";

		// Update DB
		execute_update(sql);	
		sql = null;		
	}
	
	
	public synchronized void add_job(String workflow, String jobId, String name)
	{
		// Assemble SQL statement
		String sql = "INSERT INTO jobs (workflow, id, name, started, completed, worker, status) VALUES ('"
			+ workflow + "', '" + jobId + "', '" + name + "', '0000-00-00 00:00:00', '0000-00-00 00:00:00', 'N/A', 'pending')";

		// Update DB
		execute_update(sql);
		sql = null;		
	}
	

	public synchronized void update_job_running(String workflow, String jobId, String worker)
	{
		// Assemble SQL statement
		String sql = "UPDATE jobs SET started = now(), status = 'started', worker = '" + worker + "' WHERE workflow = '" + workflow + "' AND id = '" + jobId + "'";			
		
		// Update DB
		execute_update(sql);
		sql = null;
	}

	
	public synchronized void update_job_completed(String workflow, String jobId)
	{
		// Assemble SQL statement
		String sql = "UPDATE jobs SET completed = now(), status = 'completed' WHERE workflow = '" + workflow + "' AND id = '" + jobId + "'";			

		// Update DB
		execute_update(sql);
		sql = null;
	}
}
