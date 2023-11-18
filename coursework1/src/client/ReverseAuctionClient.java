import java.security.PublicKey;

public class ReverseAuctionClient {
    public static void run(Account currentAccount, InputProcessor input, IRemoteAuction server, PublicKey publicKey)
    {
        boolean actionLoop = true;
        while(actionLoop)
        {
            // Display selection menu in terminal
            System.out.println("Reverse Auction");
            System.out.println("\nSelect an action:");
            System.out.println("0. Back");
            System.out.println("1. List all active auctions");
            System.out.println("2. Sell an item");
            System.out.println("3. Buy an item");
            // use the InputProcessor class to get the next int from terminal input
            int option = input.ReadNextInt();
            //check if input is correct
            while(option < 0 || option > 3)
            {
                System.out.println("Error: input a number 0-3");
                option = input.ReadNextInt();
            }
            String itemName = "", result = "";
            float price = -1;
            switch (option) {
                case 0: // back
                    InputProcessor.clearConsole();
                    return;
                case 1: // browse listings
                    try
                    {
                        for(String s : SignedMessage.validateMessage(server.RBrowseListings(), publicKey))
                        {
                            System.out.println(s);
                        }
                        System.out.println("");
                    }
                    catch (Exception e) {
                        System.err.println("Exception:");
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    System.out.println("Listing an item for sale...");
                    System.out.println("Input name of item: ");
                    itemName = input.ReadNextLine();
                    if(itemName == "")
                    {
                        System.out.println("Error: Name cannot be empty");
                        continue;
                    }
                    System.out.println("Specify the selling price: ");
                    price = input.ReadNextFloat();
                    try
                    {
                        result = SignedMessage.validateMessage(server.RAddEntryToListing(itemName, InputProcessor.currencyToInt(price), currentAccount), publicKey);
                        if(result.equals("Error: Listing does not exist"))
                        {
                            System.out.println("There are no other listings for this item.");
                            System.out.println("Create a new listing? (Y/N)");
                            String YN = input.ReadNextLine();
                            switch (YN) {
                                case "y":
                                case "Y":
                                    System.out.println("Write a short description of the item: ");
                                    String description = input.ReadNextLine();
                                    if(description == "")
                                    {
                                        System.out.println("Error: description cannot be empty");
                                        continue;
                                    }
                                    System.out.println("Creating a new listing...\n");
                                    result = SignedMessage.validateMessage(server.RCreateListing(itemName, description), publicKey);
                                    if(result.equals("Created new listing for " + itemName))
                                    {
                                        result = SignedMessage.validateMessage(server.RAddEntryToListing(itemName, InputProcessor.currencyToInt(price), currentAccount), publicKey);
                                        System.out.println(result);
                                    }
                                    else
                                    {
                                        System.out.println("Error: " + result);
                                        continue;
                                    }
                                    break;
                                case "n":
                                case "N":
                                    System.out.println("Aborting...");
                                    continue;
                                default:
                                    System.out.println("Option not recognized. Aborting...");
                                    continue;
                            }
                        }
                        else
                        {
                            System.out.println(result);
                            continue;
                        }
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }
                    break;
                case 3:
                    System.err.println("Purchasing an item");
                    System.out.println("Input name of item: ");
                    itemName = input.ReadNextLine();
                    if(itemName == "")
                    {
                        System.out.println("Error: Name cannot be empty");
                        continue;
                    }
                    try {
                        String toPrint = SignedMessage.validateMessage(server.RGetSpec(itemName), publicKey);
                        System.out.println(toPrint);
                        if(SignedMessage.validateMessage(server.RExists(itemName), publicKey))
                        {
                            System.out.println("Purchase at stated price? (Y/N)");
                            String YN = input.ReadNextLine();
                            switch (YN) {
                                case "y":
                                case "Y":
                                    toPrint = SignedMessage.validateMessage(server.RBuyItem(itemName, currentAccount), publicKey);
                                    System.out.println(toPrint);
                                    break;
                                case "n":
                                case "N":
                                    continue;
                                default:
                                    System.out.println("Option not recognized. Aborting...");
                                    continue;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }
}
