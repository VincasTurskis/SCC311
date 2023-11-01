import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/*
 * A class for all server-side functions of the auction system (Stage 1 level 1 & 2)
 */
public class Server implements IRemoteAuction{
    // Hash table for all currently listed items
    // The key is the same as the ID field of AuctionItem
    private Hashtable<Integer, AuctionItem> auctionItems;
    // A counter field for the number that will be used as the id for the next added listing
    private int nextId;

    // Constructor
    public Server() {
        super();
        nextId = 1;
        auctionItems = new Hashtable<Integer, AuctionItem>();
        try{
            // Create a few test items to test buyer client without having to use seller client input
            createAuction("Cup", "A nice cup.", 0, 10);
            createAuction("Fork", "A decent fork.", 500.5555f, 1000f);
            createAuction("Plate", "An ornate plate.", 4.99f, 100f);
            createAuction("Car", "An old car.", 3, 10);
            // Print created listings
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

    /*
     * Gets the specifics of an auction listing (Stage 1 Level 1)
     * @param itemId The ID of the listing
     * @param clientId the ID of the client requesting it
     * @return The listed item, null if no item for the provided ID is found
     */
    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException
    {
        return auctionItems.get(itemId);
    }

    /*
     * Creates a new auction listing with the provided parameters. Used by the seller client (Stage 1 level 2)
     * @param title The title of the listing
     * @param description A (short) description of the listed item
     * @param startingPrice The starting price for the bids on this item
     * @param reservePrice The reserve price of the item - if the listing is closed and the highest bid is below the reserve price, the item will not be sold
     * @return the ID of the new listing. -1 If the arguments were invalid.
     */
    public int createAuction(String title, String description, float startingPrice, float reservePrice) throws RemoteException
    {
        System.out.print("Server: attempting to create auction listing for " + title + "...");
        // Check if arguments are valid
        if(title == null || description == null || startingPrice < 0 || reservePrice <= startingPrice)
        {
            System.out.println("error, bad arguments");
            return -1;
        }
        // Create a new AuctionItem object (using the nextId counter for the ID) and put it in the hash table
        AuctionItem newItem = new AuctionItem(nextId, title, description, startingPrice, reservePrice);
        auctionItems.put(nextId, newItem);
        nextId++;
        System.out.println("success");
        return newItem.getId();
    }

    /*
     * Closes an auction listing, declares a winner if the reserve price was reached. Intended to use by the seller client (Stage 1 Level 2)
     * @param auctionId the ID of the listing to close.
     * @return A string (intended for output to console) of the results of the operation. 
     */
    public String closeAuction(int auctionId) throws RemoteException
    {
        System.out.print("Server: attempting to close auction for ID: " + auctionId + "...");
        String result;
        // Check if the arguments are valid
        if(auctionItems == null)
        {
            result = "Error: the listing database does not exist. Something went very wrong...";
            System.out.println(result);
            return result;
        }
        if(auctionItems.get(auctionId) == null)
        {
            // If the item ID was not found, return that as a message
            result = "Error: Item does not exist";
            System.out.println(result);
            return result;
        }
        // If the arguments are valid, remove the listing regardless if the reserve price was met
        AuctionItem toClose = auctionItems.get(auctionId);
        auctionItems.remove(auctionId);
        result = "Auction for item ID:" + auctionId + " closed. ";
        // Bloc containing different outcome possibilities
        if(toClose.getHighestBidName() == "No bid") // No bidders
        {
            result += "There were no bidders for this item";
            System.out.println(result);
            return result;
        }
        if(toClose.getHighestBidAmount() < toClose.getReservePrice()) // Highest price lower than reserve price
        {
            result += "Reserve price was not reached";
            System.out.println(result);
            return result;
        }
        //else, successful sale
        String name, email;
        float amount;
        name = toClose.getHighestBidName();
        email = toClose.getHighestBidEmail();
        amount = toClose.getHighestBidAmount();
        // Return the details of the winner, and the closing price
        result += "The winner is " + name + " (" + email + ") with an amount of " + AuctionItem.currencyToString(amount);
        System.out.println(result);
        return result;
    }

    /*
     * Gets the list of all active auction listings, intended to use by the buyer client (Stage 1 level 2) 
     * @return a list of strings; each member is a string containing the title, description and current price in a printable format
     */
    public List<String> browseActiveAuctions() throws RemoteException
    {
        List<String> result = new LinkedList<String>();
        // null and empty checks for the list
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
        // For every item in the hash table, add a formatted string to the list of strings
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
        // Return the list of strings
        return result;
    }
    /*
     * Places a bid on one of the auction listings. Intended to be used by the buyer client (Stage 1 Level 2)
     * @param itemId The ID of the listing to be bid on
     * @param newPrice The bid price
     * @param name The name of the bidder - first piece of identifying information
     * @param email The email address of the bidder - second piece of identifying information
     * @return A string (intended for output to console) of the results of the operation. 
     */
    public String placeBid(int itemId, float newPrice, String name, String email) throws RemoteException
    {
        // Get the item to be bid on from the hash table
        AuctionItem toBid = auctionItems.get(itemId);
        String result;
        // If no item was found, output that
        if(toBid == null)
        {
            result = "Error: no item for ID: " + itemId + " found";
            return result;
        }
        boolean bidResult = toBid.newBid(newPrice, name, email);
        // Prevent bids of a lower price than the current highest price
        if(!bidResult)
        {
            result = "Error: suggested price is lower than current price";
        }
        // Output the result
        else
        {
            result = "New bid placed on item ID: " + itemId + ". New price: " + newPrice;
        }
        return result;
    }
    public static void main(String[] args) {
        try {
            // Setup the server
            Server s = new Server();
            // Setup the different interfaces
            String name = "AuctionServer";
            // Get the RMI registry
            Registry registry = LocateRegistry.getRegistry();
            Remote stub = UnicastRemoteObject.exportObject(s, 0);
            // Advertise the different interfaces (basic, seller, client) on the registry
            registry.rebind(name, (IRemoteAuction) stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}