import java.io.*;
import java.util.Vector;

public class User 
{
    String userName;
    String password;
    ConnectionToClient connection;
    Vector<String> buddylist;
    boolean loggedIn;
    String initiatorUserName;
    Vector<String> pendingMessages;

    User(String userName, String password) 
    {
        this.userName = userName;
        this.password = password;
        buddylist = new Vector<String>();
        pendingMessages = new Vector<String>();
    }
    
    User(String userName, String password, ConnectionToClient connection) 
    {
        this.userName = userName;
        this.password = password;
        this.connection = connection;
        buddylist = new Vector<String>();
        pendingMessages = new Vector<String>();
    }

    void addPendingMessage(String message) 
    {
        pendingMessages.add(message);
    }
    
    Vector<String> getPendingMessages() 
    {
        return pendingMessages;
    }
    
    void clearPendingMessages() 
    {
        pendingMessages.clear();
    }

    boolean isLoggedIn()
    {
        return loggedIn;
    }
    
    void store(DataOutputStream store) throws IOException 
    {
        store.writeUTF(userName);
        store.writeUTF(password);
        store.writeInt(buddylist.size());      
        for (String buddyUserName : buddylist) // for each buddy in the buddylist
        {
            store.writeUTF(buddyUserName);     // store the buddy's username
        }
    }

    void load(DataInputStream load) throws IOException 
    {
        userName = load.readUTF();
        password = load.readUTF();
        int buddyListSize = load.readInt();           // read the number of buddies in the file
        for (int i = 0; i < buddyListSize; i++) 
        {
            String buddyUserName = load.readUTF();    // read the buddy's username
            buddylist.add(buddyUserName);             // add the buddy to the buddylist
        }
    }
    
}

