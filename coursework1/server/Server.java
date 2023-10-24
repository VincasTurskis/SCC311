import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class Server implements ICalc{
    private Hashtable<Integer, String> students = new Hashtable<Integer, String>();
    public Server() {
        super();
    }

    public void StoreData(Student toStore) throws RemoteException
    {
        if(toStore == null)
        {
            System.out.println("Server: error, the object that has been supplied is null");
            return;
        }
        students.put(toStore.GetID(), toStore.GetName());
        System.out.println("Server: Sucessfully put {" + toStore.GetID() + ", " + toStore.GetName() + "} into hashtable");
    }

    public Student RetrieveData(int id) throws RemoteException
    {
        String name = students.get(id);
        if(name == null)
        {
            System.out.println("Server: error, hash table entry not found for id: " + id);
            return null;
        }
        System.out.println("Server: successfully retrieved object for id: " + id);
        return new Student(id, name);
    }
    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "myserver";
            ICalc stub = (ICalc) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}