import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * A class to represent a rudimentary auction client (level 1)
 */
public class AuctionClient{
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: java AuctionClient {itemId} {clientId}");
      return;
    }
    // Read argument values. No input sterilization/checking is provided
    int itemId = Integer.parseInt(args[0]);
    int clientId = Integer.parseInt(args[1]);
    try {
      //Connect to the server
      String name = "AuctionServer";
      Registry registry = LocateRegistry.getRegistry("localhost");
      IRemoteAuction server = (IRemoteAuction) registry.lookup(name);
      AuctionItem item = server.getSpec(itemId, clientId);
      if(item == null)
      {
        System.out.println("Client: error, no entry for ID: " + itemId + " in server");
      }
      else
      {
        // Print out the required info about an item
        System.out.println("Client: Item found: " + item.print());
      }
    }
    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
  }
}