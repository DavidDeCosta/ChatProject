import java.io.*;
import java.net.*;

class TheServer 
{
    ServerSocket serverSocket;
    Socket clientSocket;
    ConnectionToClient connection;
    User user;
    MyUserList userList;            // used to store the user list

    TheServer()
    {
        System.out.println("Server is running...");
        userList = new MyUserList();
        doesHashTableExist();
        setupServer();
    }

    void setupServer()
    {
        try
        {
            serverSocket = new ServerSocket(12345);
            System.out.println("Waiting for client to connect...");

            while(true)
            {
            clientSocket = serverSocket.accept();                                                     //clientSocket is the socket that connects to the client
            connection = new ConnectionToClient(clientSocket, userList);                              //create a new connection to the client
            saveUserList();                                                                         // save the user list every time a new client connects
            }
        }
        catch(IOException e)
        {
            System.out.println("Error setting up server: " + e.getMessage());
        }
    }


    void doesHashTableExist()
    {
        File file = new File("userList.txt");
        userList = new MyUserList(); // Initialize userList in both cases
        if(file.exists()) // If the user list file exists
        {
            try
            {
                DataInputStream load = new DataInputStream(new FileInputStream("userList.txt"));     
                userList.load(load); // Load the user list
            }
            catch(IOException e)
            {
                System.out.println("Error loading the user list: " + e.getMessage());
            }
        }
    }

    void saveUserList() 
    {
        try 
        {
            DataOutputStream save = new DataOutputStream(new FileOutputStream("userList.txt"));
            userList.save(save);                                                                         
        } 
        catch (IOException e) 
        {
            System.out.println("Error saving the user list: " + e.getMessage());
        }
    }

}