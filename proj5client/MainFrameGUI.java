import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;                                 // for Toolkit and Dimension
import java.awt.event.*;                            // for ActionListener
import java.io.*;
import java.net.Socket;
import java.util.Properties;

class MainFrameGUI extends JFrame
                            implements ActionListener
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

    JComponent panel;
    GroupLayout groupLayout;
    JLabel labelForPortNumber;
    JLabel labelForIP;
    JLabel labelForUsername;
    JLabel labelForPassword;

    JPanel panel4;
    boolean isLoginOption = false;
    Properties properties;

    MainFrameGUI()
    {
        setupComponents();
        buildMainFrame();
    }

    void buildMainFrame()
    {
        toolkit = Toolkit.getDefaultToolkit();                                    // used to help get the users screen size
        screenSize = toolkit.getScreenSize();                                     //get the users screen size
        setSize((screenSize.width/2 + 70), (screenSize.height/2 + 70));           
        setLocationRelativeTo(null);                                           // window is placed in the center of screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);                          //when close frame the program stops
        setTitle("Project 4");
        setVisible(true);
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
        panel1 = new JPanel();
        add(panel1, BorderLayout.NORTH);

        textField = new JTextField(40);
        panel1.add(textField);
        send = new JButton("Send");
        send.addActionListener(this);
        exit = new JButton("Exit");
        exit.addActionListener(this);
        panel1.add(send);
        panel1.add(exit);

        register = new JButton("Register");
        register.addActionListener(this);

        logIn = new JButton("Login");
        logIn.addActionListener(this);
        panel2 = new JPanel();
        add(panel2, BorderLayout.CENTER);

        panel2.add(register);
        panel2.add(logIn);

    }

    void handleConnect()
    {
        String ip = fieldForIP.getText();
        int portNumber = Integer.parseInt(fieldForPortNumber.getText());
        String userID = fieldForUserName.getText();
        String password = fieldForPassword.getText();

        String message = "login " + userID + " " + password;                   // create the message to send to the server
   
        try 
        {
            socket = new Socket(ip,portNumber);                               // pass in ip and port #
            talker = new Talker(socket);                                      // used to send and receive messages from the server
            connectionToServer = new ConnectionToServer(socket, message);      // used to create a new thread for each client
        } 
        catch (IOException ex) 
        {
            System.out.println("Failed to connect to server");
            JOptionPane.showMessageDialog(null, "Failed to connect", "Could Not Connect", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        dialog.dispose();
    }

    void handleSend()
    {
        String message = textField.getText().trim();
            if(message.isEmpty())                // if the message is empty then do nothing
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

    void handleSubmit()
    {
        properties = new Properties();

        String ip = fieldForIP.getText();                       //get values from text fields
        String portNumber = fieldForPortNumber.getText();
        String username = fieldForUserName.getText();
        String password = fieldForPassword.getText();

        String message = "register " + username + " " + password;     //create a message to send to the server

        properties.setProperty("ip", ip);                    //set  values to the properties object
        properties.setProperty("portNumber", portNumber);
        properties.setProperty("username", username);
        properties.setProperty("password", password);

        try 
        {
            FileOutputStream outputStream = new FileOutputStream("information.properties");   //create a file output stream
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
            connectionToServer = new ConnectionToServer(socket, message);      // used to create a new thread for each client
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

    Properties getProperties()
    {
        Properties properties = new Properties();

        try(FileInputStream inputStream = new FileInputStream("information.properties"))    //try to read from the file
        {
            properties.load(inputStream);                                               //load the properties object from the file
        }
        catch (IOException e)
        {
            System.out.println("Failed to load properties");
        }

        return properties;
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
        
    }
    
}