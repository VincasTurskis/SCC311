import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
/*
 * A class representing the buyer/bidder client (level 2)
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
    boolean sessionLoop = true;
    while(sessionLoop)
    {
      Account currentAccount = null;
      System.out.println("Welcome to BuyerClient!\n");
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

      // A loop allows for multiple operations in a single session
      //Once one operation concludes, allow user to choose another operation
      boolean actionLoop = true;
      while(actionLoop)
      {
        // Display selection menu in terminal
        System.out.println("\nSelect an action:");
        System.out.println("0. Exit");
        System.out.println("1. List all active auctions");
        System.out.println("2. Bid on a listing");
        System.out.println("3. Log out");

        // use the InputProcessor class to get the next int from terminal input
        int actionOption = input.ReadNextInt();
        //check if input is correct
        while(actionOption < 0 || actionOption > 3)
        {
          System.out.println("Error: input a number 0-3");
          actionOption = input.ReadNextInt();
        }
        //Switch case for different operations
        switch (actionOption) {
          case 0: // Exit
            input.close();
            return;
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
            // Block of code for taking the required data for a new bid as input from user
            System.out.println("Input ID of item: ");
            id = input.ReadNextInt();
            System.out.println("Specify the amount to bid: ");
            newPrice = input.ReadNextFloat();
            System.out.println("Creating the bid...");
            try
            {
              // Try to place the bid based on the supplied parameters;
              // Print the return string of the server function to console
              String toPrint = server.placeBid(id, newPrice, currentAccount);
              System.out.println(toPrint);
            }
            catch(Exception e)
            {
              System.out.println("Exception: ");
              e.printStackTrace();
              return;
            }
            break;
          case 3: // log out
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