import java.io.Serializable;

public class AuctionItem implements Serializable {
    public int itemId;
    public String itemTitle;
    public String itemDescription;

    public AuctionItem(int id, String title, String desc)
    {
        itemId = id;
        itemTitle = title;
        itemDescription = desc;
    }
    public String print()
    {
        String s ="\n   ID: " + itemId + "\n" +
                    "   Title: " + itemTitle + "\n" +
                    "   Description: " + itemDescription;
        return s;
    }
}