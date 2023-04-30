
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;                                 // for Toolkit and Dimension
import java.awt.event.*;                            // for ActionListener
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.awt.event.WindowEvent;

class MainFrameGUI extends JFrame
                            implements ActionListener, MouseListener, WindowListener
{

    Toolkit toolkit;
    Dimension screenSize;
    JDialog dialog;
    JTextField textField;
    JTextField fieldForPortNumber;
    JTextField fieldForIP;
    JTextField fieldForUserName;
    JTextField fieldForPassword;
    JButton send;
    JButton exit;
    JButton connect;
    JLabel label;
    JPanel panel1;
    JPanel panel2;
    JPanel panel3;
    String userID;
    Socket socket;
    ConnectionToServer connectionToServer;
    Talker talker;

    JButton register;
    JButton logIn;
    JButton submit;
    JButton cancel;
    JButton addFriend;

    JComponent panel;
    GroupLayout groupLayout;
    JLabel labelForPortNumber;
    JLabel labelForIP;
    JLabel labelForUsername;
    JLabel labelForPassword;

    JPanel panel4;
    boolean isLoginOption = false;
    Properties properties;

    JList<Friend> displayList;                           // displays their names
    JScrollPane tripScrollPane;
    MyListModel justAListModel;

    Map<String, MyChatDialog> chatDialogs = new HashMap<>();    //name of the owner, then its chatdialog
    Hashtable<String, Vector<String>> pendingMessages;

    Friend friend;

    MainFrameGUI()
    {
        pendingMessages = new Hashtable<>();
        setupComponents();
        buildMainFrame();
    }

    void storePendingMessage(String sender, String messageText) 
    {
        if (!pendingMessages.containsKey(sender)) 
        {
            pendingMessages.put(sender, new Vector<String>());
        }
        pendingMessages.get(sender).addElement(messageText);
    }

    void buildMainFrame()
    {
        toolkit = Toolkit.getDefaultToolkit();                                    // used to help get the users screen size
        screenSize = toolkit.getScreenSize();                                     //get the users screen size
        setSize(400,500);           
        setLocationRelativeTo(null);                                           // window is placed in the center of screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                          //when close frame the program stops
        setTitle("Project 4");
        setVisible(true);

        addWindowListener(this);  // to send logout message to server when window is closed


    }

    void dialogSetup(boolean isLoginOption)
    {
        this.isLoginOption = isLoginOption;
        dialog = new JDialog();

        fieldForPortNumber = new JTextField(20);
        fieldForIP = new JTextField(20);
        fieldForUserName = new JTextField(20);
        fieldForPassword = new JTextField(20);

        labelForIP = new JLabel("Enter IP adress: ");
        labelForPortNumber = new JLabel("Enter Port Number: ");
        labelForUsername = new JLabel("Enter Username: ");
        labelForPassword = new JLabel("Enter Password: ");


        panel3 = new JPanel();
        groupLayout = new GroupLayout(panel3);
        panel3.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = groupLayout.createSequentialGroup();
        hGroup.addGroup(groupLayout.createParallelGroup().
        addComponent(labelForIP).addComponent(labelForPortNumber).addComponent(labelForUsername).addComponent(labelForPassword));
        hGroup.addGroup(groupLayout.createParallelGroup().
        addComponent(fieldForIP).addComponent(fieldForPortNumber).addComponent(fieldForUserName).addComponent(fieldForPassword));
        groupLayout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = groupLayout.createSequentialGroup();
        vGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).
        addComponent(labelForIP).addComponent(fieldForIP));
        vGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).
        addComponent(labelForPortNumber).addComponent(fieldForPortNumber));
        vGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).
        addComponent(labelForUsername).addComponent(fieldForUserName));
        vGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).
        addComponent(labelForPassword).addComponent(fieldForPassword));
        groupLayout.setVerticalGroup(vGroup);



        panel4 = new JPanel();
        cancel = new JButton("Cancel");

        if(isLoginOption == true)
        {
            dialog.setTitle("Login");
            properties = getProperties();                                                  //get the properties object
            if(!properties.isEmpty())
            {
                fieldForIP.setText(properties.getProperty("ip"));                       //set the text fields to the values in the properties object
                fieldForPortNumber.setText(properties.getProperty("portNumber"));
                fieldForUserName.setText(properties.getProperty("username"));
                fieldForPassword.setText(properties.getProperty("password"));
            }
            else
            {
                System.out.println("Properties file is empty");
            }
            connect = new JButton("Connect");  
            panel4.add(connect);
            connect.addActionListener(this);
            panel4.add(cancel);
            cancel.addActionListener(this);
        }
        else
        {
            dialog.setTitle("Register");
            submit = new JButton("Submit");
            panel4.add(submit);
            submit.addActionListener(this);
            panel4.add(cancel);
            cancel.addActionListener(this);
        }


        cancel.addActionListener(this);

        dialog.add(panel4, BorderLayout.SOUTH);
        dialog.add(panel3);

        dialog.setLocationRelativeTo(null);                                  // makes the dialog box position be in the center of screen
        dialog.setSize(400,400);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);         //makes the dialog box show up on top of the mainframe
        dialog.setVisible(true);
         

    }

    void setupComponents()
    {
        justAListModel = new MyListModel();
        displayList = new JList<Friend>(justAListModel);
        displayList.addMouseListener(this);
        tripScrollPane = new JScrollPane(displayList);
        add(tripScrollPane, BorderLayout.CENTER);

        register = new JButton("Register");
        register.addActionListener(this);

        addFriend = new JButton("Add Friend");
        addFriend.addActionListener(this);

        logIn = new JButton("Login");
        logIn.addActionListener(this);
        panel2 = new JPanel();
        add(panel2, BorderLayout.NORTH);

        panel2.add(register);
        panel2.add(logIn);
        panel2.add(addFriend);

    }

    void handleLogout()
    {
        if (talker != null) 
        {
            try 
            {
                talker.sendMessage("logout"); // Send a logout message to the server
                //    talker.close(); // Close the socket connection
            } 
            catch (IOException e) 
            {
                System.out.println("Failed to send logout message or close the connection");
            }
        }
    }

    void handleConnect()
    {
        String ip = fieldForIP.getText();
        int portNumber = Integer.parseInt(fieldForPortNumber.getText());
        userID = fieldForUserName.getText();
        String password = fieldForPassword.getText();

        String message = "login " + userID + " " + password;                   // create the message to send to the server
   
        try 
        {
            socket = new Socket(ip,portNumber);                               // pass in ip and port #
            talker = new Talker(socket);                                      // used to send and receive messages from the server
            connectionToServer = new ConnectionToServer(socket, message, userID,this);      // used to create a new thread for each client
        } 
        catch (IOException ex) 
        {
            System.out.println("Failed to connect to server");
            JOptionPane.showMessageDialog(null, "Failed to connect", "Could Not Connect", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        
        this.setTitle(userID);
        dialog.dispose();
    }

    void addFriendNameToList(String friendName) 
    {
        boolean friendExists = false;                                 // Check if the friend is already in the JList
        for (int i = 0; i < justAListModel.getSize(); i++) 
        {
            if (justAListModel.getElementAt(i).getName().equals(friendName)) 
            {
                friendExists = true;
                return;
            }
        }
        
        if (!friendExists)                                         // If the friend is not already in the JList, add them
        {
            Friend newFriend = new Friend(friendName);
            justAListModel.addElement(newFriend);
        }
    }

    void setStatus(Friend friendName)
    {
        friendName.setOnline(true);
    }

    void handleSend()
    {

        String message = textField.getText().trim();
        if(talker != null)
        {                                              // if the talker object is null then do nothing
            if(message.isEmpty())                       // if the message is empty then do nothing
            {
                return;
            }
            try 
            {
                talker.sendMessage(message);
                textField.setText("");                             //clears the text after sending

            } 
            catch (IOException ex) 
            {
                System.out.println("Failed to send message");
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "You are not connected to the server", "Not Connected", JOptionPane.ERROR_MESSAGE);
        }
    }

    void setupChatDialog(Friend friend, JFrame frame,Vector<String> pendingMessages)
    {
        this.friend = friend;

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        JScrollPane chatScrollPane = new JScrollPane(editorPane);

        initializeEditorPane(editorPane);   //puts some initial text in the dialog



        MyChatDialog chatDialog = new MyChatDialog(friend, editorPane, frame);
        chatDialog.setTitle(friend.name);
        chatDialog.setModal(false);
        chatDialog.setLayout(new BorderLayout());

        chatDialog.add(chatScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        chatDialog.add(messagePanel, BorderLayout.SOUTH);
    
        JTextArea messageArea = new JTextArea(4, 30);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        if (pendingMessages != null) 
        {
            for (String messageText : pendingMessages) 
            {
                addTextToChatPane(chatDialog, editorPane, messageText, false);
            }
        }

    
        JButton sendButton = new JButton("SendFromChat");
        sendButton.setText("Send");
        messagePanel.add(sendButton, BorderLayout.EAST);
        sendButton.addActionListener(e -> handleChatSend(friend, messageArea, editorPane,chatDialog));

        if (friend.hasPendingMessage) 
        {
            Vector<String> pendingMessagesForFriend = connectionToServer.getAndClearPendingMessagesForFriend(friend.getName());
            if (pendingMessagesForFriend != null) 
            {
                for (String message : pendingMessagesForFriend) 
                {
                    addTextToChatPane(chatDialog, editorPane, message, false);
                }
            }
            friend.setHasPendingMessage(false);
            justAListModel.updateFriend(friend);
        }

        chatDialog.setSize(450, 350);
        chatDialog.setLocationRelativeTo(null);
        chatDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        chatDialog.setVisible(true);


        chatDialog.addWindowListener(this);

        chatDialogs.put(friend.getName(), chatDialog);   //add the chatdialog to the hashmap

    
    }

    void handleChatSend(Friend friend, JTextArea messageArea, JEditorPane editorPane, MyChatDialog chatDialog) 
    {

        String messageText = messageArea.getText();    //get the text from message area
        if (!messageText.trim().isEmpty()) 
        {
            try 
            {
                                    // protocol msg + name of myself  + name of who im talking to  + the text of the msg
                String messageToSend = "message " + userID + " " + friend.getName() + " " + messageText ;
                talker.sendMessage(messageToSend);
            } catch (IOException e) 
            {
                e.printStackTrace();
            }
    
            addTextToChatPane(chatDialog, editorPane, messageText, true);
            messageArea.setText("");
        }
    }

    MyChatDialog findChatDialog(String userID) 
    {
        return chatDialogs.get(userID);   //return the chatdialog from the hashmap
    }

    void addTextToChatPane(JDialog chatDialog, JEditorPane editorPane, String txt, boolean isSender) 
    {
        HTMLDocument doc;
        Element html;
        Element body;
    
        doc = (HTMLDocument) editorPane.getDocument();
        html = doc.getRootElements()[0];
        body = html.getElement(1);
    
        String alignment = isSender ? "left" : "right";  //if the message is from me then align left else align right
        String color = isSender ? "blue" : "red";        //if the message is from me then color blue else color red
    
        String htmlText = "<div align=\"" + alignment + "\">" +
                "<font color=\"" + color + "\">" +
                txt +
                "</font></div>";
    
        try 
        {
            doc.insertBeforeEnd(body, htmlText);           //insert the text into the html document
            editorPane.setCaretPosition(editorPane.getDocument().getLength()); //set the caret position to the end of the document
        } 
        catch (Exception e) 
        {
            System.out.println("trouble \n");
        }
    }

    void initializeEditorPane(JEditorPane editorPane) 
    {
        String initialHtml = "<html><head></head><body></body></html>";
        editorPane.setContentType("text/html");
        editorPane.setText(initialHtml);
    }


    void handleSubmit()
    {
        properties = new Properties();

        String ip = fieldForIP.getText();                       //get values from text fields
        String portNumber = fieldForPortNumber.getText();
        String username = fieldForUserName.getText();
        username = username.toLowerCase();                  //convert username to lowercase to avoid case sensitivity
        String password = fieldForPassword.getText();
        password = password.toLowerCase();

        String message = "register " + username + " " + password;     //create a message to send to the server

        properties.setProperty("ip", ip);                    //set  values to the properties object
        properties.setProperty("portNumber", portNumber);
        properties.setProperty("username", username);
        properties.setProperty("password", password);

        try 
        {
            FileOutputStream outputStream = new FileOutputStream("information.txt");            //create a file output stream
            properties.store(outputStream, null);                                         //store the properties object in the file
            outputStream.close();
        } 
        catch (IOException e) 
        {
        e.printStackTrace();
        }

        int portnum;
        portnum = Integer.parseInt(portNumber);

        try 
        {
            socket = new Socket(ip,portnum);                                            // pass in ip and port #
            talker = new Talker(socket);                                                // used to send and receive messages from the server
            connectionToServer = new ConnectionToServer(socket, message, username,this);      // used to create a new thread for each client
        } 
        catch (IOException ex) 
        {
            System.out.println("Failed to connect to server");
            JOptionPane.showMessageDialog(null, "Can't Connect", "Not Connected", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }


        dialog.dispose();
    }

    void handleLogin()
    {
        dialogSetup(true);

    }

    void handleRegister()
    {
        dialogSetup(false);
    }

    void handleAddFriend()
    {
        String friendName = JOptionPane.showInputDialog(this, "Enter the name of the friend you want to add", "Add Friend", JOptionPane.QUESTION_MESSAGE);
        if(friendName != null)
        {
            String message = "addfriend " + friendName;
            try 
            {
                talker.sendMessage(message);
            } 
            catch (IOException ex) 
            {
                System.out.println("Failed to send message");
            }
        }
    }

    Properties getProperties()
    {
        Properties properties = new Properties();

        try(FileInputStream inputStream = new FileInputStream("information.txt"))    //try to read from the file
        {
            properties.load(inputStream);                                               //load the properties object from the file
        }
        catch (IOException e)
        {
            System.out.println("Failed to load properties");
        }

        return properties;
    }

    JPopupMenu createContextMenu() 
    {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem removeFriendMenuItem = new JMenuItem("Remove Friend");
        removeFriendMenuItem.addActionListener(this);
        removeFriendMenuItem.setActionCommand("removeFriend");
        contextMenu.add(removeFriendMenuItem);
        return contextMenu;
    }

    void handleRemoveFriend() 
    {
        int selectedIndex = displayList.getSelectedIndex();
        if (selectedIndex >= 0) 
        {
            Friend friend = justAListModel.getElementAt(selectedIndex);
            String friendName = friend.getName();
    
            // Send a message to the server to remove the friend from the buddy list
            String message = "removefriend " + friendName;
            try 
            {
                talker.sendMessage(message);
            } 
            catch (IOException ex) 
            {
                System.out.println("Failed to send remove friend message");
            }
    
            // Remove the friend from the JList
            justAListModel.removeElementAt(selectedIndex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if(e.getActionCommand().equals("Connect"))
        {
            handleConnect();
        }
        else if(e.getActionCommand().equals("Send"))
        {
            handleSend();
        }
        else if(e.getActionCommand().equals("Exit"))
        {
            System.exit(0);
        }
        else if(e.getActionCommand().equals("Register"))
        {
            handleRegister();
        }
        else if(e.getActionCommand().equals("Submit"))
        {
            handleSubmit();
        }
        else if(e.getActionCommand().equals("Cancel"))
        {
            dialog.dispose();
        }
        else if(e.getActionCommand().equals("Login"))
        {
            handleLogin();
        }
        else if(e.getActionCommand().equals("Add Friend"))
        {
            handleAddFriend();
        }
        else if (e.getActionCommand().equals("removeFriend")) 
        {
            handleRemoveFriend();
            System.out.println("i removed the friend \n");
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) 
    {
        if(e.getClickCount() == 2)
        {
            int index = displayList.locationToIndex(e.getPoint());          //get the index of the item that was clicked
            Friend friend = (Friend) justAListModel.getElementAt(index);     //get the friend object at that index

            // Check if a chat dialog is already open for the friend
            if (!chatDialogs.containsKey(friend.getName())) 
            {
            Vector<String> pendingMessagesForFriend = pendingMessages.get(friend.getName());
            setupChatDialog(friend, this, pendingMessagesForFriend);                                   //create a new chat dialog for the friend
                if (pendingMessagesForFriend != null) 
                {
                pendingMessagesForFriend.clear();
                }
            }
        }
        else if (SwingUtilities.isRightMouseButton(e)) 
        {
            int index = displayList.locationToIndex(e.getPoint());
            if (index >= 0) 
            {
                displayList.setSelectedIndex(index);
                JPopupMenu contextMenu = createContextMenu();
                contextMenu.show(displayList, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) 
    {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }

    @Override
    public void mouseReleased(MouseEvent e) 
    {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }

    @Override
    public void mouseEntered(MouseEvent e) 
    {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }

    @Override
    public void mouseExited(MouseEvent e) 
    {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
     //   throw new UnsupportedOperationException("Unimplemented method 'windowOpened'");
    }

    @Override
    public void windowClosing(WindowEvent e) 
    {
        if (e.getSource() instanceof MyChatDialog) 
        {
            MyChatDialog chatDialog = (MyChatDialog) e.getSource();
            Friend friend = chatDialog.getFriend();
            chatDialogs.remove(friend.getName());
        } 
        else if (e.getSource() == this) 
        {
            handleLogout();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub
      //  throw new UnsupportedOperationException("Unimplemented method 'windowClosed'");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub
      //  throw new UnsupportedOperationException("Unimplemented method 'windowIconified'");
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub
     //   throw new UnsupportedOperationException("Unimplemented method 'windowDeiconified'");
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub
      //  throw new UnsupportedOperationException("Unimplemented method 'windowActivated'");
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub
      //  throw new UnsupportedOperationException("Unimplemented method 'windowDeactivated'");
    }
    
}