import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

public class MyChatDialog extends JDialog
{
    Friends friend;
    JEditorPane editorPane;
    JFrame frame;

    public MyChatDialog(Friends friend, JEditorPane editorPane, JFrame frame) 
    {
        this.friend = friend;
        this.editorPane = editorPane;
        this.frame = frame;
    }

    public Friends getFriend() 
    {
        return friend;
    }

    public JEditorPane getEditorPane() 
    {
        return editorPane;
    }
}
