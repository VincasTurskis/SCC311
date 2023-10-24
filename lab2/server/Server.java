import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements ICalc{
    public Server() {
        super();
    }

    public int factorial(int n) throws RemoteException {
        if(n == 0 || n == 1) return 1;
        if(n < 0) return 0;
        int ans = 1;
        for(int i = 1; i <= n; i++)
        {
            ans *= i;
        }
        System.out.println("client request handled");
        return ans;
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