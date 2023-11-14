public class ForwardAuctionClient {
    public static void run(Account currentAccount, InputProcessor input, IRemoteAuction server)
    {
        boolean actionLoop = true;
        while(actionLoop)
        {
        // Display selection menu in terminal
        System.out.println("\nSelect an action:");
        System.out.println("0. Back");
        System.out.println("1. List all active auctions");
        System.out.println("2. Create a new listing");
        System.out.println("3. Close an auction");
        System.out.println("4. Bid on a listing");
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
            case 0: // Back
                return;
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
                    int id = server.createAuction(itemName, description, startingPrice, reservePrice, currentAccount);
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
                    toPrint = server.closeAuction(id, currentAccount);
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
                System.out.println("Creating a new bid..");
                int newId;
                float newPrice;
                // Block of code for taking the required data for a new bid as input from user
                System.out.println("Input ID of item: ");
                newId = input.ReadNextInt();
                System.out.println("Specify the amount to bid: ");
                newPrice = input.ReadNextFloat();
                System.out.println("Creating the bid...");
                try
                {
                    // Try to place the bid based on the supplied parameters;
                    // Print the return string of the server function to console
                    String newBidMessage = server.placeBid(newId, newPrice, currentAccount);
                    System.out.println(newBidMessage);
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
    }
}
