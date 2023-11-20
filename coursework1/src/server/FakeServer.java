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

public class FakeServer implements IRemoteAuction{

    private PrivateKey _fakeKey;

    private IRemoteAuction server;

    private boolean resign;

    public FakeServer(boolean Resign)
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
            message = resignMessage(message);
        }
        return message;   
    }

    public <T extends Serializable> SignedMessage<T> resignMessage(SignedMessage<T> original)
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
            FakeServer s;
            boolean resign = false;
            // Setup the server
            for(String arg : args)
            {
                if(arg.equals("-resign"))
                {
                    resign = true;
                }
            }
            s = new FakeServer(resign);
            // Setup the different interfaces
            String name = "FakeAuctionServer";
            // Get the RMI registry
            Registry registry = LocateRegistry.getRegistry();
            Remote stub = UnicastRemoteObject.exportObject(s, 0);
            // Advertise the different interfaces (basic, seller, client) on the registry
            registry.rebind(name, (IRemoteAuction) stub);
            System.out.println("Fake server ready");
            if(args.length >= 1 && args[0].equals("resign"))
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
    public SignedMessage<Boolean> createAccount(String name, String email, String password) throws InvalidPasswordException, RemoteException {
        return resignMessage(server.createAccount(name, email, password));
    }

    @Override
    public SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException {
        return resignMessage(server.login(email, password));
    }

    @Override
    public SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException {
        return resignMessage(server.getMessages(account));
    }

    @Override
    public SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException {
        return resignMessage(server.deleteMessages(account));
    }

    @Override
    public SignedMessage<LinkedList<String>> FBrowseListings() throws RemoteException {
        return resignMessage(server.FBrowseListings());
    }

    @Override
    public SignedMessage<String> FPlaceBid(int itemId, int newPrice, Account bidder) throws RemoteException {
        return tamper(server.FPlaceBid(itemId, newPrice, bidder), "Bid was totally placed. 100%. This is not a fake message");
    }

    @Override
    public SignedMessage<Integer> FCreateAuction(String title, String description, int startingPrice, int reservePrice, Account seller) throws RemoteException {
        return resignMessage(server.FCreateAuction(title, description, startingPrice, reservePrice, seller));
    }

    @Override
    public SignedMessage<String> FCloseAuction(int auctionId, Account seller) throws RemoteException {
        return resignMessage(server.FCloseAuction(auctionId, seller));
    }

    @Override
    public SignedMessage<LinkedList<String>> RBrowseListings() throws RemoteException {
        return resignMessage(server.RBrowseListings());
    }

    @Override
    public SignedMessage<String> RCreateListing(String name, String description) throws RemoteException {
        return resignMessage(server.RCreateListing(name, description));
    }

    @Override
    public SignedMessage<String> RAddEntryToListing(String name, int price, Account seller) throws RemoteException {
        return resignMessage(server.RAddEntryToListing(name, price, seller));
    }

    @Override
    public SignedMessage<String> RBuyItem(String name, Account buyer) throws RemoteException {
        return resignMessage(server.RBuyItem(name, buyer));
    }

    @Override
    public SignedMessage<String> RGetSpec(String name) throws RemoteException {
        return resignMessage(server.RGetSpec(name));
    }

    @Override
    public SignedMessage<Boolean> RExists(String name) throws RemoteException {
        return resignMessage(server.RExists(name));
    }

    @Override
    public SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException {
        return resignMessage(server.DBrowseListings());
    }

    @Override
    public SignedMessage<String> DCreateListing(String name, String description) throws RemoteException {
        return resignMessage(server.DCreateListing(name, description));
    }

    @Override
    public SignedMessage<String> DPlaceSellOrder(String itemName, int sellPrice, Account seller)
            throws RemoteException {
        return resignMessage(server.DPlaceSellOrder(itemName, sellPrice, seller));
    }

    @Override
    public SignedMessage<String> DPlaceBuyOrder(String itemName, int buyPrice, Account buyer) throws RemoteException {
        return resignMessage(server.DPlaceBuyOrder(itemName, buyPrice, buyer));
    }

    @Override
    public SignedMessage<String> DRemoveOrder(String itemName, Account account, boolean removeAll)
            throws RemoteException {
        return resignMessage(server.DRemoveOrder(itemName, account, removeAll));
    }

}
