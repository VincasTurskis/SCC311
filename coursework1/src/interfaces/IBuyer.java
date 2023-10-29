import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/*
 * A remote interface, intended to be used by the buyer client (stage 1 level 2)
 */
public interface IBuyer extends Remote{
    public List<String> browseActiveAuctions() throws RemoteException;
    public String placeBid(int itemId, float newPrice, String name, String email) throws RemoteException;

}
