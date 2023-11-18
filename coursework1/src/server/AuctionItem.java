import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/*
 * An extendable class that represents a single auction listing
 */
public class AuctionItem implements Serializable {
    protected String _itemTitle;
    protected String _itemDescription;
    protected List<Bid> _bids;    // A list that stores all bids on the item
    /*
     * @param id The ID of the listing - also used as a key in the hash table of the server
     * @param title The title of the listing
     * @param desc A short description of the item
     * @param startingPrice The price that bidding should start on
     * @reservePrice The reserve price; if the highest bid is lower than the reserve price when auction is closed, the item is not sold
     */
    public AuctionItem(String title, String desc)
    {
        _bids = new LinkedList<Bid>();
        _itemTitle = title;
        _itemDescription = desc;
    }
    public String getTitle()
    {
        return _itemTitle;
    }
    public String getDescription()
    {
        return _itemDescription;
    }
    /*
     * A static utility function for formatting a currency int into a string truncated down to 2 decimal points
     * @param amount The amount as a int
     * @return the amount as a string
     */
    public static String currencyToString(int amount)
    {
        float amountFloat = (float)amount / 100f;
        String result = String.format("%.02f", amountFloat);
        return result;
    }
}