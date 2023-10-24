import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class Server implements IRemoteAuction{
    private Hashtable<Integer, AuctionItem> auctionItem;
    public Server() {
        super();
        auctionItem = new Hashtable<Integer, AuctionItem>();
        auctionItem.put(1, new AuctionItem(1, "Cup", "A nice cup."));
        auctionItem.put(2, new AuctionItem(2, "Fork", "A decent fork."));
        auctionItem.put(3, new AuctionItem(3, "Plate", "An ornate plate."));
        auctionItem.put(4, new AuctionItem(4, "Car", "An old car."));

    }

    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException
    {
        return auctionItem.get(itemId);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "myserver";
            IRemoteAuction stub = (IRemoteAuction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}