import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

public class MyChatDialog extends JDialog
{
    Friend friend;
    JEditorPane editorPane;
    JFrame frame;

    public MyChatDialog(Friend friend, JEditorPane editorPane, JFrame frame) 
    {
        this.friend = friend;
        this.editorPane = editorPane;
        this.frame = frame;
    }

    public Friend getFriend() 
    {
        return friend;
    }

    public JEditorPane getEditorPane() 
    {
        return editorPane;
    }
}
