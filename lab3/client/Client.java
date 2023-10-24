import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client{
     public static void main(String[] args) {
       if (args.length > 2 || args.length == 0) {
          System.out.println("Usage: java Client {id} {name}, java Client {id}");
          return;
       }
       if(args.length == 1)
       {
        int id = Integer.parseInt(args[0]);
        try {
              String name = "myserver";
              Registry registry = LocateRegistry.getRegistry("localhost");
              ICalc server = (ICalc) registry.lookup(name);
              Student s = server.RetrieveData(id);
              if(s == null)
              {
                System.out.println("Client: error, no value for key " + id + "in server");
              }
              else
              {
                System.out.println("Client: Name for id: " + s.GetID() + " is " + s.GetName());
              }
            }
            catch (Exception e) {
              System.err.println("Exception:");
              e.printStackTrace();
              }
        }
        else if(args.length == 2)
        {
          int id = Integer.parseInt(args[0]);
          String studentName = args[1];
          Student s = new Student(id, studentName);
          try {
              String name = "myserver";
              Registry registry = LocateRegistry.getRegistry("localhost");
              ICalc server = (ICalc) registry.lookup(name);
              server.StoreData(s);
              System.out.println("Client: stored " + s.print() + " in server");
            }
            catch (Exception e) {
              System.err.println("Exception:");
              e.printStackTrace();
              }
        }
      }
}