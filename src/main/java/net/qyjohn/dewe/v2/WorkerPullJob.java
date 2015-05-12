package net.qyjohn.dewe.v2;

import java.io.*;
import org.dom4j.*;
import com.rabbitmq.client.*;

public class WorkerPullJob
{
	Connection connection;
	Channel channel;
	QueueingConsumer consumer;
	String master, worker, mq_name;
	PushMQ amq;

	public WorkerPullJob(String m, String w, String name, PushMQ mq)
	{
		mq_name = name;
		master = m;
		worker = w;
		amq = mq;
		
		try
	  {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(master);
			connection = factory.newConnection();
			channel = connection.createChannel();	  		  
			channel.queueDeclare(mq_name, false, false, false, null);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(mq_name, false, consumer);			// No AutoACK for messages	  
	  } catch (Exception e)
	  {
		  System.out.println(e.getMessage());
		  e.printStackTrace();
	  }
	}

	public Element pullJob()
	{
		Element job = null;
		String msg = "";

		try
		{
			// Get the next job
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			msg = new String(delivery.getBody());

			// Parse the job information
			job = DocumentHelper.parseText(msg).getRootElement();
			String project = job.attribute("project").getValue();
			String jobId = job.attribute("id").getValue();
			
			// Send out ACK message, indicating that this job is running.
			String ackInfo = "<ack project='" + project +"' id='" + jobId + "' status='running' worker='" + worker + "'/>";
			amq.pushMQ(ackInfo);
						
			// Send RabbitMQ ACK
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);	// ACK the message
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return job;
	}
}
