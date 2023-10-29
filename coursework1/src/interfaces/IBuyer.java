import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface IBuyer extends Remote{
    public List<String> browseActiveAuctions() throws RemoteException;
    public boolean placeBid(int itemId, float newPrice, String name, String email) throws RemoteException;

}
