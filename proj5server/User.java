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
        store.writeInt(buddylist.size());
        System.out.println("Storing buddy list for " + userName + ": " + buddylist);
        for (String buddyUserName : buddylist) {
            store.writeUTF(buddyUserName);
        }
    }

    void load(DataInputStream load) throws IOException {
        userName = load.readUTF();
        password = load.readUTF();
        int buddyListSize = load.readInt();
        System.out.println("Loading buddy list for " + userName);
        for (int i = 0; i < buddyListSize; i++) {
            String buddyUserName = load.readUTF();
            buddylist.add(buddyUserName);
        }
        System.out.println("Loaded buddy list for " + userName + ": " + buddylist);
    }

    void saveBuddyList(MyUserList userList) 
    {
        try 
        {
            DataOutputStream save = new DataOutputStream(new FileOutputStream("userList.txt"));
            userList.save(save);
        } catch (IOException e) 
        {
            System.out.println("Error saving the user list: " + e.getMessage());
        }
    }


    
    void loadBuddyList(MyUserList userList) {
        try (DataInputStream load = new DataInputStream(new FileInputStream("userList.txt"))) {
            userList.load(load);
        } catch (IOException e) {
            System.out.println("Error loading the user list: " + e.getMessage());
        }
    }

    
}

