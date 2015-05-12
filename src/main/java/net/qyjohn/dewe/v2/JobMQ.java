package net.qyjohn.dewe.v2;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class JobMQ
{
	public static String SIMPLE_WORKFLOW_JOB_MQ = "JobMQ";
	public static String SIMPLE_WORKFLOW_ACK_MQ = "AckMQ";
	public static String SIMPLE_WORKFLOW_PRJ_MQ = "PrjMQ";
	public static String SIMPLE_WORKFLOW_PUSH = "push";
	public static String SIMPLE_WORKFLOW_PULL = "pull";

	Connection connection;
	Channel channel;
	QueueingConsumer consumer;
	String queue;
	
  public JobMQ(String server, String queue, String action)
  {
	  this.queue = queue;
	  try
	  {
		  ConnectionFactory factory = new ConnectionFactory();
		  factory.setHost(server);
		  connection = factory.newConnection();
		  channel = connection.createChannel();	  		  
		  channel.queueDeclare(queue, false, false, false, null);
		  
		  
		  if (action.equals("pull"))
		  {
			  consumer = new QueueingConsumer(channel);
			  channel.basicConsume(queue, true, consumer);				  
		  }
	  } catch (Exception e)
	  {
		  System.out.println(e.getMessage());
		  e.printStackTrace();
	  }
  }
  
	public void pushMQ(String msg)
	{
		try
		{
			System.out.println(msg);
			channel.basicPublish("", queue, null, msg.getBytes());
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}  
	
	public String pullMQ()
	{
		String msg = "";
		try
		{
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			msg = new String(delivery.getBody());
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return msg;
	}
  
}
