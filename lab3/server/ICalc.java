import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICalc extends Remote{
    public void StoreData(Student toStore) throws RemoteException;
    public Student RetrieveData(int id) throws RemoteException;
}
