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
    String friendUserName;
    MyUserList userList;
    String potentialFriend;
  //  String initiatorUserName;

    ConnectionToClient(Socket clientSocket, MyUserList userList) throws IOException 
    {
        this.clientSocket = clientSocket;
        this.userList = userList;                                                           // store MyUserList object
        this.thread = new Thread(this);                                                    //  create a new thread for the client
        talker = new Talker(clientSocket);
        thread.start(); 
    }

    void handleLogout() throws IOException 
    {
        String userName = null;
        if(user !=null)
        {
            userName = user.userName;
            user.loggedIn = false;
            user.connection = null;
        }

        if (user != null && user.isLoggedIn()) 
        {
            user.loggedIn = false;
            user.connection = null;
            
            for (String buddy : user.buddylist)             // tell all his frriends that he logged out
            {
                User buddyUser = userList.get(buddy);                  //gets his  buddys User class
                if (buddyUser != null && buddyUser.isLoggedIn())       //as long as that person exists and is online, send them the message
                {
                    buddyUser.connection.talker.sendMessage("friendLogout " + user.userName);  //if person A logs out, user.userName is person A
                }
            }

            user = null;                                        // Reset the user and talker objects
          //  talker = new Talker(clientSocket);
         //   talker.sendMessage("logout success");
        } 
        else 
        {
          //  talker.sendMessage("logout failed");
        }
    }

    void handleLogin() throws IOException
    {
        user = userList.get(clientID);     //returns the user that logs in
        if (user != null && user.password.equals(clientPassword))  //as long as there was a user and the password matched
        {
            if (!user.isLoggedIn())  //also makes sure they arent already logged in
            {
                user.connection = this;                    //set the users connection to this instance             
                talker = new Talker(clientSocket, user);
                talker.sendMessage("login success");
                
                for (String buddy : user.buddylist)   //display that users buddy list, also send the client its buddys names
                {
                    System.out.println("buddy: " + buddy);
                    talker.sendMessage("addFriendSuccess " + buddy + " " + clientID);
                
                    User buddyUser = userList.get(buddy);
                    if (buddyUser != null && buddyUser.isLoggedIn()) 
                    {
                        buddyUser.connection.talker.sendMessage("addFriendSuccess " + clientID + " " + buddy);
                    }
                }
                user.loggedIn = true;
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

    void handleRegister() throws IOException
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

    void handleAddFriendResponse(String recieverID) throws IOException
    {
        potentialFriend = clientID;
        if(response.equals("0"))
        {
            user.buddylist.add(recieverID);  // add friend to buddylist with the key being the username and the value being the User object
            potentialUser = userList.get(recieverID); // get the User object of the potential friend
            potentialUser.buddylist.add(user.userName); // add the user to the potential friend's buddylist   so now both users have each other in their buddylist

            
            potentialUser.connection.talker.sendMessage("addFriendSuccess " + user.userName + " " + recieverID); // send  command to the initiator

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
        //   potentialUser.connection.talker.sendMessage("addfriend failed " + user.userName + " " + potentialFriend); // send the addfriend command to the potential friend

        //  talker.sendMessage("addfriend failed");
        }
}
    

    void handleAddFriend() throws IOException
{
    potentialFriend = friendUserName; // Potential friend has the friend's username; if person A is trying to add person B
    if(userList.isUsernameInUse(potentialFriend)) // As long as person B is a registered User, try to add them
    {
        User potentialFriendUser = userList.get(potentialFriend); // Get the User object of the potential friend
        potentialFriendUser.initiatorUserName = user.userName; // Set the initiatorUserName
        potentialFriendUser.connection.talker.sendMessage("addfriend " + user.userName + " " + potentialFriend); // Use person B's talker to ask B if they want to add A
    }
    else
    {
        System.out.println("i shouldnt be here \n");
    }
}

    @Override
    public void run() 
    {
        try 
        {
            while (true) 
            {
                String message;
                message = talker.receiveMessage(); // read message from client
                command = ""; // reset command
                if (message.startsWith("register") || message.startsWith("login")) 
                {
                    information = message.split(" ");
                    command = information[0];
                    clientID = information[1];
                    clientPassword = information[2];

                    if (command.equals("register")) 
                    {
                        handleRegister();
                    } 
                    else if (command.equals("login")) 
                    {
                        handleLogin();
                    }
                }
                else if (message.startsWith("logout")) 
                {
                    handleLogout();
                }
                else if (message.startsWith("addfriend")) 
                {
                    information = message.split(" ");
                    command = information[0];
                    friendUserName = information[1];                        // The friend's username that person A is trying to add so "B"
                    handleAddFriend();
                } 
                else if (message.startsWith("addFriendResponse")) //B's talker send back   B, yes 
                {
                    information = message.split(" ");
                    command = information[0];
                    clientID = information[1];
                    response = information[2];
                    handleAddFriendResponse(clientID);    //client iD will be (B)
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
        }
    }
}


