import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BuyerClient{
  public static void main(String[] args) {
    IBuyer server;
    try {
      String name = "buyerClient";
      Registry registry = LocateRegistry.getRegistry("localhost");
      server = (IBuyer) registry.lookup(name);
    }
    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
      return;
    }
    while(true)
    {
      
    }
  }
}