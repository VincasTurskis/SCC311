import java.io.FileInputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.util.LinkedList;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

/*
 * A class for all server-side functions of the auction system (level 2)
 */
public class FrontendServer implements IRemoteAuction{
    private JChannel _channel;
    private RpcDispatcher _dispacher;

    // Constructor
    public FrontendServer() {
        super();

        try {
            _channel = new JChannel();
            _channel.connect("AuctionCluster");
            _channel.setDiscardOwnMessages(true);
            _dispacher = new RpcDispatcher(_channel, this);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println(_channel.getViewAsString());
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("sender_keystore.jks"), "auctionPassword".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            
            addAccount(new Account("Example", "a@b.com", "password"));
            Account exampleSeller = login("a@b.com", "password").getMessage();
            //Create a few test items to test buyer client without having to use seller client input
            FCreateAuction("Cup", "A nice cup.", 1000, 1000, exampleSeller);
            RCreateListing("Plate", "An antique plate");
            RAddEntryToListing("Plate", 1000, exampleSeller);
            DCreateListing("Fork", "A fancy fork");
            DPlaceSellOrder("Fork", 1000, exampleSeller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SignedMessage<Boolean> addAccount(Account a) throws InvalidPasswordException, RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Boolean>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "addAccount", new Object[]{a}, new Class[]{a.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Boolean> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<Account> login(String email, String password) throws InvalidPasswordException, RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Account>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "login", new Object[]{email, password}, new Class[]{email.getClass(), password.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Account> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<LinkedList<String>> getMessages(Account account) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<LinkedList<String>>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "getMessages", new Object[]{account}, new Class[]{account.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<LinkedList<String>> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<Boolean> deleteMessages(Account account) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Boolean>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "deleteMessages", new Object[]{account}, new Class[]{account.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Boolean> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<Integer> FCreateAuction(String title, String description, Integer startingPrice, Integer reservePrice, Account seller) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Integer>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "FCreateAuction", new Object[]{title, description, startingPrice, reservePrice, seller}, new Class[]{title.getClass(), description.getClass(), startingPrice.getClass(), reservePrice.getClass(), seller.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Integer> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<String> FCloseAuction(Integer auctionId, Account requestSource) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "FCloseAuction", new Object[]{auctionId, requestSource}, new Class[]{auctionId.getClass(), requestSource.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<LinkedList<String>> FBrowseListings() throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<LinkedList<String>>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "FBrowseListings", new Object[]{}, new Class[]{}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<LinkedList<String>> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<String> FPlaceBid(Integer itemId, Integer newPrice, Account bidder) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "FPlaceBid", new Object[]{itemId, newPrice, bidder}, new Class[]{itemId.getClass(), newPrice.getClass(), bidder.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<LinkedList<String>> RBrowseListings() throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<LinkedList<String>>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "RBrowseListings", new Object[]{}, new Class[]{}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<LinkedList<String>> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<String> RCreateListing(String name, String description) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "RCreateListing", new Object[]{name, description}, new Class[]{name.getClass(), description.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<String> RAddEntryToListing(String name, Integer price, Account seller) throws RemoteException, NoConsensusException {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "RAddEntryToListing", new Object[]{name, price, seller}, new Class[]{name.getClass(), price.getClass(), seller.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<String> RBuyItem(String name, Account buyer) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "RBuyItem", new Object[]{name, buyer}, new Class[]{name.getClass(), buyer.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<String> RGetSpec(String name) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "RGetSpec", new Object[]{name}, new Class[]{name.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<Boolean> RExists(String name) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<Boolean>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "RExists", new Object[]{name}, new Class[]{name.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<Boolean> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<LinkedList<String>> DBrowseListings() throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<LinkedList<String>>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "DBrowseListings", new Object[]{}, new Class[]{}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<LinkedList<String>> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<String> DCreateListing(String name, String description) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "DCreateListing", new Object[]{name, description}, new Class[]{name.getClass(), description.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public SignedMessage<String> DPlaceSellOrder(String itemName, Integer sellPrice, Account seller) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "DPlaceSellOrder", new Object[]{itemName, sellPrice, seller}, new Class[]{itemName.getClass(), sellPrice.getClass(), seller.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<String> DPlaceBuyOrder(String itemName, Integer buyPrice, Account buyer) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "DPlaceBuyOrder", new Object[]{itemName, buyPrice, buyer}, new Class[]{itemName.getClass(), buyPrice.getClass(), buyer.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public SignedMessage<String> DRemoveOrder(String itemName, Account account, Boolean removeAll) throws RemoteException, NoConsensusException
    {
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        RspList<SignedMessage<String>> answers;
        try {
            answers = _dispacher.callRemoteMethods(null, "DRemoveOrder", new Object[]{itemName, account, removeAll}, new Class[]{itemName.getClass(), account.getClass(), removeAll.getClass()}, ro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            SignedMessage<String> message = InputProcessor.GetConsensusMessage(answers);
            return message;
        } catch (NoConsensusException e) {
            throw e;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public ServerState getState() throws Exception
    {
        if(_channel.getView().getMembers().size() == 1)
        {
            throw new Exception("No backend servers active");
        }
        System.out.println("Redirecting state transfer request to a backend server");
        Address nextBackend = _channel.getView().getMembers().get(1);
        RequestOptions ro = new RequestOptions(ResponseMode.GET_ALL, 2000, false);
        return _dispacher.callRemoteMethod(nextBackend, "getState", new Object[]{}, new Class[]{}, ro);
    }

    public static void main(String[] args) {
        try {
            InputProcessor.clearConsole();
            System.out.println("Starting frontend server...");
            // Setup the server
            FrontendServer s = new FrontendServer();
            // Setup the different interfaces
            String name = "AuctionServer";
            // Get the RMI registry
            Registry registry = LocateRegistry.getRegistry();
            Remote stub = UnicastRemoteObject.exportObject(s, 0);
            // Advertise the different interfaces (basic, seller, client) on the registry
            registry.rebind(name, (IRemoteAuction) stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}