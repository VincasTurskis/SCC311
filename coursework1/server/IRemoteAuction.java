import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteAuction extends Remote{
    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException;
}
