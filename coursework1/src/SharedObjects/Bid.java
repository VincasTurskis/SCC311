/*
* A helper class to represent a single bid on this item
*/

import java.io.Serializable;

public class Bid implements Serializable
{
    public int bidPrice;
    public Account bidder;
    public Bid(int price, Account bidderAccount)
    {
        bidder = bidderAccount;
        bidPrice = price;
    }
}
