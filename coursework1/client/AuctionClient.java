import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AuctionClient{
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: java AuctionClient {itemId} {clientId}");
      return;
    }
    int itemId = Integer.parseInt(args[0]);
    int clientId = Integer.parseInt(args[1]);
    try {
      String name = "myserver";
      Registry registry = LocateRegistry.getRegistry("localhost");
      IRemoteAuction server = (IRemoteAuction) registry.lookup(name);
      AuctionItem item = server.getSpec(itemId, clientId);
      if(item == null)
      {
        System.out.println("Client: error, no entry for ID: " + itemId + "in server");
      }
      else
      {
        System.out.println("Client: Item found: " + item.print());
      }
    }
    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }
}