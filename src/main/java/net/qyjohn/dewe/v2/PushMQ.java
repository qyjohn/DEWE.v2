package net.qyjohn.dewe.v2;

import java.io.*;
import com.rabbitmq.client.*;

public class PushMQ
{
	Connection connection;
	Channel channel;
	QueueingConsumer consumer;
	String mq_name;

	public PushMQ(String master, String name)
	{
		mq_name = name;
	  try
	  {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(master);
			connection = factory.newConnection();
			channel = connection.createChannel();	  		  
			channel.queueDeclare(mq_name, false, false, false, null);
			channel.txSelect();
	  } catch (Exception e)
	  {
		  System.out.println(e.getMessage());
		  e.printStackTrace();
	  }
		
	}

	// Multiple threads will need tpu pushMQ
	
	public synchronized void pushMQ(String msg)
	{		
		try
		{
			channel.basicPublish("", mq_name, MessageProperties.PERSISTENT_BASIC, msg.getBytes());
			channel.txCommit();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
