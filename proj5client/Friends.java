class Friends 
{
    String name;
    boolean online;

    Friends(String name) 
    {
        this.name = name;
        this.online = false;
    }

    public String getName() 
    {
        return name;
    }

    public boolean isOnline() 
    {
        return online;
    }

    public void setOnline(boolean online) 
    {
        this.online = online;
    }

    @Override
    public String toString() 
    {
        return name;
    }
}
