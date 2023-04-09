import java.io.*;
import java.util.Hashtable;

public class User 
{
    String userName;
    String password;
    ConnectionToClient connection;
    Hashtable <String, User> buddylist;
    String initiatorUserName;

    User(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
        buddylist = new Hashtable<String, User>();
    }

    User(String userName, String password, ConnectionToClient connection)
    {
        this.userName = userName;
        this.password = password;
        this.connection = connection;
        buddylist = new Hashtable<String, User>();
    }
    
    void store(DataOutputStream store) throws IOException 
    {
        store.writeUTF(userName);
        store.writeUTF(password);
    }

    void load(DataInputStream load) throws IOException 
    {
        userName = load.readUTF();
        password = load.readUTF();
    }
}

