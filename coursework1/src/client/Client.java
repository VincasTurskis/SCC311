import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
/*
* A class representing the shared buyer/seller client (level 3-5)
*/
public class Client{
  public static void main(String[] args) {
    IRemoteAuction server;
    InputProcessor input = new InputProcessor();
    PublicKey publicKey;
    boolean printHash = false;
    InputProcessor.clearConsole();
    String serverName = "AuctionServer";
    for(String arg: args)
    {
      if(arg.equals("-verbose"))
      {
        System.out.println("Starting Client in verbose mode");
        printHash = true;
      }
      if(arg.equals("-fake"))
      {
        serverName = "FakeAuctionServer";
        System.out.println("Connecting to a definitely not fake server...");
      }
    }
    try {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(new FileInputStream("receiver_keystore.jks"), "auctionPassword".toCharArray());
      Certificate certificate = keyStore.getCertificate("receiverKeyPair");
      publicKey = certificate.getPublicKey();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    //Acquire the correct server interface from the RMI registry
    try {
      Registry registry = LocateRegistry.getRegistry("localhost");
      server = (IRemoteAuction) registry.lookup(serverName);
    }
    catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
      return;
    }
    while(true)
    {
      Account currentAccount = null;
      System.out.println("Welcome to the auction client!\n");
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
      InputProcessor.clearConsole();
      String accountName = "", email = "", password = "";
      switch (loginOption) {
        case 0: // exit
          input.close();
          return;
        case 1: // login
        System.out.println("Login:\n");
          System.out.println("Enter your email address: ");
          email = input.ReadNextLine();
          System.out.println("Enter your password: ");
          password = input.ReadNextLine();
          InputProcessor.clearConsole();
          System.out.print("Logging in... ");
          try {
            currentAccount = SignedMessage.validateMessage(server.login(email, password), publicKey, printHash);
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
          }
          break;
        case 2: // create account;
          System.out.println("Create account:\n");
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
            result = SignedMessage.validateMessage(server.addAccount(new Account(accountName, email, password)), publicKey, printHash);
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
            currentAccount = SignedMessage.validateMessage(server.login(email, password), publicKey, printHash);
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
          }
          break;
        default:
          break;
      }
      boolean sessionLoop = true;
      while(sessionLoop)
      {
        System.out.println("Greetings, " + currentAccount.getName());
        System.out.println("\nSelect the type of auction to participate in:");
        System.out.println("0. Exit");
        System.out.println("1. View your messages");
        System.out.println("2. Forward Auction");
        System.out.println("3. Reverse Auction");
        System.out.println("4. Double Auction");
        System.out.println("5: Log out");
        // use the InputProcessor class to get the next int from terminal input
        int option = input.ReadNextInt();
        //check if input is correct
        while(option < 0 || option > 5)
        {
            System.out.println("Error: input a number 0-5");
            option = input.ReadNextInt();
        }
        InputProcessor.clearConsole();
        //Switch case for different operations
        switch (option) {
          case 0: // Exit
            input.close();
            return;
          case 1:
            MessageClient.run(currentAccount, input, server, publicKey, printHash);
            break;
          case 2: // forward auction
            ForwardAuctionClient.run(currentAccount, input, server, publicKey, printHash);
            break;
          case 3: // reverse auction
            ReverseAuctionClient.run(currentAccount, input, server, publicKey, printHash);
            break;
          case 4: // double auction
            DoubleAuctionClient.run(currentAccount, input, server, publicKey, printHash);
            break;
          case 5: // log out
            System.out.println("Logging out...");
            System.out.println("");
            currentAccount = null;
            sessionLoop = false;
            break;
          default:
            break;
        }
      }
    }
  }
}