import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

public class AuctionItem implements Serializable {
    private int _itemId;
    private String _itemTitle;
    private String _itemDescription;
    private float _reservePrice;
    private float _currentBidPrice;
    private List<Bid> _bidHistory; // a list that stores all bids, current and previous, on the item.
                                    // Possible use is recovery if highest bid on closing is invalid

    public AuctionItem(int id, String title, String desc, float startingPrice, float reservePrice)
    {
        _bidHistory = new LinkedList<Bid>();
        _itemId = id;
        _itemTitle = title;
        _itemDescription = desc;
        _reservePrice = reservePrice;
        _currentBidPrice = startingPrice;

    }
    public String print()
    {
        String s ="\n   ID: " + _itemId + "\n" +
                    "   Title: " + _itemTitle + "\n" +
                    "   Description: " + _itemDescription + "\n";
        return s;
    }
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
        if(_bidHistory == null || _bidHistory.size() == 0) return -1;
        return _bidHistory.get(_bidHistory.size() - 1).bidPrice;
    }
    public String newBid(int newPrice, String newBuyerName, String newBuyerEmail)
    {
        
        if(newPrice <= _currentBidPrice) return "Bid failed: proposed price is lower than current price.";
        _currentBidPrice = newPrice;
        Bid newBid = new Bid(newPrice, newBuyerName, newBuyerEmail);
        _bidHistory.add(newBid);
        return "Bid succeeded.";

    }
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
}