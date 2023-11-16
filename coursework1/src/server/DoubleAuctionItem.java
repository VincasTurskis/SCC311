// A class representing both the reverse and double auction items

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DoubleAuctionItem extends AuctionItem{
    protected List<Bid> _sales;
    private float _lastSalePrice;
    public DoubleAuctionItem(String title, String desc)
    {
        super(title, desc);
        _sales = new LinkedList<Bid>();
        _lastSalePrice = -1;
    }
    public void newBid(float price, Account bidder)
    {
        Bid b = new Bid(price, bidder);
        _bids.add(b);
        // Sort the list of bids based on highest price
        while(_bids.indexOf(b) > 0 && _bids.get(_bids.indexOf(b) - 1).bidPrice < b.bidPrice)
        {
            int index = _bids.indexOf(b);
            Collections.swap(_bids, index, index-1);
        }
        return;
    }
    public void newSale(float price, Account seller)
    {
        Bid b = new Bid(price, seller);
        _sales.add(b);
        // Sort the list of sales based on lowest price
        while(_sales.indexOf(b) > 0 && _sales.get(_sales.indexOf(b) - 1).bidPrice > b.bidPrice)
        {
            int index = _sales.indexOf(b);
            Collections.swap(_sales, index, index-1);
        }
        return;
    }

    public float getLowestSellPrice()
    {
        if(_sales.size() <= 0)
        {
            return -1;
        }
        return _sales.get(0).bidPrice;
    }
    public Account getLowestSeller()
    {
        if(_sales.size() <= 0)
        {
            return null;
        }
        return _sales.get(0).bidder;
    }
    public float getHighestBuyPrice()
    {
        if(_bids.size() <= 0)
        {
            return -1;
        }
        return _bids.get(0).bidPrice;
    }
    public Account getHighestBuyer()
    {
        if(_bids.size() <= 0)
        {
            return null;
        }
        return _bids.get(0).bidder;
    }
    public float getLastSalePrice()
    {
        return _lastSalePrice;
    }
}
