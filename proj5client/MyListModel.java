import javax.lang.model.util.ElementScanner14;
import javax.swing.DefaultListModel;

public class MyListModel extends DefaultListModel<Friend>
{
    MyListModel()
    {
        super();
    }


    Friend getFriend(String name) 
    {
        boolean found = false;
        int n;
        n = 0;
    
        while (!found && n < this.size()) 
        {
            if (this.elementAt(n).getName().equals(name)) 
            {
                found = true;
            } 
            else 
            {
                n++;
            }
        }
    
        if (found) 
        {
            return this.elementAt(n);
        } else 
        {
            return null;
        }
    }
}
