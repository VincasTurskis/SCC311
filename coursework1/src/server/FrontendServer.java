import java.io.FileInputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

/*
 * A class for all server-side functions of the auction system (level 2)
 */
public class FrontendServer implements IRemoteAuction{
    // Hash table for all currently listed items for the forward auction
    // The key is the same as the ID field of AuctionItem
    private Hashtable<Integer, ForwardAuctionItem> _forwardAuctionItems;

    // Hash table for all items for the reverse auction
    // The key is the item name, the value is a list of all the listings in the category

    private Hashtable<String, ReverseAuctionItem> _reverseAuctionItems;

    // Hash table for all items for the double auction
    // The key is the item name, the value is a list of all buy and sell orders for the item.

    private Hashtable<String, DoubleAuctionItem> _doubleAuctionItems;


    // A counter field for the number that will be used as the id for the next added listing
    private Hashtable<String, Account> _accounts;
    private Hashtable<Account, LinkedList<String>> _messages;
    private int FNextID;

    private PrivateKey _privateKey;

    private JChannel _channel;
    private RpcDispatcher _dispacher;

    // Constructor
    public FrontendServer() {
        super();

        try {
            _channel = new JChannel();
            _channel.connect("AuctionCluster");
            _channel.setDiscardOwnMessages(true);
            _dispacher = new RpcDispatcher(_channel, this);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        _forwardAuctionItems = new Hashtable<Integer, ForwardAuctionItem>();
        _reverseAuctionItems = new Hashtable<String, ReverseAuctionItem>();
        _doubleAuctionItems = new Hashtable<String, DoubleAuctionItem>();
        _accounts = new Hashtable<String, Account>();
        _messages = new Hashtable<Account, LinkedList<String>>();
        System.out.println(_channel.getViewAsString());
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("sender_keystore.jks"), "auctionPassword".toCharArray());
            _privateKey = (PrivateKey) keyStore.getKey("senderKeyPair", "auctionPassword".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            
            addAccount(new Account("Example", "a@b.com", "password"));
            Account exampleSeller = login("a@b.com", "password").getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            // Create a few test items to test buyer client without having to use seller client input
            //FCreateAuction("Cup", "A nice cup.", 1000, 1000, exampleSeller);
            //RCreateListing("Plate", "An antique plate");
            //RAddEntryToListing("Plate", 1000, exampleSeller);
            //DCreateListing("Fork", "A fancy fork");
            //DPlaceSellOrder("Fork", 1000, exampleSeller);
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }

    }

