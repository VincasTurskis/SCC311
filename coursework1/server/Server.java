import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class Server implements IRemoteAuction{
    private Hashtable<Integer, AuctionItem> auctionItems;
    private int nextId;
    public Server() {
        super();
        nextId = 1;
        auctionItems = new Hashtable<Integer, AuctionItem>();
        try{
            createAuction("Cup", "A nice cup.", 0, 10);
            createAuction("Fork", "A decent fork.", 5, 10);
            createAuction("Plate", "An ornate plate.", 4, 10);
            createAuction("Car", "An old car.", 3, 10);
            for(String s : browseActiveAuctions())
            {
                System.out.println(s);
            }
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }

    }

    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException
    {
        return auctionItems.get(itemId);
    }

    public int createAuction(String title, String description, int startingPrice, int reservePrice) throws RemoteException
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

    public String closeAuction(int auctionId) throws RemoteException
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
            System.out.println(result);
            return result;
        }
        if(toClose.getHighestBidName() == "No bid")
        {
            result = "There were no bidders for this item";
            System.out.println(result);
            return result;
        }
        String name, email;
        float amount;
        name = toClose.getHighestBidName();
        email = toClose.getHighestBidEmail();
        amount = toClose.getHighestBidAmount();
        result = "Auction for item ID:" + auctionId + "closed. The winner is " + name + " (" + email + ") with an amount of " + AuctionItem.currencyToString(amount);
        System.out.println("success");
        return result;
    }

    public List<String> browseActiveAuctions() throws RemoteException
    {
        List<String> result = new LinkedList<String>();
        if(auctionItems == null)
        {
            result.add("Something has gone wrong");
            return result;
        }
        if(auctionItems.size() == 0)
        {
            result.add("There are no items for sale");
            return result;
        }
        for(int i = 1; i < nextId; i++)
        {
            AuctionItem item = auctionItems.get(i);
            if(item != null)
            {
                String toAdd = "\n" +
                "ID: " + item.getId() + "\n" +
                "   Title: " + item.getTitle() + "\n" +
                "   Description: " + item.getDescription() + "\n" +
                "   Current Price: " + AuctionItem.currencyToString(item.getHighestBidAmount());
                result.add(toAdd);
            }
        }
        return result;
    }
    public boolean placeBid(int itemId, float newPrice, String name, String email) throws RemoteException
    {
        AuctionItem toBid = auctionItems.get(itemId);
        if(toBid == null) return false;
        return toBid.newBid(newPrice, name, email);
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