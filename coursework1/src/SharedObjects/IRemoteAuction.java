import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/*
 * A remote interface for the server (level 2)
 */
public interface IRemoteAuction extends Remote{
    public boolean createAccount(String name, String email, String password) throws InvalidPasswordException, RemoteException;
    public Account login(String email, String password) throws InvalidPasswordException, RemoteException;
    public List<String> getMessages(Account account) throws RemoteException;
    public ForwardAuctionItem getSpec (int itemId, int clientId) throws RemoteException;
    public List<String> FBrowseListings() throws RemoteException;
    public String FPlaceBid(int itemId, float newPrice, Account bidder) throws RemoteException;
    public int FCreateAuction(String title, String description, float startingPrice, float reservePrice, Account seller) throws RemoteException;
    public String FCloseAuction(int auctionId, Account seller) throws RemoteException;
    public List<String> RBrowseListings() throws RemoteException;
    public String RCreateListing(String name, String description) throws RemoteException;
    public String RAddEntryToListing(String name, float price, Account seller) throws RemoteException;
    public String RBuyItem(String name, Account buyer) throws RemoteException;
    public String RGetSpec(String name) throws RemoteException;
    public boolean RExists(String name) throws RemoteException;
    public List<String> DBrowseListings() throws RemoteException;
    public String DCreateListing(String name, String description) throws RemoteException;
    public String DPlaceSellOrder(String itemName, float sellPrice, Account seller) throws RemoteException;
    public String DPlaceBuyOrder(String itemName, float buyPrice, Account buyer) throws RemoteException;
    public String DRemoveOrder(String itemName, Account account, boolean removeAll) throws RemoteException;
}
