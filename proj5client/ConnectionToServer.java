import java.io.IOException;
import java.net.*;

class ConnectionToServer implements Runnable  // this class is used to create a new thread for each client
{
    String message;
    Socket normalSocket;        // socket used to communicate with client
    boolean recieved = false;   
    Talker talker;


    ConnectionToServer(Socket normalSocket, String message) throws IOException
    {
        this.normalSocket = normalSocket;
        this.message = message;
        talker = new Talker(normalSocket);                                // create a talker for this client                        
        new Thread(this).start();                                        //client gets its own thread
    }

    @Override
    public void run() 
    {
        try 
        {
            talker.sendMessage(message);

            while (true) 
            {
                talker.receiveMessage();                        //always ready to receive message from server
                if(recieved == true)
                {

                }
            } 
        }
        catch (IOException e) 
        {
            System.out.println("Connection to server lost");
        }
}
}
 