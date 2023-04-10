import java.io.IOException;
import java.net.*;

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


    ConnectionToServer(Socket normalSocket, String message,String userID, MainFrameGUI mainFrameGUI) throws IOException
    {
        this.normalSocket = normalSocket;
        this.mainFrameGUI = mainFrameGUI;
        this.userID = userID;
        this.message = message;
        talker = new Talker(normalSocket);                                // create a talker for this client                        
        new Thread(this).start();                                        //client gets its own thread
    }

    @Override
    public void run() 
    {
        try {
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
                else if(recievedMessage.startsWith("addFriendSuccess"))
                {
                    String[] parts = recievedMessage.split(" ");
                    String friendName = parts[1];
                    SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    mainFrameGUI.addFriendNameToList(friendName);
                }
                });
                    command = "";
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
                                talker.sendMessage("addFriendResponse " + userID + " " + responseString);
                            } catch (IOException e) 
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    command = "";
                }
            }
        } catch (IOException e) {
            System.out.println("Connection to server lost");
        }
    }
}

 