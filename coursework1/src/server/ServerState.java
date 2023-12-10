import java.util.Hashtable;
import java.util.LinkedList;

public class ServerState {
    // Hash table for all currently listed items for the forward auction
    // The key is the same as the ID field of AuctionItem
    public Hashtable<Integer, ForwardAuctionItem> forwardAuctionItems;

    // Hash table for all items for the reverse auction
    // The key is the item name, the value is a list of all the listings in the category

    public Hashtable<String, ReverseAuctionItem> reverseAuctionItems;

    // Hash table for all items for the double auction
    // The key is the item name, the value is a list of all buy and sell orders for the item.

    public Hashtable<String, DoubleAuctionItem> doubleAuctionItems;


    // A counter field for the number that will be used as the id for the next added listing
    public Hashtable<String, Account> accounts;
    public Hashtable<Account, LinkedList<String>> messages;
    public int FNextID = 1;

    public ServerState()
    {
        forwardAuctionItems = new Hashtable<Integer, ForwardAuctionItem>();
        reverseAuctionItems = new Hashtable<String, ReverseAuctionItem>();
        doubleAuctionItems = new Hashtable<String, DoubleAuctionItem>();
        accounts = new Hashtable<String, Account>();
        messages = new Hashtable<Account, LinkedList<String>>();
    }
}
