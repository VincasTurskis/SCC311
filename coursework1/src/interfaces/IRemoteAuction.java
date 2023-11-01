import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/*
 * A remote interface for the server (level 2)
 */
public interface IRemoteAuction extends Remote{
    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException;
    public List<String> browseActiveAuctions() throws RemoteException;
    public String placeBid(int itemId, float newPrice, String name, String email) throws RemoteException;
    public int createAuction(String title, String description, float startingPrice, float reservePrice) throws RemoteException;
    public String closeAuction(int auctionId) throws RemoteException;
}