    public SignedMessage<Boolean> addAccount(Account a) throws InvalidPasswordException, RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Boolean>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "addAccount", new Object[]{a}, new Class[]{a.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Boolean> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Account>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "login", new Object[]{email, password}, new Class[]{email.getClass(), password.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Account> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Using an account received from client as a key in a hashtable doesn't work
    // This method takes the email from the argument account and fetches the correct account from the hash table
    private Account accountTranslation(Account remote)
    {
        if(remote == null) return null;
        synchronized(_accounts)
        {
            return _accounts.get(remote.getEmail());
        }
    }

    private boolean sendMessage(Account receiver, String message)
    {
        Account tReceiver = accountTranslation(receiver);
        if(tReceiver == null || message == null) return false;
        synchronized(_messages)
        {
            if(_messages == null) return false;
            List<String> msgList = _messages.get(tReceiver);
            if(msgList == null) return false;
            msgList.add(message);
        }
        return true;
    }

    public SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException
    {
        Account tAccount = accountTranslation(account);
        if(tAccount == null) return null;
        synchronized(_messages)
        {
            return new SignedMessage<LinkedList<String>>(_messages.get(tAccount), _privateKey);
        }
    }

    public SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException
    {
        Account tAccount = accountTranslation(account);
        if(tAccount == null) return new SignedMessage<Boolean>(false, _privateKey);
        synchronized(_messages)
        {
            if(_messages.get(tAccount) == null) return new SignedMessage<Boolean>(false, _privateKey);
            _messages.put(tAccount, new LinkedList<String>());
        }
        System.out.println("Deleted messages of " + tAccount.getName());
        return new SignedMessage<Boolean>(true, _privateKey);
    }
    /*
     * Creates a new auction listing with the provided parameters. Used by the seller client (level 2)
     * @param title The title of the listing
     * @param description A (short) description of the listed item
     * @param startingPrice The starting price for the bids on this item
     * @param reservePrice The reserve price of the item - if the listing is closed and the highest bid is below the reserve price, the item will not be sold
     * @return the ID of the new listing. -1 If the arguments were invalid.
     */
    public SignedMessage<Integer> FCreateAuction(String title, String description, int startingPrice, int reservePrice, Account seller) throws RemoteException
    {
        System.out.print("Server: attempting to create auction listing for " + title + "...");
        // Check if arguments are valid
        if(title == null || description == null || startingPrice < 0 || reservePrice < 0)
        {
            System.out.println("error, bad arguments");
            return new SignedMessage<Integer>(-1, _privateKey);
        }
        // Create a new AuctionItem object (using the nextId counter for the ID) and put it in the hash table
        ForwardAuctionItem newItem = new ForwardAuctionItem(FNextID, title, description, startingPrice, reservePrice, seller);
        synchronized(_forwardAuctionItems)
        {
            _forwardAuctionItems.put(FNextID, newItem);
        }
        FNextID++;
        System.out.println("success");
        return new SignedMessage<Integer>(newItem.getId(), _privateKey);
    }

    /*
     * Closes an auction listing, declares a winner if the reserve price was reached. Intended to use by the seller client (Level 2)
     * @param auctionId the ID of the listing to close.
     * @param requestSource the account that's making the request to close.
     * @return A string (intended for output to console) of the results of the operation. 
     */
    public SignedMessage<String> FCloseAuction(int auctionId, Account requestSource) throws RemoteException
    {
        System.out.print("Server: attempting to close auction for ID: " + auctionId + "...");
        String result;
        if(requestSource == null)
        {
            result = "Error: Invalid arguments";
            System.out.println(result);
            return new SignedMessage<String>(result, _privateKey);
        }
        synchronized(_forwardAuctionItems)
        {
            // Check if the arguments are valid
            if(_forwardAuctionItems == null)
            {
                result = "Error: the listing database does not exist. Something went very wrong...";
                System.out.println(result);
                return new SignedMessage<String>(result, _privateKey);
            }
            ForwardAuctionItem toClose;
            toClose = _forwardAuctionItems.get(auctionId);
            if(toClose == null)
            {
                // If the item ID was not found, return that as a message
                result = "Error: Item does not exist";
            }
            else if(!requestSource.equals(toClose.getSellerAccount()))
            {
                result = "Error: You do not have permission to close this listing.";
            }
            else
            {
                // If the arguments are valid, remove the listing regardless if the reserve price was met
                _forwardAuctionItems.remove(auctionId);
                result = "Auction for " + toClose.getTitle() + " (ID:" + auctionId + ") closed. ";
                // Bloc containing different outcome possibilities
                if(!toClose.hasABid()) // No bidders
                {
                    result += "There were no bidders for this item";
                }
                else if(toClose.getHighestBidAmount() < toClose.getReservePrice()) // Highest price lower than reserve price
                {
                    result += "Reserve price was not reached";
                }
                else {//else, successful sale
                    String name, email;
                    int amount;
                    name = toClose.getHighestBidder().getName();
                    email = toClose.getHighestBidder().getEmail();
                    amount = toClose.getHighestBidAmount();
                    // Return the details of the winner, and the closing price
                    result += "The winner is " + name + " (Email: " + email + ") with an amount of " + AuctionItem.currencyToString(amount);
                    sendMessage(toClose.getSellerAccount(), "Forward Auction: " + result);
                    sendMessage(toClose.getHighestBidder(), "Forward Auction: You have won the auction for " + toClose.getTitle() + " (ID: " + toClose.getId() + ") with a bid of " + AuctionItem.currencyToString(amount));
                }
            }
        }
        System.out.println(result);
        return new SignedMessage<String>(result, _privateKey);
    }

    /*
     * Gets the list of all active auction listings, intended to use by the buyer client (level 2) 
     * @return a list of strings; each member is a string containing the title, description and current price in a printable format
     */
    public SignedMessage<LinkedList<String>> FBrowseListings() throws RemoteException
    {
        LinkedList<String> result = new LinkedList<String>();
        // null and empty checks for the list
        synchronized(_forwardAuctionItems)
        {
            if(_forwardAuctionItems == null)
            {
                result.add("Something has gone wrong");
            }
            else if(_forwardAuctionItems.size() == 0)
            {
                result.add("There are no items for sale");
            }
            else
            {
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
                        "   Starting Price: " + AuctionItem.currencyToString(item.getStartingPrice()) + 
                        "   Current Price: " + AuctionItem.currencyToString(item.getHighestBidAmount());
                        result.add(toAdd);
                    }
                }
            }
        }
        // Return the list of strings
        return new SignedMessage<LinkedList<String>>(result, _privateKey);
    }
    /*
     * Places a bid on one of the auction listings. Intended to be used by the buyer client (Level 2)
     * @param itemId The ID of the listing to be bid on
     * @param newPrice The bid price
     * @param name The name of the bidder - first piece of identifying information
     * @param email The email address of the bidder - second piece of identifying information
     * @return A string (intended for output to console) of the results of the operation. 
     */
    public SignedMessage<String> FPlaceBid(int itemId, int newPrice, Account bidder) throws RemoteException
    {
        if(bidder == null || newPrice < 0)
        {
            return new SignedMessage<String>("Error: invalid arguments", _privateKey);
        }
        String result;
        ForwardAuctionItem toBid;
        synchronized(_forwardAuctionItems)
        {
            // Get the item to be bid on from the hash table
            toBid = _forwardAuctionItems.get(itemId);
            // If no item was found, output that
            if(toBid == null)
            {
                result = "Error: no item for ID: " + itemId + " found";
                return new SignedMessage<String>(result, _privateKey);
            }
            // Prevent bids on own item
            if(bidder.equals(toBid.getSellerAccount()))
            {
                result = "Error: cannot bid on an item you are selling";
                return new SignedMessage<String>(result, _privateKey);
            }
            Account oldHighestBidder = toBid.getHighestBidder();
            boolean bidResult = toBid.newBid(newPrice, bidder);
            // Prevent bids of a lower price than the current highest price
            if(!bidResult)
            {
                result = "Error: suggested price is lower than current price";
            }
            // Output the result
            else
            {
                sendMessage(oldHighestBidder, "Forward auction: You have been outbid on " + toBid.getTitle() +". New highest bid - " +AuctionItem.currencyToString(toBid.getHighestBidAmount()));
                result = "New bid placed on item ID: " + itemId + ". New price: " + AuctionItem.currencyToString(newPrice);
            }
        }
        System.out.println(bidder.getName() + " has placed a new bid on " + toBid.getTitle() + " with a price of " + AuctionItem.currencyToString(newPrice));
        return new SignedMessage<String>(result, _privateKey);
    }

