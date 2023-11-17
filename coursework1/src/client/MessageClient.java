import java.util.List;

public class MessageClient {
    public static void run(Account currentAccount, InputProcessor input, IRemoteAuction server)
    {
        while(true)
        {
            try {
                List<String> messages = server.getMessages(currentAccount);
                if(messages == null)
                {
                    System.out.println("Something has gone wrong. Please log out and try again.");
                    return;
                }
                else if(messages.isEmpty())
                {
                    System.out.println("There are no new messages");
                }
                else
                {
                    System.out.println("Your messages:\n");
                    for(String msg : messages)
                    {
                        System.out.println(msg);
                        System.out.println("");
                    }
                }
                System.out.println("Choose an option: ");
                System.out.println("0. Back");
                System.out.println("1. Refresh");
                System.out.println("2. Delete all messages");
                // use the InputProcessor class to get the next int from terminal input
                int option = input.ReadNextInt();
                //check if input is correct
                while(option < 0 || option > 2)
                {
                    System.out.println("Error: input a number 0-2");
                    option = input.ReadNextInt();
                }
                InputProcessor.clearConsole();
                //Switch case for different operations
                switch (option) {
                    case 0:
                        return;
                    case 1:
                        continue;
                    case 2:
                        System.out.println("Are you sure you want to delete all messages? (Y/N)");
                        String YN = input.ReadNextLine();
                        switch (YN) {
                            case "y":
                            case "Y":
                                boolean result = server.deleteMessages(currentAccount);
                                if(!result)
                                {
                                    System.out.println("Could not delete messages. Please log out and try again.");
                                    return;
                                }
                                System.out.println("Messages deleted");
                                return;
                            case "n":
                            case "N":
                                InputProcessor.clearConsole();
                                continue;
                            default:
                                continue;
                        }
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



        }
    }
}
