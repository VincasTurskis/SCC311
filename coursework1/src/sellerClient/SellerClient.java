import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class SellerClient{
  public static void main(String[] args) {
    ISeller server;
    InputProcessor input = new InputProcessor();
    try {
      String name = "sellerClient";
      Registry registry = LocateRegistry.getRegistry("localhost");
      server = (ISeller) registry.lookup(name);
    }
    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
      return;
    }
    System.out.println("Welcome to SellerClient!");
    boolean loop = true;
    while(loop)
    {
      System.out.println("Select an action:");
      System.out.println("0. Exit");
      System.out.println("1. List all active auctions");
      System.out.println("2. Create a new listing");
      System.out.println("3. Close an auction");

      int option = input.ReadNextInt();
      while(option < 0 || option > 3)
      {
        System.out.println("Error: input a number 0-3");
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
          String name, description;
          float startingPrice, reservePrice;
          System.out.println("Input name of item: ");
          name = input.ReadNextLine();
          System.out.println("Write a short description of the item: ");
          description = input.ReadNextLine();
          System.out.println("Specify the starting price: ");
          startingPrice = input.ReadNextFloat();
          System.out.println("Specify the reserve price: ");
          reservePrice = input.ReadNextFloat();
          System.out.println("Creating the listing...");
          try
          {
            int id = server.createAuction(name, description, startingPrice, reservePrice);
            System.out.println("Listing created. ID: " + id + "\n");
          }
          catch(Exception e)
          {
            System.err.println("Exception:");
            e.printStackTrace();
          }
          break;
        default:
          break;
      }
    }
    input.close();
  }
}