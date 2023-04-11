import java.io.*;
import java.util.Vector;

public class User 
{
    String userName;
    String password;
    ConnectionToClient connection;
    Vector<String> buddylist;
    String initiatorUserName;
    boolean loggedIn;

    User(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
        buddylist = new Vector<String>();
    }

    User(String userName, String password, ConnectionToClient connection)
    {
        this.userName = userName;
        this.password = password;
        this.connection = connection;
        buddylist = new Vector<String>();
    }

    boolean isLoggedIn()
    {
        return loggedIn;
    }
    
    void store(DataOutputStream store) throws IOException 
    {
        store.writeUTF(userName);
        store.writeUTF(password);
        store.writeInt(buddylist.size());           // Write the buddy list size
        for (String buddyUserName : buddylist)      // Write each buddy's username
        {        
            System.out.println("IM IN HERE \n");
            store.writeUTF(buddyUserName);
        }
    }

    void load(DataInputStream load) throws IOException 
    {
        userName = load.readUTF();
        password = load.readUTF();
        int buddyListSize = load.readInt();             // Read the buddy list size
        for (int i = 0; i < buddyListSize; i++)             // Read each buddy's username
        { 
            String buddyUserName = load.readUTF();
            buddylist.add(buddyUserName);               // Add the buddy's username to the buddy list
        }
    }
}

