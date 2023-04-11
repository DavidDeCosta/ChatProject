import java.io.*;
import java.net.Socket;

class ConnectionToClient implements Runnable 
{
    Socket clientSocket;
    DataInputStream instream;
    DataOutputStream outStream;
    Thread thread;
    String id;
    Talker talker;
    String [] information;
    String command;
    String clientID;
    String clientPassword;
    User user;
    User potentialUser;
    String response;

   // TheServer theServer;

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
      //  boolean running = true;
        try 
        {         
            while(true)
            {  
                String message;
                message = talker.receiveMessage();                                                              // read message from client
                command = "";  // reset command
                if(message.startsWith("register") || message.startsWith("login"))
                {
                    information = message.split(" ");
                    command = information[0];
                    clientID = information[1];
                    clientPassword = information[2];
                }
                else if(message.startsWith("addfriend"))
                {
                    information = message.split(" ");
                    command = information[0];
                    clientID = information[1];
                }
                else if(message.startsWith("addFriendResponse"))
                {
                    information = message.split(" ");
                    command = information[0];
                    clientID = information[1];
                    response = information[2];
                }
                else
                {

                }

                if(command.equals("register"))
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
                    user.connection = this;
                    userList.put(clientID, user);
                    talker = new Talker(clientSocket, user);
                    talker.sendMessage(" register success");

                    potentialUser = user;
                    }
                }
                else if(command.equals("login"))
                {
                    user = userList.get(clientID);
                    if(user != null && user.password.equals(clientPassword))
                    {
                        if(!user.isLoggedIn())
                        {
                        user.connection = this;
                        userList.put(clientID, user);
                        talker = new Talker(clientSocket, user);
                        talker.sendMessage("login success");
                     //   instream = new DataInputStream(new FileInputStream("userList.txt"));
                     //   user.load(instream);
                        for(String buddy : user.buddylist )
                        {
                            System.out.println("buddy: " + buddy);
                            talker.sendMessage("addFriendSuccess " + buddy );
                        }
                        user.loggedIn = true;
                      //  System.out.println("Login success");
                        }
                        else
                        {
                            talker.sendMessage("already logged in");
                        }
                    }
                    else
                    {
                        System.out.println("Login failed");
                        talker.sendMessage("login failed");
                    }
                }
                else if(command.equals("addfriend"))
                {
                    String potentialFriend = clientID;
                    if(userList.isUsernameInUse(potentialFriend))
                    {
                        potentialUser = userList.get(potentialFriend); // get the User object of the potential friend
                        potentialUser.initiatorUserName = user.userName;
                        potentialUser.connection.talker.sendMessage("addfriend " + user.userName + " " + potentialFriend); // send the addfriend command to the potential friend
                    }
                    else
                    {
                        //talker.sendMessage("addfriend failed");
                    }
                }
                else if(command.equals("addFriendResponse"))
                {
                    String potentialFriend = clientID;
                    if(response.equals("0"))
                    {
                        user.buddylist.add(potentialFriend);  // add friend to buddylist with the key being the username and the value being the User object
                        potentialUser = userList.get(potentialFriend); // get the User object of the potential friend
                        potentialUser.buddylist.add(user.userName); // add the user to the potential friend's buddylist   so now both users have each other in their buddylist
                        User initiatorUser = userList.get(potentialUser.initiatorUserName); // get the User object of the initiator
                        initiatorUser.connection.talker.sendMessage("addFriendSuccess " + user.userName + " " + potentialUser.initiatorUserName); // send  command to the initiator
                        //userList.save(outStream);
                        try 
                        {
                            DataOutputStream save = new DataOutputStream(new FileOutputStream("userList.txt"));
                            userList.save(save);                                                                         
                        } catch (IOException e) 
                        {
                            System.out.println("Error saving the user list: " + e.getMessage());
                        }
                    }
                    else
                    {
                        potentialUser = userList.get(potentialFriend); // get the User object of the potential friend
                        potentialUser.connection.talker.sendMessage("addfriend failed " + user.userName + " " + potentialFriend); // send the addfriend command to the potential friend

                        talker.sendMessage("addfriend failed");
                    }
                }
                else
                {
                System.out.println("Error: " + command);
                }
            } 
        }
        catch (IOException e) 
        {
            System.out.println("Error reading or writing to client (" + user.userName + "): " + e.getMessage());
            clientSocket = null;
                                                  
        }
    }
}

