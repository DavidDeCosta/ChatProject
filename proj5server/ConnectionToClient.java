import java.io.*;
import java.net.Socket;
import java.util.Vector;

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
        if (user != null)
        {
            user.loggedIn = false;                                  // Set loggedIn to false
            user.connection = null;

            for (String buddy : user.buddylist) 
            {
                User buddyUser = userList.get(buddy); // gets his buddy's User class
                if (buddyUser != null && buddyUser.isLoggedIn()) // as long as that person exists and is online, send them the message
                {
                    buddyUser.connection.talker.sendMessage("friendLogout " + user.userName); // if person A logs out, user.userName is person A
                }
            }
            user = null;               // Reset the user and talker objects
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
                talker.sendMessage("login success");   //let the user know the login was successful
            
                for (String buddy : user.buddylist)   //display that users buddy list, also send the client its buddys names
                {
                    talker.sendMessage("addFriendSuccess " + buddy + " " + clientID);
            
                    User buddyUser = userList.get(buddy);   //get your friends User class
                    if (buddyUser != null) 
                    {
                        if (buddyUser.isLoggedIn())   //if the friend of you exists and are online use their talker to tell them your online
                        {
                            buddyUser.connection.talker.sendMessage("onlineStatus " + clientID + " " + buddy);
                            talker.sendMessage("onlineStatus " + buddy + " " + clientID); // Send onlineStatus to the user for each online friend
                        }
                    }
                }
                user.loggedIn = true;
                for (String pendingMessage : user.getPendingMessages()) 
                {
                    talker.sendMessage("pendingMessage " + pendingMessage);
                }
                user.clearPendingMessages();
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
        }
    }
    

    void handleAddFriend() throws IOException
    {
        potentialFriend = friendUserName; // Potential friend has the friend's username; if person A is trying to add person B
        if(userList.isUsernameInUse(potentialFriend)) // As long as person B is a registered User, try to add them
        {
            User potentialFriendUser = userList.get(potentialFriend); // get the user object of the potential friend
            potentialFriendUser.initiatorUserName = user.userName; 
            if (potentialFriendUser.isLoggedIn()) 
            {
                potentialFriendUser.connection.talker.sendMessage("addfriend " + user.userName + " " + potentialFriend); // Use person B's talker to ask B if they want to add A
            } 
            else 
            {
                String friendRequestMessage = "addfriend " + user.userName + " " + potentialFriend;
                if (potentialFriendUser.pendingMessages == null)     // if potential friend is offline add the message to their pending messages
                {
                    potentialFriendUser.pendingMessages = new Vector<String>();   // make new pending messages list
                }
            potentialFriendUser.pendingMessages.add(friendRequestMessage);            // add the message to list
            }
        }
        else
        {
            System.out.println("i shouldnt be here \n");
        }
    }

    void handleSendMessage(String receiverID, String messageText) throws IOException 
    {
        User receiverUser = userList.get(receiverID);
        if (receiverUser != null) 
        {
            if (receiverUser.isLoggedIn()) 
            {
                receiverUser.connection.talker.sendMessage("message " + user.userName + " " + messageText);
            } 
            else 
            {
                receiverUser.addPendingMessage("message " + user.userName + " " + messageText); // Add the message as a pending message for the offline user
            }
        } 
        else 
        {
            System.out.println("Failed to send message to " + receiverID);
        }
    }

    void handleRemoveFriend(String friendToRemove) throws IOException 
    {
        if (user.buddylist.contains(friendToRemove)) 
        {
            user.buddylist.remove(friendToRemove); // Remove friend from the user's buddy list
            User removedFriend = userList.get(friendToRemove); // Get the User object of the removed friend

            if (removedFriend != null) 
            {
                removedFriend.buddylist.remove(user.userName); // Remove the user from the removed friend's buddy list

                if (removedFriend.isLoggedIn()) 
                {
                
                    removedFriend.connection.talker.sendMessage("friendRemoved " + user.userName); //tell the removed friend they were removed
                }

                try 
                {
                    DataOutputStream save = new DataOutputStream(new FileOutputStream("userList.txt"));
                    userList.save(save);    //update the userlist file after removing the friend
                } 
                catch (IOException e) 
                {
                    System.out.println("Error saving the user list: " + e.getMessage());
                }
            }
        } 
        else 
        {
            System.out.println("Friend not found in the buddy list");
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
                else if (message.startsWith("message")) 
                {
                    information = message.split(" ", 4); // Limit the split to 4 parts
                    command = information[0];
                    String clientID = information[1];   //person A who send the message
                    String receiverID = information[2]; //person B who is recieving the sent message from A
                    String messageText = information[3]; 
                    handleSendMessage(receiverID, messageText);
                }
                else if (message.startsWith("removefriend")) 
                {
                    information = message.split(" ");
                    command = information[0];
                    String friendToRemove = information[1];
                    handleRemoveFriend(friendToRemove);
                }
                else 
                {
                    System.out.println("Error: " + command);
                }
            }
        } 
        catch (IOException e) 
        {
            if (user != null) 
            {
                System.out.println("Error reading or writing to client (" + user.userName + "): " + e.getMessage());
            } else 
            {
                System.out.println("Error reading or writing to client: " + e.getMessage());
            }
        }
    }
}


