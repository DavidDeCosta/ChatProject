import java.io.*;
import java.util.Hashtable;

public class MyUserList  extends Hashtable<String, User>
{
    
    MyUserList() 
    {
        try 
        {
            DataInputStream load = new DataInputStream(new FileInputStream("userList.txt"));
            load(load);
        } 
        catch (IOException e) 
        {
            System.out.println("Error loading the user list: " + e.getMessage());
        }
    }

    void save(DataOutputStream store) throws IOException 
    {
        store.writeInt(size());
        System.out.println("Saving user list...");
        for (User user : values()) 
        {
            user.store(store);
        }
        System.out.println("User list saved.");
    }

    void load(DataInputStream load) throws IOException 
    {
        int size = load.readInt();
        System.out.println("Loading user list...");
        for (int i = 0; i < size; i++) 
        {
            User user = new User("", "");
            user.load(load);
            put(user.userName, user);
        }
        System.out.println("User list loaded: " + this);
    }

    boolean isUsernameInUse(String username) 
    {
        return containsKey(username);
    }

}


