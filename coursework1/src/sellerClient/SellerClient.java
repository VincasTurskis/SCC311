import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
/*
 * A class representing the seller client (level 2)
 */
public class SellerClient{
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
    boolean sessionLoop = true;
    while(sessionLoop)
    {
      Account currentAccount = null;
      System.out.println("Welcome to SellerClient!\n");
      System.out.println("Select an action:");
      System.out.println("0. Exit");
      System.out.println("1. Login");
      System.out.println("2. Create an account");

      // use the InputProcessor class to get the next int from terminal input
      int loginOption = input.ReadNextInt();
      //check if input is correct
      while(loginOption < 0 || loginOption > 2)
      {
        System.out.println("Error: input a number 0-2");
        loginOption = input.ReadNextInt();
      }
      String accountName = "", email = "", password = "";
      switch (loginOption) {
        case 0: // exit
          input.close();
          return;
        case 1: // login
          System.out.println("Enter your email address: ");
          email = input.ReadNextLine();
          System.out.println("Enter your password: ");
          password = input.ReadNextLine();
          System.out.print("Logging in... ");
          try {
            currentAccount = server.login(email, password);
          } catch ( InvalidPasswordException e) {
            System.out.println("Error: the password is not valid. Try again.");
            continue;
          } catch (Exception e)
          {
            System.out.println("Exception:");
            e.printStackTrace();
            return;
          }
          if(currentAccount == null)
          {
            System.out.println("Error: invalid email address or password");
            continue;
          }
          else
          {
            System.out.println("Logged in successfully.");
            System.out.println("Greetings, " + currentAccount.getName());
          }
          break;
        case 2: // create account;
          System.out.println("Enter your name: ");
          accountName = input.ReadNextLine();
          if(accountName.equals(""))
          {
            System.out.println("Error: name cannot be empty.");
            continue;
          }
          System.out.println("Enter your email address: ");
          email = input.ReadNextLine();
          if(email.equals(""))
          {
            System.out.println("Error: email address cannot be empty.");
            continue;
          }
          System.out.println("Enter your password: ");
          password = input.ReadNextLine();
          if(password.equals(""))
          {
            System.out.println("Error: password cannot be empty.");
            continue;
          }
          System.out.println("Enter your password again: ");
          String testPassword = input.ReadNextLine();
          if(!password.equals(testPassword))
          {
            System.out.println("Error: passwords do not match");
            continue;
          }
          System.out.print("Creating an account... ");
          boolean result = false;
          try {
            result = server.createAccount(accountName, email, password);
          } catch ( InvalidPasswordException e) {
            System.out.println("Error: the password is not valid. Try again.");
            continue;
          } catch (Exception e)
          {
            System.out.println("Exception:");
            e.printStackTrace();
            return;
          }
          if(result)
          {
            System.out.println("Account created succesfully.");
          }
          else
          {
            System.out.println("Error: an account with this email address already exists");
            continue;
          }
          // Log in with the new account
          System.out.print("Logging in... ");
          try {
            currentAccount = server.login(email, password);
          } catch ( InvalidPasswordException e) {
            System.out.println("Error: the password is not valid. Try again.");
            continue;
          } catch (Exception e)
          {
            System.out.println("Exception:");
            e.printStackTrace();
            return;
          }
          if(currentAccount == null)
          {
            System.out.println("Error: invalid email address or password");
            continue;
          }
          else
          {
            System.out.println("Logged in successfully.");
            System.out.println("Greetings, " + currentAccount.getName());
          }
          break;
        default:
          break;
      }
      boolean actionLoop = true;
      while(actionLoop)
      {
        // Display selection menu in terminal
        System.out.println("\nSelect an action:");
        System.out.println("0. Exit");
        System.out.println("1. List all active auctions");
        System.out.println("2. Create a new listing");
        System.out.println("3. Close an auction");
        System.out.println("4: Log out");
        // use the InputProcessor class to get the next int from terminal input
        int option = input.ReadNextInt();
        //check if input is correct
        while(option < 0 || option > 4)
        {
          System.out.println("Error: input a number 0-4");
          option = input.ReadNextInt();
        }
        //Switch case for different operations
        switch (option) {
          case 0: // Exit
            input.close();
            return;
          case 1: //Switch case for different operations
            try
            {
              System.out.println(server.FBrowseListings());
            }
            catch (Exception e) {
              System.err.println("Exception:");
              e.printStackTrace();
            }
            break;
          case 2: //Create a new auction listing
            System.out.println("Creating a new listing...");
            String itemName, description;
            float startingPrice, reservePrice;
            // Bloc of code to prompt the user and read their input
            System.out.println("Input name of item: ");
            itemName = input.ReadNextLine();
            if(itemName == "")
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
              int id = server.FCreateAuction(itemName, description, startingPrice, reservePrice, currentAccount);
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
              toPrint = server.FCloseAuction(id, currentAccount);
              System.out.println(toPrint);
            }
            catch(Exception e)
            {
              System.out.println("Exception: ");
              e.printStackTrace();
              return;
            }
            break;
          case 4:
            System.out.println("Logging out...");
            System.out.println("");
            currentAccount = null;
            actionLoop = false;
            continue;
          default:
            break;
        }
      }
    }
  }
}