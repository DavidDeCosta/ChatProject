import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

class ConnectionToServer implements Runnable  // this class is used to create a new thread for each client
{
    String message;
    Socket normalSocket;        // socket used to communicate with client
    //boolean recieved = false;   
    Talker talker;
    String recievedMessage;
    String [] information;
    String command;
    String clientID;
    String receiverID;
    int response;
    String responseString;
    String userID;
    String senderID;
    MainFrameGUI mainFrameGUI;

    HashMap<String, Vector<String>> pendingMessages = new HashMap<String, Vector<String>>();

  //  Vector<Friend> friends = new Vector<>();


    ConnectionToServer(Socket normalSocket, String message,String userID, MainFrameGUI mainFrameGUI) throws IOException
    {
        this.normalSocket = normalSocket;
        this.mainFrameGUI = mainFrameGUI;
        this.userID = userID;
        this.message = message;
        talker = new Talker(normalSocket);                                // create a talker for this client                        
        new Thread(this).start();                                        //client gets its own thread
    }


    
    Vector<String> getAndClearPendingMessagesForFriend(String friendName) 
    {
        Vector<String> pendingMessagesForFriend = pendingMessages.get(friendName);
        if (pendingMessagesForFriend != null) 
        {
            pendingMessages.put(friendName, new Vector<String>()); // clear the pending messages for this friend by replacing it with a new empty vector
            return pendingMessagesForFriend;
        }
        return null;
    }

