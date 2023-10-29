import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * A remote interface, intended to be used by the basic client (stage 1 level 1)
 */
public interface IRemoteAuction extends Remote{
    public AuctionItem getSpec (int itemId, int clientId) throws RemoteException;
}
