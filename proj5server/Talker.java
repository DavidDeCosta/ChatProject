import java.io.*;
import java.net.Socket;

class Talker 
{
        Socket socket;
        private BufferedReader instream;
        private DataOutputStream outstream;
      
        Talker(Socket socket) throws IOException 
        {
          this.socket = socket;
          this.instream = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // used to read from server
          this.outstream = new DataOutputStream(socket.getOutputStream());                     // used to send to server
        }
      
        void sendMessage( String message) throws IOException 
        {
          outstream.writeBytes(message +  " \n");                           
        }
      
        String receiveMessage() throws IOException 
        {
            String message;
            message = instream.readLine();                                    // read message from server
            System.out.println("Talker received: " + message);
            return message;
        }
      
}