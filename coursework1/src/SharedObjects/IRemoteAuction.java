import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

/*
 * A remote interface for the server (level 2)
 */
public interface IRemoteAuction extends Remote{
    public  SignedMessage<Boolean> addAccount(Account account) throws InvalidPasswordException, RemoteException, NoConsensusException;
    public  SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException, NoConsensusException;
    public  SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException;
    public  SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException;
    public  SignedMessage<LinkedList<String>> FBrowseListings() throws RemoteException;
    public  SignedMessage<String> FPlaceBid(int itemId, int newPrice, Account bidder) throws RemoteException;
    public  SignedMessage<Integer> FCreateAuction(String title, String description, int startingPrice, int reservePrice, Account seller) throws RemoteException;
    public  SignedMessage<String> FCloseAuction(int auctionId, Account seller) throws RemoteException;
    public  SignedMessage<LinkedList<String>> RBrowseListings() throws RemoteException;
    public  SignedMessage<String> RCreateListing(String name, String description) throws RemoteException;
    public  SignedMessage<String> RAddEntryToListing(String name, int price, Account seller) throws RemoteException;
    public  SignedMessage<String> RBuyItem(String name, Account buyer) throws RemoteException;
    public  SignedMessage<String> RGetSpec(String name) throws RemoteException;
    public  SignedMessage<Boolean> RExists(String name) throws RemoteException;
    public  SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException;
    public  SignedMessage<String> DCreateListing(String name, String description) throws RemoteException;
    public  SignedMessage<String> DPlaceSellOrder(String itemName, int sellPrice, Account seller) throws RemoteException;
    public  SignedMessage<String> DPlaceBuyOrder(String itemName, int buyPrice, Account buyer) throws RemoteException;
    public  SignedMessage<String> DRemoveOrder(String itemName, Account account, boolean removeAll) throws RemoteException;
}
