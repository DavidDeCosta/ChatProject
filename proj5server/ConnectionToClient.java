import java.io.*;
import java.net.Socket;

class ConnectionToClient implements Runnable 
{
    Socket clientSocket;
    BufferedReader instream;
    DataOutputStream outStream;
    Thread thread;
    String id;
    Talker talker;
    String [] information;
    String signUpOrRegister;
    String clientID;
    String clientPassword;
    User user;

    MyUserList userList;

    ConnectionToClient(Socket clientSocket, MyUserList userList) throws IOException {
        this.clientSocket = clientSocket;
        this.userList = userList;                                                           // store MyUserList object
        this.thread = new Thread(this);                                                    //  create a new thread for the client
        talker = new Talker(clientSocket);
        thread.start(); 
    }

    @Override
    public void run() 
    {
        try 
        {
        //    outStream.writeBytes("Hello Client, I am the Server \n");                  
            String message;
            message = talker.receiveMessage();                                                              // read message from client
            information = message.split(" ");
            signUpOrRegister = information[0];      
      //      System.out.println("SignUpOrRegister: " + information[0]);      
            clientID = information[1];
      //      System.out.println("clientID: " + information[1]);
            clientPassword = information[2];
     //       System.out.println("clientPassword: " + information[2]);
            
            if(signUpOrRegister.equals("register"))
            {
                if(userList.isUsernameInUse(clientID))
                {
                    talker.sendMessage("register failed");
                    System.out.println("username already in use");
                    return;
                }
                else
                {
                user = new User(clientID, clientPassword);
                userList.put(clientID, user);
                talker.sendMessage(" register success");
                }
            }
            else if(signUpOrRegister.equals("login"))
            {
                user = userList.get(clientID);
                if(user != null && user.password.equals(clientPassword))
                {
                    talker.sendMessage("login success");
                    System.out.println("Login success");
                }
                else
                {
                    System.out.println("Login failed");
                    talker.sendMessage("login failed");
                }
            }
            else
            {
                System.out.println("Error: " + signUpOrRegister);
            }

            while (true) 
            {
              message = talker.receiveMessage();    // wait for a message from the client after getting the credentials

            }
        } 
        catch (IOException e) 
        {
            System.out.println("Error reading or writing to client (" + id + "): " + e.getMessage());
                                                  
        }
    }
}
