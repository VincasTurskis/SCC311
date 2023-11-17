// A class representing the double auction
// The orders are matched whenever a new order - buy or sell - is placed.
// This means that all sell orders in the system that aren't matched instantly are higher in price
// than all the standing buy orders.
public class DoubleAuctionClient {
    public static void run(Account currentAccount, InputProcessor input, IRemoteAuction server)
    {
        boolean actionLoop = true;
        while(actionLoop)
        {
            // Display selection menu in terminal
            System.out.println("Double Auction");
            System.out.println("\nSelect an action:");
            System.out.println("0. Back");
            System.out.println("1. Show all listings");
            System.out.println("2. Sell an item");
            System.out.println("3. Buy an item");
            System.out.println("4. Cancel an order");
            // use the InputProcessor class to get the next int from terminal input
            int option = input.ReadNextInt();
            //check if input is correct
            while(option < 0 || option > 4)
            {
                System.out.println("Error: input a number 0-4");
                option = input.ReadNextInt();
            }
            String itemName = "", result = "";
            float price = -1;
            switch(option)
            {
                case 0: // back
                    InputProcessor.clearConsole();
                    return;
                case 1:
                    try
                    {
                        System.out.println(server.DBrowseListings());
                    }
                    catch (Exception e) {
                        System.err.println("Exception:");
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    System.out.println("Creating a sell order...");
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
                        result = server.DPlaceSellOrder(itemName, price, currentAccount);
                        if(result.equals("Error: Listing does not exist"))
                        {
                            System.out.println("There are no listings for this item.");
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
                                    result = server.DCreateListing(itemName, description);
                                    if(result.equals("Created new listing for " + itemName))
                                    {
                                        result = server.DPlaceSellOrder(itemName, price, currentAccount);
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
                    System.out.println("Creating a new buy order...");
                    System.out.println("Input name of item: ");
                    itemName = input.ReadNextLine();
                    if(itemName == "")
                    {
                        System.out.println("Error: Name cannot be empty");
                        continue;
                    }
                    System.out.println("Specify the buying price: ");
                    price = input.ReadNextFloat();
                    try
                    {
                        result = server.DPlaceBuyOrder(itemName, price, currentAccount);
                        if(result.equals("Error: Listing does not exist"))
                        {
                            System.out.println("There are no listings for this item.");
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
                                    result = server.DCreateListing(itemName, description);
                                    if(result.equals("Created new listing for " + itemName))
                                    {
                                        result = server.DPlaceBuyOrder(itemName, price, currentAccount);
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
                case 4:
                    System.out.println("Removing order...");
                    System.out.println("Input name of item: ");
                    itemName = input.ReadNextLine();
                    if(itemName == "")
                    {
                        System.out.println("Error: Name cannot be empty");
                        continue;
                    }
                    System.out.println("Remove all your orders for this item? (Y/N)");
                    System.out.println("If \"N\" is selected, only the highest priced buy order/lowest priced sell order will be removed:");
                    String YN = input.ReadNextLine();
                    boolean all = false;
                    switch (YN) {
                        case "y":
                        case "Y":
                            all = true;
                            break;
                        case "n":
                        case "N":
                            all = false;
                            break;
                        default:
                            System.out.println("Option not recognized. Aborting...");
                            continue;
                    }
                    if(all)
                    {
                        System.out.println("Are you sure you want to remove all your orders for " + itemName + "? (Y/N)");
                    }
                    else
                    {
                        System.out.println("Are you sure you want to remove one of your orders for " + itemName + "? (Y/N)");   
                    }
                    String YN2 = input.ReadNextLine();
                    switch (YN2) {
                        case "y":
                        case "Y":
                            try {
                                System.out.println(server.DRemoveOrder(itemName, currentAccount, all));
                            } catch (Exception e) {
                                e.printStackTrace();
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
                default:
                    break;
            }
        }
    }
}
