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
            // use the InputProcessor class to get the next int from terminal input
            int option = input.ReadNextInt();
            //check if input is correct
            while(option < 0 || option > 3)
            {
                System.out.println("Error: input a number 0-3");
                option = input.ReadNextInt();
            }
        }
    }
}
