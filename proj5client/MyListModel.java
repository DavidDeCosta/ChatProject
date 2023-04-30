import javax.swing.DefaultListModel;

public class MyListModel extends DefaultListModel<Friend>
{
    MyListModel()
    {
        super();
    }

    void updateIcon(Friend friend) 
    {
        int index = this.indexOf(friend);         //index of friend in list       
        if (index >= 0)                           //if friend is in the list otherwise it will be -1
        {
            fireContentsChanged(this, index, index);         //update the list
        }
    }

    Friend getFriend(String name) 
    {
        boolean found = false;
        int n;
        n = 0;
    
        while (!found && n < this.size()) 
        {
            if (this.elementAt(n).getName().equals(name))   //if the name of the friend is equal to the name of the friend in the list
            {
                found = true;
            } 
            else 
            {
                n++;                                     //keep searching
            }
        }
    
        if (found) 
        {
            return this.elementAt(n);
        } 
        else 
        {
            return null;
        }
    }
}
