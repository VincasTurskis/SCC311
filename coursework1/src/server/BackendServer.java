
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.LinkedList;
import java.util.List;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;

/*
 * A class for all server-side functions of the auction system (level 2)
 */
public class BackendServer{

    private PrivateKey _privateKey;

    private JChannel _channel;

    private RpcDispatcher _dispatcher;

    private ServerState _state;

    // Constructor
    public BackendServer() {
        super();
        _state = new ServerState();
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("sender_keystore.jks"), "auctionPassword".toCharArray());
            _privateKey = (PrivateKey) keyStore.getKey("senderKeyPair", "auctionPassword".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            _channel = new JChannel();
            _channel.connect("AuctionCluster");
            _dispatcher = new RpcDispatcher(_channel, this);
            RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
            _state = _dispatcher.callRemoteMethod(_channel.getView().getMembers().get(0), "getState", new Object[]{}, new Class[]{}, ro);
            Account a = new Account("Example", "a@b.com", "password");
            addAccount(a);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public SignedMessage<Boolean> addAccount(Account a) throws InvalidPasswordException, RemoteException
    {
        if(a == null) return new SignedMessage<Boolean>(false, _privateKey);
        synchronized(_state.accounts)
        {
            Account existing = _state.accounts.get(a.getEmail());
            if(existing != null)
            {
                System.out.println("Server: Account for " + a.getEmail() + " already exists.");
                return new SignedMessage<Boolean>(false, _privateKey);
            }
            _state.accounts.put(a.getEmail(), a);
            _state.messages.put(a, new LinkedList<String>());
        }
        System.out.println("Server: Created account for " + a.getEmail());
        return new SignedMessage<Boolean>(true, _privateKey);
    }

    public SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException
    {
        synchronized(_state.accounts)
        {
            Account a = _state.accounts.get(email);
            if(a == null) return null;
            if(!a.validatePassword(password)) return null;
            System.out.println("Signed in as " + a.getName());
            return new SignedMessage<Account>(a, _privateKey);
        }
    }

    // Using an account received from client as a key in a hashtable doesn't work
    // This method takes the email from the argument account and fetches the correct account from the hash table
    private Account accountTranslation(Account remote)
    {
        if(remote == null) return null;
        synchronized(_state.accounts)
        {
            return _state.accounts.get(remote.getEmail());
        }
    }

    private boolean sendMessage(Account receiver, String message)
    {
        Account tReceiver = accountTranslation(receiver);
        if(tReceiver == null || message == null) return false;
        synchronized(_state.messages)
        {
            if(_state.messages == null) return false;
            List<String> msgList = _state.messages.get(tReceiver);
            if(msgList == null) return false;
            msgList.add(message);
        }
        return true;
    }

    public SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException
    {
        Account tAccount = accountTranslation(account);
        if(tAccount == null) return null;
        synchronized(_state.messages)
        {
            return new SignedMessage<LinkedList<String>>(_state.messages.get(tAccount), _privateKey);
        }
    }

    public SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException
    {
        Account tAccount = accountTranslation(account);
        if(tAccount == null) return new SignedMessage<Boolean>(false, _privateKey);
        synchronized(_state.messages)
        {
            if(_state.messages.get(tAccount) == null) return new SignedMessage<Boolean>(false, _privateKey);
            _state.messages.put(tAccount, new LinkedList<String>());
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
        ForwardAuctionItem newItem = new ForwardAuctionItem(_state.FNextID, title, description, startingPrice, reservePrice, seller);
        synchronized(_state.forwardAuctionItems)
        {
            _state.forwardAuctionItems.put(_state.FNextID, newItem);
        }
        _state.FNextID++;
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
        synchronized(_state.forwardAuctionItems)
        {
            // Check if the arguments are valid
            if(_state.forwardAuctionItems == null)
            {
                result = "Error: the listing database does not exist. Something went very wrong...";
                System.out.println(result);
                return new SignedMessage<String>(result, _privateKey);
            }
            ForwardAuctionItem toClose;
            toClose = _state.forwardAuctionItems.get(auctionId);
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
                _state.forwardAuctionItems.remove(auctionId);
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
        synchronized(_state.forwardAuctionItems)
        {
            if(_state.forwardAuctionItems == null)
            {
                result.add("Something has gone wrong");
            }
            else if(_state.forwardAuctionItems.size() == 0)
            {
                result.add("There are no items for sale");
            }
            else
            {
                // For every item in the hash table, add a formatted string to the list of strings
                for(int i = 1; i < _state.FNextID; i++)
                {
                    ForwardAuctionItem item = _state.forwardAuctionItems.get(i);
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
        synchronized(_state.forwardAuctionItems)
        {
            // Get the item to be bid on from the hash table
            toBid = _state.forwardAuctionItems.get(itemId);
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
        synchronized(_state.reverseAuctionItems)
        {
            if(_state.reverseAuctionItems == null)
            {
                result.add("Something has gone wrong");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            if(_state.reverseAuctionItems.size() == 0)
            {
                result.add("There are no items for sale");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            for(String s : _state.reverseAuctionItems.keySet())
            {
                ReverseAuctionItem item = _state.reverseAuctionItems.get(s);
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
        synchronized(_state.reverseAuctionItems)
        {
            if(_state.reverseAuctionItems.containsKey(name))
            {
                result = "Listing already exists";
            }
            _state.reverseAuctionItems.put(name, new ReverseAuctionItem(name, description));
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
        synchronized(_state.reverseAuctionItems)
        {
            ReverseAuctionItem item = _state.reverseAuctionItems.get(name);
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
        synchronized(_state.reverseAuctionItems)
        {
            ReverseAuctionItem rai = _state.reverseAuctionItems.get(name);
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
        ReverseAuctionItem rai = _state.reverseAuctionItems.get(name);
        synchronized(_state.reverseAuctionItems)
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
        synchronized(_state.reverseAuctionItems)
        {
            ReverseAuctionItem rai = _state.reverseAuctionItems.get(name);
            if(rai == null || Math.abs(rai.getLowestBidPrice() + 1) == -1) return new SignedMessage<Boolean>(false, _privateKey);
        }
        return new SignedMessage<Boolean>(true, _privateKey);
    }

    public SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException
    {
        LinkedList<String> result = new LinkedList<String>();
        synchronized(_state.doubleAuctionItems)
        {
            if(_state.doubleAuctionItems == null)
            {
                result.add("Something has gone wrong");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            if(_state.doubleAuctionItems.size() == 0)
            {
                result.add("There are no items for sale");
                return new SignedMessage<LinkedList<String>>(result, _privateKey);
            }
            for(String s : _state.doubleAuctionItems.keySet())
            {
                DoubleAuctionItem item = _state.doubleAuctionItems.get(s);
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
        synchronized(_state.doubleAuctionItems)
        {
            if(_state.doubleAuctionItems.containsKey(name))
            {
                result = "Listing already exists";
            }
            _state.doubleAuctionItems.put(name, new DoubleAuctionItem(name, description));
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
        synchronized(_state.doubleAuctionItems)
        {
            DoubleAuctionItem item = _state.doubleAuctionItems.get(itemName);
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
        synchronized(_state.doubleAuctionItems)
        {
            DoubleAuctionItem item = _state.doubleAuctionItems.get(itemName);
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
        synchronized(_state.doubleAuctionItems)
        {
            DoubleAuctionItem item = _state.doubleAuctionItems.get(itemName);
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

    public ServerState getState() throws Exception
    {
        System.out.println("Getting state");
        synchronized(_state)
        {
            return _state;
        }
    }
    public static void main(String[] args) {
        try {
            InputProcessor.clearConsole();
            System.out.println("Starting server...");
            // Setup the server
            BackendServer s = new BackendServer();
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}