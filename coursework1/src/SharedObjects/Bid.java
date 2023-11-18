/*
* A helper class to represent a single bid on this item
*/
public class Bid
{
    public int bidPrice;
    public Account bidder;
    public Bid(int price, Account bidderAccount)
    {
        bidder = bidderAccount;
        bidPrice = price;
    }
}
