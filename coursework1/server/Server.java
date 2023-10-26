import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class Server implements IRemoteAuction{
    private Hashtable<Integer, AuctionItem> auctionItems;
    private int nextId;
    public Server() {
        super();
        nextId = 1;
        auctionItems = new Hashtable<Integer, AuctionItem>();
        createAuction("Cup", "A nice cup.", 0, 1);
        createAuction("Fork", "A decent fork.", 0, 1);
        createAuction("Plate", "An ornate plate.", 0, 1);
        createAuction("Car", "An old car.", 0, 1);

    }

    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException
    {
        return auctionItems.get(itemId);
    }

    public int createAuction(String title, String description, int startingPrice, int reservePrice)
    {
        System.out.print("Server: attempting to create auction listing for " + title + "...");
        if(title == null || description == null || startingPrice < 0 || reservePrice <= startingPrice)
        {
            System.out.println("error, bad arguments");
            return -1;
        }
        AuctionItem newItem = new AuctionItem(nextId, title, description, startingPrice, reservePrice);
        auctionItems.put(nextId, newItem);
        nextId++;
        System.out.println("success");
        return newItem.getId();
    }

    public String closeAuction(int auctionId)
    {
        System.out.print("Server: attempting to close auction for ID: " + auctionId + "...");
        String result;
        if(auctionItems == null || auctionItems.get(auctionId) == null)
        {
            result = "Item does not exist";
            System.out.println("error: " + result);
            return result;
        }
        AuctionItem toClose = auctionItems.get(auctionId);
        auctionItems.remove(auctionId);

        if(toClose.getHighestBidAmount() < toClose.getReservePrice())
        {
            result = "Reserve price was not reached";
            System.out.println("error: " + result);
            return result;
        }
        if(toClose.getHighestBidName() == "No bid")
        {
            result = "There were no bidders for this item";
            System.out.println("error: " + result);
            return result;
        }
        String name, email, amountString;
        float amount;
        name = toClose.getHighestBidName();
        email = toClose.getHighestBidEmail();
        amount = toClose.getHighestBidAmount();
        amountString = String.format("%.02f", amount);
        result = "Auction for item ID:" + auctionId + "closed. The winner is " + name + " (" + email + ") with an amount of " + amountString;
        System.out.println("success");
        return result;
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