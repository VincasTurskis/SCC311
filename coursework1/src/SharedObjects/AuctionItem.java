import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

/*
 * A class that represents a single auction listing
 */
public class AuctionItem implements Serializable {
    private int _itemId;
    private String _itemTitle;
    private String _itemDescription;
    private float _reservePrice;
    private float _currentBidPrice;
    private float _startingPrice;
    private List<Bid> _bidHistory;  // A list that stores all bids, current and previous, on the item.
                                    // Not useful right now - call it future proofing.
                                    // Possible use is recovery if highest bid on closing is invalid

    /*
     * @param id The ID of the listing - also used as a key in the hash table of the server
     * @param title The title of the listing
     * @param desc A short description of the item
     * @param startingPrice The price that bidding should start on
     * @reservePrice The reserve price; if the highest bid is lower than the reserve price when auction is closed, the item is not sold
     */
    public AuctionItem(int id, String title, String desc, float startingPrice, float reservePrice)
    {
        _bidHistory = new LinkedList<Bid>();
        _itemId = id;
        _itemTitle = title;
        _itemDescription = desc;
        _reservePrice = reservePrice;
        _currentBidPrice = 0;
        _startingPrice = startingPrice;
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
    public float getReservePrice()
    {
        return _reservePrice;
    }
    public String getHighestBidName()
    {
        if(_bidHistory == null || _bidHistory.size() == 0) return "No bid";
        return _bidHistory.get(_bidHistory.size() - 1).bidderName;
    }
    public String getHighestBidEmail()
    {
        if(_bidHistory == null || _bidHistory.size() == 0) return "No bid";
        return _bidHistory.get(_bidHistory.size() - 1).bidderEmail;
    }
    public float getHighestBidAmount()
    {
        return _currentBidPrice;
    }
    public List<Bid> getBidHistory()
    {
        return _bidHistory;
    }
    public float getStartingPrice()
    {
        return _startingPrice;
    }
    /*
     * Creates a new bid on the item.
     * @param newPrice The price of the new bid
     * @param newBuyerName The name of the new bidder
     * @param newBuyerEmail The email of the new bidder
     * @return true if the bid was successful, false if the new price is lower than the previous highest price
     */
    public boolean newBid(float newPrice, String newBuyerName, String newBuyerEmail)
    {
        Bid newBid = new Bid(newPrice, newBuyerName, newBuyerEmail);
        if(newBid.bidPrice <= _currentBidPrice) return false;
        _currentBidPrice = newPrice;
        _bidHistory.add(newBid);
        return true;

    }
    /*
     * A helper class to represent a single bid on this item
     */
    private class Bid
    {
        public float bidPrice;
        public String bidderName;
        public String bidderEmail;
        public Bid(float price, String name, String email)
        {
            bidderName = name;
            bidderEmail = email;
            bidPrice = price;
        }
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
}