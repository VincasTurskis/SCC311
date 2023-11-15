import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/*
 * An extendable class that represents a single auction listing
 */
public class AuctionItem implements Serializable {
    protected int _itemId;
    protected String _itemTitle;
    protected String _itemDescription;
    protected Account _seller;
    protected List<Bid> _bids;    // A list that stores all bids on the item
    /*
     * @param id The ID of the listing - also used as a key in the hash table of the server
     * @param title The title of the listing
     * @param desc A short description of the item
     * @param startingPrice The price that bidding should start on
     * @reservePrice The reserve price; if the highest bid is lower than the reserve price when auction is closed, the item is not sold
     */
    public AuctionItem(int id, String title, String desc, Account seller)
    {
        _bids = new LinkedList<Bid>();
        _itemId = id;
        _itemTitle = title;
        _itemDescription = desc;
        _seller = seller;
    }
    /*
     * Prints a summary of the info of the items (Stage 1 Level 1)
     * @return a string (formatted for printing) with the information
     */
    public String print()
    {
        String s ="\n   ID: " + _itemId + "\n" +
                    "   Title: " + _itemTitle + "\n" +
                    "   Description: " + _itemDescription + "\n";
        return s;
    }
    //Getters
    public int getId()
    {
        return _itemId;
    }
    public String getTitle()
    {
        return _itemTitle;
    }
    public String getDescription()
    {
        return _itemDescription;
    }
    public Account getSellerAccount()
    {
        return _seller;
    }
    /*
     * A static utility function for formatting a currency float into a string truncated down to 2 decimal points
     * @param amount The amount as a float
     * @return the amount as a string
     */
    public static String currencyToString(float amount)
    {
        String result = String.format("%.02f", amount);
        return result;
    }
    /*
     * A helper class to represent a single bid on this item
     */
    protected class Bid
    {
        public float bidPrice;
        public Account bidder;
        public Bid(float price, Account bidderAccount)
        {
            bidder = bidderAccount;
            bidPrice = price;
        }
    }
}