    void handlePendingMessages() 
    {
        String[] messageParts = recievedMessage.split(" ", 2);
        String messageType = messageParts[0];
        String messageContent = messageParts[1];
    
        if (messageType.equals("pendingMessage")) 
        {
            String[] pendingMessagesArray = messageContent.split(Pattern.quote("||"));   // split the pending messages with || because the message can contain spaces
            for (String pendingMessage : pendingMessagesArray) 
            {
                if (pendingMessage.startsWith("addfriend"))                         
                {
                    String[] friendRequestParts = pendingMessage.split(" ");           // split the pending message with space
                    String senderID = friendRequestParts[1];
                    String receiverID = friendRequestParts[2];
    
                    SwingUtilities.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            int response = JOptionPane.showConfirmDialog(null, "Do you want to add " + senderID + " as a friend?", "Add Friend", JOptionPane.YES_NO_OPTION);
                            String responseString = Integer.toString(response);
                            if (response == 0) 
                            {
                                mainFrameGUI.addFriendNameToList(senderID);
                            }
                            try 
                            {
                                talker.sendMessage("addFriendResponse " + senderID + " " + responseString);
                            } 
                            catch (IOException e) 
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                } 
                else if (pendingMessage.startsWith("message")) 
                {
                    String[] messageDetails = pendingMessage.split(" ", 3);    // split the pending message with space
                    String sender = messageDetails[1];
                    String messageText = messageDetails[2];
                    MyChatDialog chatDialog = mainFrameGUI.findChatDialog(sender);        // find the chat dialog for this friend
                    if (chatDialog == null)    
                    {
                        Friend friend = mainFrameGUI.justAListModel.getFriend(sender);             // get the friend object from the list
                        if (friend != null) 
                        {
                            friend.setHasPendingMessage(true);
                            mainFrameGUI.justAListModel.updateFriend(friend);                       // update the friend in the list
        
                            Vector<String> pendingMessagesForFriend = pendingMessages.get(sender);  // get the pending messages for this friend
                            if (pendingMessagesForFriend == null)                                   // if there are no pending messages for this friend
                            {
                                pendingMessagesForFriend = new Vector<String>();                    // create a new vector to hold the pending messages
                                pendingMessages.put(sender, pendingMessagesForFriend);              // add the vector to the hashmap
                            }
                            pendingMessagesForFriend.add(messageText);                              // add the message to the vector
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() 
    {
        try 
        {
            talker.sendMessage(message);

            while (true) 
            {
                command = ""; 
                recievedMessage = talker.receiveMessage();
                if (recievedMessage.startsWith("addfriend")) 
                {
                    information = recievedMessage.split(" ");
                    command = information[0];
                    senderID = information[1];
                    receiverID = information[2];
                }
                else if (recievedMessage.startsWith("pendingMessage")) 
                {
                    handlePendingMessages();
                }
                else if(recievedMessage.startsWith("addFriendSuccess"))
                {
                    String[] parts = recievedMessage.split(" ");
                    String friendName = parts[1];

                    // Create a Friend object and add it to the justAListModel
                    Friend newFriend = new Friend(friendName);
                    mainFrameGUI.justAListModel.addElement(newFriend);

                    command = "";
                }
                else if(recievedMessage.startsWith("onlineStatus"))
                {
                    information = recievedMessage.split(" ");
                    command = information[0];
                    senderID = information[1];
                    Friend f;
                    f = mainFrameGUI.justAListModel.getFriend(senderID);  
    
                    if (f != null) 
                    {
                        f.setOnline(true);
                    } else 
                    {
                        System.out.println("Friend not found: " + senderID);
                    }

                }
                else if (recievedMessage.startsWith("friendLogout")) 
                {
                    information = recievedMessage.split(" ");
                    command = information[0];
                    senderID = information[1];
                    Friend f;
                
                    f = mainFrameGUI.justAListModel.getFriend(senderID);
                
                    if (f != null) 
                    {
                        f.setOnline(false);
                    } 
                    else 
                    {
                        System.out.println("Friend not found: " + senderID);
                    }
                }
                else if (recievedMessage.startsWith("login success")) 
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            mainFrameGUI.logIn.setEnabled(false);
                            mainFrameGUI.addFriend.setVisible(true);
                            mainFrameGUI.logOut.setVisible(true);
                        }
                    });
                }
                else if(recievedMessage.startsWith("message"))
                {
                    String[] messageParts = recievedMessage.split(" ", 3);
                    String sender = messageParts[1];
                    String messageText = messageParts[2];
                    System.out.println(messageParts[2]);
                    SwingUtilities.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run() {
                            MyChatDialog chatDialog = mainFrameGUI.findChatDialog(sender);
                            if(chatDialog != null && chatDialog.isVisible())           //if they have the chat open
                            {
                                JEditorPane editorPane = chatDialog.getEditorPane();          // get the editor pane from the chat dialog
                                mainFrameGUI.addTextToChatPane(chatDialog, editorPane, messageText, false);
                            }
                            else
                            {
                                Friend friend = mainFrameGUI.justAListModel.getFriend(sender);   // get the friend object from the list
                                if(friend != null)
                                {
                                    friend.setHasPendingMessage(true);          // set the hasPendingMessage to true
                                    mainFrameGUI.justAListModel.updateFriend(friend);             // show the message icon 
                                    Vector<String> pendingMessagesForFriend = pendingMessages.get(sender);   // get the pending messages for this friend
                                    if(pendingMessagesForFriend == null)
                                    {
                                        pendingMessagesForFriend = new Vector<String>();                        // create a new vector to hold the pending messages later
                                        pendingMessages.put(sender, pendingMessagesForFriend);                  // add the vector to the hashmap
                                    }
                                    pendingMessagesForFriend.add(messageText);                                     
                                }
                            }

                        }
                    });
                }
                if (command.equals("addfriend") && userID.equals(receiverID)&& !userID.equals(senderID)) 
                {
                    SwingUtilities.invokeLater(new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            response = JOptionPane.showConfirmDialog(null, "Do you want to add " + senderID + " as a friend?", "Add Friend", JOptionPane.YES_NO_OPTION);
                            responseString = Integer.toString(response);
                            if(response == 0)
                            {
                                mainFrameGUI.addFriendNameToList(senderID);
                            }
                            try 
                            {
                                System.out.println("SenderID: " + senderID);
                                talker.sendMessage("addFriendResponse " + senderID + " " + responseString);
                            } catch (IOException e) 
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    command = "";
                }
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Connection to server lost");
        }
    }
}

 