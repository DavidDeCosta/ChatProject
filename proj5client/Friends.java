class Friends 
{
    String name;
    boolean online;

    Friends(String name) 
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
        return name;
    }
}
