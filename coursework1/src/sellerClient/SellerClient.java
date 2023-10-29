import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
/*
 * A class representing the seller client (stage 1 level 2)
 */
public class SellerClient{
  public static void main(String[] args) {
    ISeller server;
    InputProcessor input = new InputProcessor();
    //Acquire the correct server interface from the RMI registry
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
    // A loop allows for multiple operations in a single session
    //Once one operation concludes, allow user to choose another operation
    System.out.println("Welcome to SellerClient!\n");
    boolean loop = true;
    while(loop)
    {
      // Display selection menu in terminal
      System.out.println("\nSelect an action:");
      System.out.println("0. Exit");
      System.out.println("1. List all active auctions");
      System.out.println("2. Create a new listing");
      System.out.println("3. Close an auction");
      // use the InputProcessor class to get the next int from terminal input
      int option = input.ReadNextInt();
      //check if input is correct
      while(option < 0 || option > 3)
      {
        System.out.println("Error: input a number 0-3");
        option = input.ReadNextInt();
      }
      //Switch case for different operations
      switch (option) {
        case 0: // Exit
          loop = false;
          continue;
        case 1: //Switch case for different operations
          try
          {
            System.out.println(server.browseActiveAuctions());
          }
          catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
          }
          break;
        case 2: //Create a new auction listing
          System.out.println("Creating a new listing...");
          String name, description;
          float startingPrice, reservePrice;
          // Bloc of code to prompt the user and read their input
          System.out.println("Input name of item: ");
          name = input.ReadNextLine();
          if(name == "")
          {
            System.out.println("Error: Name cannot be empty");
            continue;
          }
          System.out.println("Write a short description of the item: ");
          description = input.ReadNextLine();
          if(description == "")
          {
            System.out.println("Error: description cannot be empty");
            continue;
          }
          System.out.println("Specify the starting price: ");
          startingPrice = input.ReadNextFloat();
          System.out.println("Specify the reserve price: ");
          reservePrice = input.ReadNextFloat();

          System.out.println("Creating the listing...");
          try
          {
            //Call server function to create a new listing. Return the id if it was successful
            int id = server.createAuction(name, description, startingPrice, reservePrice);
            System.out.println("Listing created. ID: " + id + "\n");
          }
          catch(Exception e)
          {
            System.err.println("Exception:");
            e.printStackTrace();
            return;
          }
          break;
        case 3: // Close an auction
          int id;
          String toPrint;
          // Prompt user for input
          System.out.println("Enter the ID of the listing to close: ");
          id = input.ReadNextInt();
          
          try
          {
            // Call server function to close auction and print the returned status string
            toPrint = server.closeAuction(id);
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