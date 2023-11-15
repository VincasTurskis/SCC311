// A class representing both the reverse and double auction items

import java.util.Collections;

public class ReverseAuctionItem extends AuctionItem{
    public ReverseAuctionItem(String title, String desc)
    {
        super(title, desc);
    }
    public void newBid(float price, Account bidder)
    {
        Bid b = new Bid(price, bidder);
        _bids.add(b);
        // Sort the list of bids based on lowest price
        while(_bids.indexOf(b) > 0 && _bids.get(_bids.indexOf(b) - 1).bidPrice > b.bidPrice)
        {
            int index = _bids.indexOf(b);
            Collections.swap(_bids, index, index-1);
        }
        return;
    }
    public float getLowestPrice()
    {
        if(_bids.size() <= 0)
        {
            return -1;
        }
        return _bids.get(0).bidPrice;
    }
}
