import java.io.*;
import java.net.Socket;

class Talker 
{
        Socket socket;
        private BufferedReader instream;
        private DataOutputStream outstream;
        User user;
      
        Talker(Socket socket) throws IOException 
        {
          this.socket = socket;
          this.instream = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // used to read from server
          this.outstream = new DataOutputStream(socket.getOutputStream());                     // used to send to server
        }

        Talker(Socket socket, User user) throws IOException 
        {
          this.socket = socket;
          this.user = user;
          this.instream = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // used to read from server
          this.outstream = new DataOutputStream(socket.getOutputStream());                     // used to send to server
        }
      
        void sendMessage( String message) throws IOException 
        {
          outstream.writeBytes(message +  " \n");         
         // System.out.println("Talker sent: " + message + " to: " + user.userName);                  
        }
      
        String receiveMessage() throws IOException 
        {
            String message;
            message = instream.readLine();                                    // read message from server
            if(user != null)
            {
            System.out.println("Talker received: " + message + " from: " + user.userName);
            }
            else
            {
                System.out.println("Talker received: " + message);
            }
            return message;
        }
      
}