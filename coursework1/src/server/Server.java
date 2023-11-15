import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/*
 * A class for all server-side functions of the auction system (level 2)
 */
public class Server implements IRemoteAuction{
    // Hash table for all currently listed items for the forward auction
    // The key is the same as the ID field of AuctionItem
    private Hashtable<Integer, ForwardAuctionItem> _forwardAuctionItems;

    // Hash table for all items for the reverse auction
    // The key is the item ID, the value is a list of all the listings in the category

    private Hashtable<String, ReverseAuctionItem> _reverseAuctionItems;


    // A counter field for the number that will be used as the id for the next added listing
    private Hashtable<String, Account> accounts;
    private int FNextID;

    // Constructor
    public Server() {
        super();
        FNextID = 1;
        //DNextID = 1;
        _forwardAuctionItems = new Hashtable<Integer, ForwardAuctionItem>();
        _reverseAuctionItems = new Hashtable<String, ReverseAuctionItem>();
        accounts = new Hashtable<String, Account>();
        try {
            createAccount("Example Seller", "Example@seller.com", "examplePassword");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Account exampleSeller = accounts.get("Example@seller.com");
        try{
            // Create a few test items to test buyer client without having to use seller client input
            FCreateAuction("Cup", "A nice cup.", 0, 10, exampleSeller);
            FCreateAuction("Fork", "A decent fork.", 500.5555f, 1000f, exampleSeller);
            FCreateAuction("Plate", "An ornate plate.", 4.99f, 100f, exampleSeller);
            FCreateAuction("Car", "An old car.", 3, 10, exampleSeller);
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }

    }

    public boolean createAccount(String name, String email, String password) throws InvalidPasswordException, RemoteException
    {
        if(name == null || email == null || password == null) return false;
        Account a = new Account(name, email, password);
        Account existing = accounts.get(email);
        if(existing != null)
        {
            return false;
        }
        accounts.put(email, a);
        System.out.println("Server: Created account for " + email);
        return true;
    }

    public Account login(String email, String password) throws InvalidPasswordException, RemoteException
    {
        Account a = accounts.get(email);
        if(a == null) return null;
        if(!a.validatePassword(password)) return null;
        return a;
    }

    /*
     * Gets the specifics of an auction listing (Level 1)
     * @param itemId The ID of the listing
     * @param clientId the ID of the client requesting it
     * @return The listed item, null if no item for the provided ID is found
     */
    public ForwardAuctionItem getSpec (int itemId, int clientId) throws RemoteException
    {
        return _forwardAuctionItems.get(itemId);
    }

    /*
     * Creates a new auction listing with the provided parameters. Used by the seller client (level 2)
     * @param title The title of the listing
     * @param description A (short) description of the listed item
     * @param startingPrice The starting price for the bids on this item
     * @param reservePrice The reserve price of the item - if the listing is closed and the highest bid is below the reserve price, the item will not be sold
     * @return the ID of the new listing. -1 If the arguments were invalid.
     */
    public int FCreateAuction(String title, String description, float startingPrice, float reservePrice, Account seller) throws RemoteException
    {
        System.out.print("Server: attempting to create auction listing for " + title + "...");
        // Check if arguments are valid
        if(title == null || description == null || startingPrice < 0 || reservePrice < 0)
        {
            System.out.println("error, bad arguments");
            return -1;
        }
        // Create a new AuctionItem object (using the nextId counter for the ID) and put it in the hash table
        ForwardAuctionItem newItem = new ForwardAuctionItem(FNextID, title, description, startingPrice, reservePrice, seller);
        _forwardAuctionItems.put(FNextID, newItem);
        FNextID++;
        System.out.println("success");
        return newItem.getId();
    }

    /*
     * Closes an auction listing, declares a winner if the reserve price was reached. Intended to use by the seller client (Level 2)
     * @param auctionId the ID of the listing to close.
     * @param requestSource the account that's making the request to close.
     * @return A string (intended for output to console) of the results of the operation. 
     */
    public String FCloseAuction(int auctionId, Account requestSource) throws RemoteException
    {
        System.out.print("Server: attempting to close auction for ID: " + auctionId + "...");
        String result;
        // Check if the arguments are valid
        if(_forwardAuctionItems == null)
        {
            result = "Error: the listing database does not exist. Something went very wrong...";
            System.out.println(result);
            return result;
        }
        if(_forwardAuctionItems.get(auctionId) == null)
        {
            // If the item ID was not found, return that as a message
            result = "Error: Item does not exist";
            System.out.println(result);
            return result;
        }
        if(!requestSource.equals(_forwardAuctionItems.get(auctionId).getSellerAccount()))
        {
            result = "Error: You do not have permission to close this listing.";
            System.out.println(result);
            return result;
        }
        // If the arguments are valid, remove the listing regardless if the reserve price was met
        ForwardAuctionItem toClose = _forwardAuctionItems.get(auctionId);
        _forwardAuctionItems.remove(auctionId);
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
        result += "The winner is " + name + " (" + email + ") with an amount of " + ForwardAuctionItem.currencyToString(amount);
        System.out.println(result);
        return result;
    }

    /*
     * Gets the list of all active auction listings, intended to use by the buyer client (level 2) 
     * @return a list of strings; each member is a string containing the title, description and current price in a printable format
     */
    public List<String> FBrowseListings() throws RemoteException
    {
        List<String> result = new LinkedList<String>();
        // null and empty checks for the list
        if(_forwardAuctionItems == null)
        {
            result.add("Something has gone wrong");
            return result;
        }
        if(_forwardAuctionItems.size() == 0)
        {
            result.add("There are no items for sale");
            return result;
        }
        // For every item in the hash table, add a formatted string to the list of strings
        for(int i = 1; i < FNextID; i++)
        {
            ForwardAuctionItem item = _forwardAuctionItems.get(i);
            if(item != null)
            {
                String toAdd = "\n" +
                "ID: " + item.getId() + "\n" +
                "   Title: " + item.getTitle() + "\n" +
                "   Description: " + item.getDescription() + "\n" +
                "   Starting Price: " + ForwardAuctionItem.currencyToString(item.getStartingPrice()) + 
                "   Current Price: " + ForwardAuctionItem.currencyToString(item.getHighestBidAmount());
                result.add(toAdd);
            }
        }
        // Return the list of strings
        return result;
    }
    /*
     * Places a bid on one of the auction listings. Intended to be used by the buyer client (Level 2)
     * @param itemId The ID of the listing to be bid on
     * @param newPrice The bid price
     * @param name The name of the bidder - first piece of identifying information
     * @param email The email address of the bidder - second piece of identifying information
     * @return A string (intended for output to console) of the results of the operation. 
     */
    public String FPlaceBid(int itemId, float newPrice, Account bidder) throws RemoteException
    {
        // Get the item to be bid on from the hash table
        ForwardAuctionItem toBid = _forwardAuctionItems.get(itemId);
        String result;
        // If no item was found, output that
        if(toBid == null)
        {
            result = "Error: no item for ID: " + itemId + " found";
            return result;
        }
        // Prevent bids on own item
        if(bidder.equals(toBid.getSellerAccount()))
        {
            result = "Error: cannot bid on an item you are selling";
            return result;
        }
        boolean bidResult = toBid.newBid(newPrice, bidder);
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

    public List<String> RBrowseListings() throws RemoteException
    {
        List<String> result = new LinkedList<String>();
        if(_reverseAuctionItems == null)
        {
            result.add("Something has gone wrong");
            return result;
        }
        if(_reverseAuctionItems.size() == 0)
        {
            result.add("There are no items for sale");
            return result;
        }
        for(String s : _reverseAuctionItems.keySet())
        {
            ReverseAuctionItem item = _reverseAuctionItems.get(s);
            if(item != null)
            {
                String toAdd = "\n" +
                "Name: " + item.getTitle() + "\n" +
                "   Description: " + item.getDescription() + "\n";
                if(item.getLowestPrice() < 0)
                {
                    toAdd +=
                    "   There are no active listings for this item";
                }
                else
                {
                    toAdd += 
                    "   Current Lowest Price: " + ForwardAuctionItem.currencyToString(item.getLowestPrice());
                }
                result.add(toAdd);
            }
        }
        return result;
    }
    public String RCreateListing(String name, String description) throws RemoteException
    {
        String result = "";
        if(name == null || description == null)
        {
            result = "Invalid arguments";
            return result;
        }
        if(_reverseAuctionItems.containsKey(name))
        {
            result = "Listing already exists";
        }
        _reverseAuctionItems.put(name, new ReverseAuctionItem(name, description));
        result = "Created new listing for " + name;
        return result;
    }
    public String RAddEntryToListing(String name, float price, Account seller) throws RemoteException
    {
        String result = "";
        if(name == null || price < 0 || seller == null)
        {
            result = "Invalid arguments";
            return result;
        }
        ReverseAuctionItem item = _reverseAuctionItems.get(name);
        if(item == null)
        {
            result = "Listing does not exist";
            return result;
        }
        item.newBid(price, seller);
        result = "Successfully added new offer for " + name + " at " + AuctionItem.currencyToString(price);
        return result;
    }
    public static void main(String[] args) {
        try {
            InputProcessor.clearConsole();
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