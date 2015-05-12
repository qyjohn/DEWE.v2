package net.qyjohn.dewe.v2;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try
        {
            if (args[0].equals("master"))
            {
                System.out.println( "Starting DEWE.v2 master daemon..." );
				new FoxServer().start();
            }
            else if (args[0].equals("worker"))
            {
                System.out.println( "Starting DEWE.v2 worker daemon..." );
                new FoxWorker().start();
            }
            else if (args[0].equals("monitor"))
            {
                System.out.println( "Starting DEWE.v2 load monitoring daemon..." );
                new LoadMonitor(args[1]).start();
            }
            else if (args[0].equals("submit"))
            {
                System.out.println( "Starting DEWE.v2 worker daemon..." );
                new FoxSubmit().submit(args[1], args[2]);
            }
            else
            {
                System.out.println( "Invalid startup parameter. Exiting..." );
            }
        } catch (Exception e)
        {
            System.out.println( "Invalid startup parameter. Exiting..." );
        }
    }
}
