public class ReverseAuctionClient {
    public static void run(Account currentAccount, InputProcessor input, IRemoteAuction server)
    {
        boolean actionLoop = true;
        while(actionLoop)
        {
            // Display selection menu in terminal
            System.out.println("\nSelect an action:");
            System.out.println("0. Back");
            System.out.println("1. List all active auctions");
            System.out.println("2. Create a new category");
            System.out.println("3. Add a listing to an existing category");
            System.out.println("4. Bid on an item");
            // use the InputProcessor class to get the next int from terminal input
            int option = input.ReadNextInt();
            //check if input is correct
            while(option < 0 || option > 4)
            {
                System.out.println("Error: input a number 0-4");
                option = input.ReadNextInt();
            }
            switch (option) {
                case 0:
                    return;
                case 1:
                    
                    break;
                case 2:
                    
                    break;
                case 3:
                    
                    break;
                case 4:
                    
                    break;
                default:
                    break;
            }
        }
    }
}
