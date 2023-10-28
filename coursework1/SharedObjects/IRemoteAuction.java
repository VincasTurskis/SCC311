import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRemoteAuction extends Remote{
    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException;
    public int createAuction(String title, String description, int startingPrice, int reservePrice) throws RemoteException;
    public String closeAuction(int auctionId) throws RemoteException;
    public List<String> browseActiveAuctions() throws RemoteException;
    public boolean placeBid(int itemId, float newPrice, String name, String email) throws RemoteException;
}
