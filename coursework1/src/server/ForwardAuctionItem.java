import java.util.List;

public class ForwardAuctionItem extends AuctionItem{
    protected int _itemId;
    protected int _reservePrice;
    protected int _currentBidPrice;
    protected int _startingPrice;
    protected Account _seller;
    public ForwardAuctionItem(int id, String title, String desc, int startingPrice, int reservePrice, Account seller)
    {
        super(title, desc);
        _itemId = id;
        _reservePrice = reservePrice;
        _currentBidPrice = 0;
        _startingPrice = startingPrice;
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
    public int getStartingPrice()
    {
        return _startingPrice;
    }
    public int getHighestBidAmount()
    {
        return _currentBidPrice;
    }
    public int getReservePrice()
    {
        return _reservePrice;
    }
        public List<Bid> getBidHistory()
    {
        return _bids;
    }
    public Account getHighestBidder()
    {
        if(_bids == null || _bids.size() == 0) return null;
        return _bids.get(_bids.size() - 1).bidder;
    }
    public boolean hasABid()
    {
        if(_bids == null || _bids.size() == 0) return false;
        return true;
    }
    public Account getSellerAccount()
    {
        return _seller;
    }
    /*
     * Creates a new bid on the item.
     * @param newPrice The price of the new bid
     * @param newBuyerName The name of the new bidder
     * @param newBuyerEmail The email of the new bidder
     * @return true if the bid was successful, false if the new price is lower than the previous highest price
     */
    public boolean newBid(int newPrice, Account bidder)
    {
        Bid newBid = new Bid(newPrice, bidder);
        if(newBid.bidPrice <= _currentBidPrice) return false;
        _currentBidPrice = newPrice;
        _bids.add(newBid);
        return true;
    }
}

