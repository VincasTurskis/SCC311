import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/*
 * A remote interface, intended to be used by the seller client (stage 1 level 2)
 */
public interface ISeller extends Remote {
    public int createAuction(String title, String description, float startingPrice, float reservePrice) throws RemoteException;
    public String closeAuction(int auctionId) throws RemoteException;
    public List<String> browseActiveAuctions() throws RemoteException;
}
