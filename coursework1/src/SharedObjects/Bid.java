/*
* A helper class to represent a single bid on this item
*/
public class Bid
{
    public float bidPrice;
    public Account bidder;
    public Bid(float price, Account bidderAccount)
    {
        bidder = bidderAccount;
        bidPrice = price;
    }
}
