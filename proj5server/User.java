import java.io.*;

public class User 
{
    String userName;
    String password;
    ConnectionToClient connection;

    User(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }

    User(String userName, String password, ConnectionToClient connection)
    {
        this.userName = userName;
        this.password = password;
        this.connection = connection;
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

