import java.util.LinkedList;
import java.util.List;

public class ForwardAuctionItem extends AuctionItem{
    protected float _reservePrice;
    protected float _currentBidPrice;
    protected float _startingPrice;
    protected List<Bid> _bidHistory;  // A list that stores all bids, current and previous, on the item.
    // Not useful right now - call it future proofing.
    // Possible use is recovery if highest bid on closing is invalid
    public ForwardAuctionItem(int id, String title, String desc, float startingPrice, float reservePrice, Account seller)
    {
        super(id, title, desc, seller);
        _bidHistory = new LinkedList<Bid>();
        _reservePrice = reservePrice;
        _currentBidPrice = 0;
        _startingPrice = startingPrice;
    }
    public float getStartingPrice()
    {
        return _startingPrice;
    }
    public float getHighestBidAmount()
    {
        return _currentBidPrice;
    }
    public float getReservePrice()
    {
        return _reservePrice;
    }
        public List<Bid> getBidHistory()
    {
        return _bidHistory;
    }
    public String getHighestBidName()
    {
        if(_bidHistory == null || _bidHistory.size() == 0) return "No bid";
        return _bidHistory.get(_bidHistory.size() - 1).bidder.getName();
    }
    public String getHighestBidEmail()
    {
        if(_bidHistory == null || _bidHistory.size() == 0) return "No bid";
        return _bidHistory.get(_bidHistory.size() - 1).bidder.getEmail();
    }
    /*
     * Creates a new bid on the item.
     * @param newPrice The price of the new bid
     * @param newBuyerName The name of the new bidder
     * @param newBuyerEmail The email of the new bidder
     * @return true if the bid was successful, false if the new price is lower than the previous highest price
     */
    public boolean newBid(float newPrice, Account bidder)
    {
        Bid newBid = new Bid(newPrice, bidder);
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
        public Account bidder;
        public Bid(float price, Account bidderAccount)
        {
            bidder = bidderAccount;
            bidPrice = price;
        }
    }
}

