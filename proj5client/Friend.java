class Friend 
{
    String name;
    boolean online;

    Friend(String name) 
    {
        this.name = name;
        this.online = false;
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
        if(online)
        {
            return name + " *";
        }
        else
        {
            return name;
        }
    }
}
