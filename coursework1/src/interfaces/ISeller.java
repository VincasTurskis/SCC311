import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface ISeller extends Remote {
    public int createAuction(String title, String description, float startingPrice, float reservePrice) throws RemoteException;
    public String closeAuction(int auctionId) throws RemoteException;
    public List<String> browseActiveAuctions() throws RemoteException;
}
