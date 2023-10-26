import java.io.Serializable;

public class AuctionItem implements Serializable {
    private int _itemId;
    private String _itemTitle;
    private String _itemDescription;
    private int _reservePrice;
    private int _currentBidPrice;
    private int _currentBuyerId;

    public AuctionItem(int id, String title, String desc, int startingPrice, int reservePrice)
    {
        _itemId = id;
        _itemTitle = title;
        _itemDescription = desc;
        _reservePrice = reservePrice;
        _currentBidPrice = startingPrice;
        _currentBuyerId = -1;
    }
    public String print()
    {
        String s ="\n   ID: " + _itemId + "\n" +
                    "   Title: " + _itemTitle + "\n" +
                    "   Description: " + _itemDescription;
        return s;
    }
    public int getId()
    {
        return _itemId;
    }
    public int getCurrentBuyerId()
    {
        return _currentBuyerId;
    }
    public void newBid(int newPrice, int newBuyerId)
    {
        if(newPrice <= _currentBidPrice) return;
        _currentBidPrice = newPrice;
        _currentBuyerId = newBuyerId;
    }
    public int getReservePrice()
    {
        return _reservePrice;
    }
}