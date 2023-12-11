import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

/*
 * A remote interface for the server (level 2)
 */
public interface IRemoteAuction extends Remote{
    public  SignedMessage<Boolean> addAccount(Account account) throws InvalidPasswordException, RemoteException, NoConsensusException;
    public  SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException, NoConsensusException;
    public  SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException, NoConsensusException;
    public  SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException, NoConsensusException;
    public  SignedMessage<LinkedList<String>> FBrowseListings() throws RemoteException, NoConsensusException;
    public  SignedMessage<String> FPlaceBid(Integer itemId, Integer newPrice, Account bidder) throws RemoteException, NoConsensusException;
    public  SignedMessage<Integer> FCreateAuction(String title, String description, Integer startingPrice, Integer reservePrice, Account seller) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> FCloseAuction(Integer auctionId, Account seller) throws RemoteException, NoConsensusException;
    public  SignedMessage<LinkedList<String>> RBrowseListings() throws RemoteException, NoConsensusException;
    public  SignedMessage<String> RCreateListing(String name, String description) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> RAddEntryToListing(String name, Integer price, Account seller) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> RBuyItem(String name, Account buyer) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> RGetSpec(String name) throws RemoteException, NoConsensusException;
    public  SignedMessage<Boolean> RExists(String name) throws RemoteException, NoConsensusException;
    public  SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException, NoConsensusException;
    public  SignedMessage<String> DCreateListing(String name, String description) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> DPlaceSellOrder(String itemName, Integer sellPrice, Account seller) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> DPlaceBuyOrder(String itemName, Integer buyPrice, Account buyer) throws RemoteException, NoConsensusException;
    public  SignedMessage<String> DRemoveOrder(String itemName, Account account, Boolean removeAll) throws RemoteException, NoConsensusException;
}
