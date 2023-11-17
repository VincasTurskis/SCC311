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
    public boolean isAccountOnOtherSide(Account toCheck, boolean isSeller)
    {
        if(isSeller)
        {
            for(Bid b : _bids)
            {
                if(b.bidder.equals(toCheck)) return true;
            }
            return false;
        }
        else
        {
            for(Bid b : _sales)
            {
                if(b.bidder.equals(toCheck)) return true;
            }
            return false;
        }
    }
    // A function that checks for matches between buyers and sellers
    // Supposed to be called after a new order (buy or sell) is created
    // returns an array of 2 bids - [0] is the seller's bid, [1] is the buyer's
    public Bid[] match()
    {
        Bid[] result = new Bid[2];
        result[0] = null; result[1] = null;
        float lowestSale = getLowestSellPrice(), highestBuy = getHighestBuyPrice();
        if(lowestSale < 0 || highestBuy < 0) return result;
        if(lowestSale > highestBuy) return result;

        Bid buy = _bids.remove(0), sell = _sales.remove(0);
        result[0] = sell;
        result[1] = buy;
        _lastSalePrice = sell.bidPrice;
        return result;
    }

    public boolean removeOrders(Account owner, boolean all)
    {
        if(owner == null) return false;
        boolean found = false;
        for(int i = 0; i < _bids.size(); i++)
        {
            Bid b = _bids.get(i);
            if(b.bidder.equals(owner))
            {
                found = true;
                _bids.remove(b);
                i--;
                if(!all) return found;
            }
        }
        for(int i = 0; i < _sales.size(); i++)
        {
            Bid b = _sales.get(i);
            if(b.bidder.equals(owner))
            {
                found = true;
                _sales.remove(b);
                i--;
                if(!all) return found;
            }
        }
        return found;
    }
}
