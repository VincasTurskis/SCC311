import java.io.Serializable;

public class Student implements Serializable{
    private int _id;
    private String _name;

    public Student(int id, String name)
    {
        _id = id;
        _name = name;
    }
    public int GetID()
    {
        return _id;
    }
    public String GetName()
    {
        return _name;
    }
    public String print()
    {
        return "{" + _id + ", " + _name + "}";
    }
}
