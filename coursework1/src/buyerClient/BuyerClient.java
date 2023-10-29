import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BuyerClient{
  public static void main(String[] args) {
    IBuyer server;
    InputProcessor input = new InputProcessor();
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
    boolean loop = true;
    while(loop)
    {
      System.out.println("\nSelect an action:");
      System.out.println("0. Exit");
      System.out.println("1. List all active auctions");
      System.out.println("2. Bid on a listing");

      int option = input.ReadNextInt();
      while(option < 0 || option > 2)
      {
        System.out.println("Error: input a number 0-2");
        input.ReadNextInt();
      }
      switch (option) {
        case 0:
          loop = false;
          continue;
        case 1:
          try
          {
            System.out.println(server.browseActiveAuctions());
          }
          catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
          }
          break;
        case 2:
          System.out.println("Creating a new listing...");
          int id;
          float newPrice;
          String name, email;
          System.out.println("Input ID of item: ");
          id = input.ReadNextInt();
          System.out.println("Specify the amount to bid: ");
          newPrice = input.ReadNextFloat();
          System.out.println("Bidder's name: ");
          name = input.ReadNextLine();
          if(name == "")
          {
            System.out.println("Error: name cannot be empty");
            continue;
          }
          System.out.println("Bidder's email address: ");
          email = input.ReadNextLine();
          if(email == "")
          {
            System.out.println("Error: email address cannot be empty");
            continue;
          }
          System.out.println("Creating the bid...");
          try
          {
            String toPrint = server.placeBid(id, newPrice, name, email);
            System.out.println(toPrint);
          }
          catch(Exception e)
          {
            System.out.println("Exception: ");
            e.printStackTrace();
            return;
          }
          break;
        default:
          break;
      }
    }
    input.close();
  }
}