    public SignedMessage<LinkedList<String>> RBrowseListings() throws RemoteException
    {
        LinkedList<String> result = new LinkedList<String>();
        synchronized(_reverseAuctionItems)
        {
            if(_reverseAuctionItems == null)
            {
                result.add("Something has gone wrong");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            if(_reverseAuctionItems.size() == 0)
            {
                result.add("There are no items for sale");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            for(String s : _reverseAuctionItems.keySet())
            {
                ReverseAuctionItem item = _reverseAuctionItems.get(s);
                if(item != null)
                {
                    String toAdd = "\n" +
                    "Name: " + item.getTitle() + "\n" +
                    "   Description: " + item.getDescription() + "\n";
                    if(item.getLowestBidPrice() < 0)
                    {
                        toAdd +=
                        "   There are no active listings for this item";
                    }
                    else
                    {
                        toAdd += 
                        "   Current Lowest Price: " + AuctionItem.currencyToString(item.getLowestBidPrice());
                    }
                    result.add(toAdd);
                }
            }
        }
        return new SignedMessage<LinkedList<String>>(result, _privateKey);
    }
    public SignedMessage<String> RCreateListing(String name, String description) throws RemoteException
    {
        String result = "";
        if(name == null || description == null)
        {
            result = "Invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }
        synchronized(_reverseAuctionItems)
        {
            if(_reverseAuctionItems.containsKey(name))
            {
                result = "Listing already exists";
            }
            _reverseAuctionItems.put(name, new ReverseAuctionItem(name, description));
        }
        result = "Created new listing for " + name;
        System.out.println(result);
        return new SignedMessage<String>(result, _privateKey);
    }
    public SignedMessage<String> RAddEntryToListing(String name, int price, Account seller) throws RemoteException
    {
        String result = "";
        if(name == null || price < 0 || seller == null)
        {
            result = "Error: Invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }
        synchronized(_reverseAuctionItems)
        {
            ReverseAuctionItem item = _reverseAuctionItems.get(name);
            if(item == null)
            {
                result = "Error: Listing does not exist";
                return new SignedMessage<String>(result, _privateKey);
            }
            item.newBid(price, seller);
        }
        result = "Successfully added new offer for " + name + " at " + AuctionItem.currencyToString(price);
        System.out.println(seller.getName() + " added new offer for " + name + " at " + AuctionItem.currencyToString(price));
        return new SignedMessage<String>(result, _privateKey);
    }
    public SignedMessage<String> RBuyItem(String name, Account buyer) throws RemoteException
    {
        String result = "";
        if(name == null || buyer == null) 
        {
            result = "Error: Invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }
        int purchasePrice;
        synchronized(_reverseAuctionItems)
        {
            ReverseAuctionItem rai = _reverseAuctionItems.get(name);
            if(rai == null) 
            {
                result = "Error: No item \"" + name + "\" found";
                return new SignedMessage<String>(result, _privateKey);
            }
            if(buyer.equals(rai.getLowestBidder()))
            {
                result = "Error: You cannot buy an item you are selling";
                return new SignedMessage<String>(result, _privateKey);
            }
            if(Math.abs(rai.getLowestBidPrice() + 1) == -1)
            {
                result = "No active listings for " + name + "have been found.";
                return new SignedMessage<String>(result, _privateKey);
            }
            purchasePrice = rai.getLowestBidPrice();
            sendMessage(rai.getLowestBidder(), "Reverse Auction: Your listing for " + name + " has been sold for " + AuctionItem.currencyToString(purchasePrice));
            rai.buyLowest();
            sendMessage(buyer, "Reverse Auction: You have purchased " + name + " for " + AuctionItem.currencyToString(purchasePrice));
        }
        result = "Purchased " + name + " for " + AuctionItem.currencyToString(purchasePrice);
        System.out.println("Server: " + buyer.getName() + " " + result);
        return new SignedMessage<String>(result, _privateKey);
    }
    public SignedMessage<String> RGetSpec(String name) throws RemoteException
    {
        String result;
        if(name == null) 
        {
            result = "Error: Name cannot be null";
            return new SignedMessage<String>(result, _privateKey);
        }
        ReverseAuctionItem rai = _reverseAuctionItems.get(name);
        synchronized(_reverseAuctionItems)
        {
            if(rai == null) 
            {
                result = "Error: No item \"" + name + "\" found";
                return new SignedMessage<String>(result, _privateKey);
            }
            if(Math.abs(rai.getLowestBidPrice() + 1) == -1)
            {
                result = "No active listings for " + name + "have been found.";
                return new SignedMessage<String>(result, _privateKey);
            }
            result =
            "   Description: " + rai.getDescription() + "\n" +
            "   Lowest price: " + AuctionItem.currencyToString(rai.getLowestBidPrice());
        }
        return new SignedMessage<String>(result, _privateKey);
    }
    public SignedMessage<Boolean> RExists(String name) throws RemoteException
    {
        if(name == null) return new SignedMessage<Boolean>(false, _privateKey);
        synchronized(_reverseAuctionItems)
        {
            ReverseAuctionItem rai = _reverseAuctionItems.get(name);
            if(rai == null || Math.abs(rai.getLowestBidPrice() + 1) == -1) return new SignedMessage<Boolean>(false, _privateKey);
        }
        return new SignedMessage<Boolean>(true, _privateKey);
    }

    public SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException
    {
        LinkedList<String> result = new LinkedList<String>();
        synchronized(_doubleAuctionItems)
        {
            if(_doubleAuctionItems == null)
            {
                result.add("Something has gone wrong");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            if(_doubleAuctionItems.size() == 0)
            {
                result.add("There are no items for sale");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            for(String s : _doubleAuctionItems.keySet())
            {
                DoubleAuctionItem item = _doubleAuctionItems.get(s);
                if(item != null)
                {
                    String toAdd = "\n" +
                    "Name: " + item.getTitle() + "\n" +
                    "   Description: " + item.getDescription() + "\n";
                    if(item.getLastSalePrice() < 0)
                    {
                        toAdd +=
                        "   No transactions have taken place for this item";
                    }
                    else
                    {
                        toAdd +=
                        "   Price of last sale: " + AuctionItem.currencyToString(item.getLastSalePrice());
                    }
                    result.add(toAdd);
                }
            }
        }
        return new SignedMessage<LinkedList<String>>(result, _privateKey);

    }

    /*
     * A function to check for double auction matches. Should be called after a new order (buy or sell) is placed
     * @param item the item category being checked
     * @param isNewOrderSeller true if the newest order was a sell order.
     * @return null if no matches found, the seller if isNewOrderSeller = true, the buyer if isNewOrderSeller = false
     */
    private Account DCheckForMatches(DoubleAuctionItem item, boolean isNewOrderSeller)
    {
        if(item == null) return null;
        Bid[] match = item.match();
        if(match[0] != null && match[1] != null)
        {
            sendMessage(match[0].bidder,
            "Double Auction: Your sell order for " + item.getTitle() + " at " + AuctionItem.currencyToString(match[0].bidPrice) + " has been completed."
            );
            sendMessage(match[1].bidder,
            "Double Auction: Your buy order for " + item.getTitle() + " at " + AuctionItem.currencyToString(match[1].bidPrice) + " has been completed."
            );
            System.out.println("Total profit: " + AuctionItem.currencyToString(Math.abs(match[0].bidPrice - match[1].bidPrice)));
            if(isNewOrderSeller)
            {
                return match[0].bidder;
            }
            else return match[1].bidder;
        }
        else return null;
    }

    public SignedMessage<String> DCreateListing(String name, String description) throws RemoteException
    {
        String result = "";
        if(name == null || description == null)
        {
            result = "Invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }
        synchronized(_doubleAuctionItems)
        {
            if(_doubleAuctionItems.containsKey(name))
            {
                result = "Listing already exists";
            }
            _doubleAuctionItems.put(name, new DoubleAuctionItem(name, description));
        }
        result = "Created new listing for " + name;
        System.out.println(result);
        return new SignedMessage<String>(result, _privateKey);
    }

    public SignedMessage<String> DPlaceSellOrder(String itemName, int sellPrice, Account seller) throws RemoteException
    {
        String result = "";
        if(itemName == null || sellPrice < 0 || seller == null)
        {
            result = "Error: Invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }
        synchronized(_doubleAuctionItems)
        {
            DoubleAuctionItem item = _doubleAuctionItems.get(itemName);
            if(item == null)
            {
                result = "Error: Listing does not exist";
                return new SignedMessage<String>(result, _privateKey);
            }
            if(item.isAccountOnOtherSide(seller, true))
            {
                result = "Error: Cannot sell item you are also buying";
                return new SignedMessage<String>(result, _privateKey);
            }
            item.newSale(sellPrice, seller);
            result = "Successfully placed sell order for " + itemName + " at " + AuctionItem.currencyToString(sellPrice);
            Account matchSeller = DCheckForMatches(item, true);
            if(matchSeller != null && matchSeller.equals(seller))
            {
                result += "\nMatch found, sell order completed.";
            }
        }
        System.out.println(seller.getName() + ": " + result);
        return new SignedMessage<String>(result, _privateKey);
    }
    public SignedMessage<String> DPlaceBuyOrder(String itemName, int buyPrice, Account buyer) throws RemoteException
    {
        String result = "";
        if(itemName == null || buyPrice < 0 || buyer == null)
        {
            result = "Error: Invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }
        synchronized(_doubleAuctionItems)
        {
            DoubleAuctionItem item = _doubleAuctionItems.get(itemName);
            if(item == null)
            {
                result = "Error: Listing does not exist";
                return new SignedMessage<String>(result, _privateKey);
            }
            if(item.isAccountOnOtherSide(buyer, false))
            {
                result = "Error: Cannot buy item you are also selling";
                return new SignedMessage<String>(result, _privateKey);
            }
            item.newBid(buyPrice, buyer);
            result = "Successfully placed buy order for " + itemName + " at " + AuctionItem.currencyToString(buyPrice);
            Account matchBuyer = DCheckForMatches(item, false);
            if(matchBuyer != null && matchBuyer.equals(buyer))
            {
                result += "\nMatch found, buy order completed.";
            }
        }
        System.out.println(buyer.getName() + ": " + result);
        return new SignedMessage<String>(result, _privateKey);
    }
    public SignedMessage<String> DRemoveOrder(String itemName, Account account, boolean removeAll) throws RemoteException
    {
        String result;
        if(itemName == null || account == null)
        {
            result = "Error: invalid arguments";
            return new SignedMessage<String>(result, _privateKey);
        }   
        boolean boolResult;
        synchronized(_doubleAuctionItems)
        {
            DoubleAuctionItem item = _doubleAuctionItems.get(itemName);
            if(item == null) 
            {
                result = "Error: Listing does not exist";
                return new SignedMessage<String>(result, _privateKey);
            }
            boolResult = item.removeOrders(account, removeAll);
        }
        if(boolResult == false)
        {
            result = "No orders for user " + account.getName() + " were found";
            System.out.println(account.getName() + ": " + result);
            return new SignedMessage<String>(result, _privateKey);
        }
        if(removeAll)
        {
            result = "All orders removed.";
            System.out.println(account.getName() + ": " + result);
            return new SignedMessage<String>(result, _privateKey);
        }
        result = "Order removed.";
        System.out.println(account.getName() + ": " + result);
        return new SignedMessage<String>(result, _privateKey);
    }

    public static void main(String[] args) {
        try {
            InputProcessor.clearConsole();
            System.out.println("Starting frontend server...");
            // Setup the server
            FrontendServer s = new FrontendServer();
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