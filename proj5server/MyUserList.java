import java.io.*;
import java.util.Hashtable;

public class MyUserList  extends Hashtable<String, User>
{
    
    MyUserList() 
    {
        
    }

    void save(DataOutputStream store) throws IOException 
    {
        store.writeInt(size());            // write the size of the list
        for (User user : values())        // for each user in the list
        {
            user.store(store);                // write each user
        }
    }

    void load(DataInputStream load) throws IOException 
    { 
        clear();                                               // clear the list
        int size = load.readInt();                             // read the size of the list
        for (int i = 0; i < size; i++) 
        {
            User user = new User("", "");           // create a new user
            user.load(load);                                         // read the user
            put(user.userName, user);                               // add the user to the list
        }
    }

    boolean isUsernameInUse(String username) 
    {
        return containsKey(username);
    }


    @Override
    public String toString() 
    {
        return "My list = {" + values() + '}';
    }



}


