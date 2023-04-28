class Friend 
{
    String name;
    boolean online;
    boolean hasPendingMessage;

    Friend(String name) 
    {
        this.name = name;
        this.online = false;
        this.hasPendingMessage = false;

    }

    void setHasPendingMessage(boolean hasPendingMessage) 
    {
        this.hasPendingMessage = hasPendingMessage;
    }

    String getName() 
    {
        return name;
    }

    boolean isOnline() 
    {
        return online;
    }

    void setOnline(boolean online) 
    {
        this.online = online;
    }

    @Override
    public String toString() 
    {
        if(hasPendingMessage && online)
        {
            return name + " * (pending message)";
        }
        else if(online)
        {
            return name + " *";
        }
        else if(hasPendingMessage)
        {
            return name + " (pending message)";
        }
        else
        {
            return name;
        }
    }
}
