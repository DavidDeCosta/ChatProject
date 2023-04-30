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
        for (User user : values())      // for each user in the hashtable
        {
            user.store(store);            // store the user
        }
    }

    void load(DataInputStream load) throws IOException 
    {
        int size = load.readInt();                              // read the number of users in the file
        for (int i = 0; i < size; i++) 
        {
            User user = new User("", "");       // create a new user
            user.load(load);                                    // load the user
            put(user.userName, user);                           // add the user to the hashtable
        }
    }

    boolean isUsernameInUse(String username) 
    {
        return containsKey(username);
    }

}


