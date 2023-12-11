import java.io.FileInputStream;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.LinkedList;

public class FakeFrontendServer implements IRemoteAuction{

    private PrivateKey _fakeKey;

    private IRemoteAuction server;

    private Boolean resign;

    public FakeFrontendServer(Boolean Resign)
    {
        resign = Resign;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("fake_keystore.jks"), "FakePassword".toCharArray());
            _fakeKey = (PrivateKey) keyStore.getKey("fakeKeyPair", "FakePassword".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //Acquire the correct server interface from the RMI registry
        try {
            String name = "AuctionServer";
            Registry registry = LocateRegistry.getRegistry("localhost");
            server = (IRemoteAuction) registry.lookup(name);
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
            return;
        }
    }

    public <T extends Serializable> SignedMessage<T> tamper(SignedMessage<T> original, T newMessage)
    {
        SignedMessage<T> message = new SignedMessage<T>(newMessage, original.getSignature());
        if(resign)
        {
            message = proxyMessage(message);
        }
        return message;   
    }

    public <T extends Serializable> SignedMessage<T> proxyMessage(SignedMessage<T> original)
    {
        if(resign)
        {
            return new SignedMessage<T>(original.getMessage(), _fakeKey);
        }
        else return original;
    }

    public static void main(String args[])
    {
        try {
            InputProcessor.clearConsole();
            System.out.println("Starting fake server...");
            FakeFrontendServer s;
            boolean resign = false;
            // Setup the server
            for(String arg : args)
            {
                if(arg.equals("-resign"))
                {
                    resign = true;
                }
            }
            s = new FakeFrontendServer(resign);
            // Setup the different interfaces
            String name = "FakeAuctionServer";
            // Get the RMI registry
            Registry registry = LocateRegistry.getRegistry();
            Remote stub = UnicastRemoteObject.exportObject(s, 0);
            // Advertise the different interfaces (basic, seller, client) on the registry
            registry.rebind(name, (IRemoteAuction) stub);
            System.out.println("Fake server ready");
            if(resign)
            {
                System.out.println("This server will re-sign all messages with its own private key");
            }
            System.out.println("This server will tamper with an attempt to place a forward auction bid. Try it!");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    @Override
    public SignedMessage<Boolean> addAccount(Account a) throws InvalidPasswordException, RemoteException, NoConsensusException {
        return proxyMessage(server.addAccount(a));
    }

    @Override
    public SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException, NoConsensusException {
        return proxyMessage(server.login(email, password));
    }

    @Override
    public SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException, NoConsensusException {
        return proxyMessage(server.getMessages(account));
    }

    @Override
    public SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException, NoConsensusException {
        return proxyMessage(server.deleteMessages(account));
    }

    @Override
    public SignedMessage<LinkedList<String>> FBrowseListings() throws RemoteException, NoConsensusException{
        return proxyMessage(server.FBrowseListings());
    }

    @Override
    public SignedMessage<String> FPlaceBid(Integer itemId, Integer newPrice, Account bidder) throws RemoteException, NoConsensusException{
        return tamper(server.FPlaceBid(itemId, newPrice, bidder), "Bid was totally placed. 100%. This is not a fake message");
    }

    @Override
    public SignedMessage<Integer> FCreateAuction(String title, String description, Integer startingPrice, Integer reservePrice, Account seller) throws RemoteException, NoConsensusException {
        return proxyMessage(server.FCreateAuction(title, description, startingPrice, reservePrice, seller));
    }

    @Override
    public SignedMessage<String> FCloseAuction(Integer auctionId, Account seller) throws RemoteException, NoConsensusException {
        return proxyMessage(server.FCloseAuction(auctionId, seller));
    }

    @Override
    public SignedMessage<LinkedList<String>> RBrowseListings() throws RemoteException, NoConsensusException {
        return proxyMessage(server.RBrowseListings());
    }

    @Override
    public SignedMessage<String> RCreateListing(String name, String description) throws RemoteException, NoConsensusException {
        return proxyMessage(server.RCreateListing(name, description));
    }

    @Override
    public SignedMessage<String> RAddEntryToListing(String name, Integer price, Account seller) throws RemoteException, NoConsensusException {
        return proxyMessage(server.RAddEntryToListing(name, price, seller));
    }

    @Override
    public SignedMessage<String> RBuyItem(String name, Account buyer) throws RemoteException, NoConsensusException {
        return proxyMessage(server.RBuyItem(name, buyer));
    }

    @Override
    public SignedMessage<String> RGetSpec(String name) throws RemoteException, NoConsensusException {
        return proxyMessage(server.RGetSpec(name));
    }

    @Override
    public SignedMessage<Boolean> RExists(String name) throws RemoteException, NoConsensusException {
        return proxyMessage(server.RExists(name));
    }

    @Override
    public SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException, NoConsensusException {
        return proxyMessage(server.DBrowseListings());
    }

    @Override
    public SignedMessage<String> DCreateListing(String name, String description) throws RemoteException, NoConsensusException {
        return proxyMessage(server.DCreateListing(name, description));
    }

    @Override
    public SignedMessage<String> DPlaceSellOrder(String itemName, Integer sellPrice, Account seller)
            throws RemoteException, NoConsensusException {
        return proxyMessage(server.DPlaceSellOrder(itemName, sellPrice, seller));
    }

    @Override
    public SignedMessage<String> DPlaceBuyOrder(String itemName, Integer buyPrice, Account buyer) throws RemoteException, NoConsensusException {
        return proxyMessage(server.DPlaceBuyOrder(itemName, buyPrice, buyer));
    }

    @Override
    public SignedMessage<String> DRemoveOrder(String itemName, Account account, Boolean removeAll)
            throws RemoteException, NoConsensusException {
        return proxyMessage(server.DRemoveOrder(itemName, account, removeAll));
    }

}
