import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
/*
 * A class representing the buyer/bidder client (stage 1 level 2)
 */
public class BuyerClient{
  public static void main(String[] args) {

    IRemoteAuction server;
    InputProcessor input = new InputProcessor();
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

    // A loop allows for multiple operations in a single session
    //Once one operation concludes, allow user to choose another operation
    System.out.println("Welcome to BuyerClient!\n");
    boolean loop = true;
    while(loop)
    {
      // Display selection menu in terminal
      System.out.println("\nSelect an action:");
      System.out.println("0. Exit");
      System.out.println("1. List all active auctions");
      System.out.println("2. Bid on a listing");

      // use the InputProcessor class to get the next int from terminal input
      int option = input.ReadNextInt();
      //check if input is correct
      while(option < 0 || option > 2)
      {
        System.out.println("Error: input a number 0-2");
        option = input.ReadNextInt();
      }
      //Switch case for different operations
      switch (option) {
        case 0: // Exit
          loop = false;
          continue;
        case 1: // Display list of active auction listings
          try
          {
            System.out.println(server.browseActiveAuctions());
          }
          catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
          }
          break;
        case 2: // Bid on a listing
          System.out.println("Creating a new bid..");
          int id;
          float newPrice;
          // Large block of code for taking the required data for a new bid as input from user
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
            // Try to place the bid based on the supplied parameters;
            // Print the return string of the server function to console
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
    // Close the input
    input.close();
  }
}