import java.util.Scanner;
/*
 * A utility class for reading and validating user input from the console
 */
public class InputProcessor {
    private Scanner s;
    public InputProcessor()
    {
        s = new Scanner(System.in);
    }
    /*
     * Gets the next string input from the console. Accounts for and trims white space
     * @return The input string
     */
    public String ReadNextLine()
    {
        String result;
        try
        {
            // Use scanner to read the next line, trim whitespace.
            result = s.nextLine();
            result = result.trim();
            // If the previous input was read with Scanner.nextInt() or nextFloat(), Scanner.nextLine() will return just the new line characters
            // Therefore, if the input was just whitespace, scan one more time and trim
            if(result.length() == 0)
            {
                result = s.nextLine();
                result = result.trim();
            }
            return result;
        }
        catch(Exception e)
        {
            System.out.println("Input Exception: ");
            e.printStackTrace();
            return "";
        }
    }
    /*
     * Gets the next integer input from the console. Keeps requesting input until a valid one is given.
     * Does not accept negative numbers.
     * @return The input integer. -1 if there was an unhandled exception.
     */
    public int ReadNextInt()
    {
        int result;
        while(true)
        {
            try
            {
                String input = s.nextLine();
                result = Integer.parseInt(input);
                if(result < 0)
                {
                    System.out.println("Input error: input a positive integer: ");
                }
                else return result;
            }
            catch(NumberFormatException e)
            {
                System.out.println("Input error: input a valid integer: ");
            }
            catch(Exception e)
            {
                System.out.println("Input Exception: ");
                e.printStackTrace();
                return -1;
            }
        }
    }
    /*
     * Gets the next float input from the console. Keeps requesting input until a valid one is given.
     * Does not accept negative numbers.
     * @return The input float. -1 if there was an unhandled exception.
     */
    public float ReadNextFloat()
    {
        float result;
        while(true)
        {
            try
            {
                String input = s.nextLine();
                result = Float.parseFloat(input);
                if(result < 0)
                {
                    System.out.println("Input error: input a positive number: ");
                }
                else return result;
            }
            catch(NumberFormatException e)
            {
                System.out.println("Input error: input a valid number: ");
                //Discard the input that was just provided (as it's invalid)
            }
            catch(Exception e)
            {
                System.out.println("Input Exception: ");
                e.printStackTrace();
                return -1;
            }
        }
    }
    /*
     * Closes the input Scanner.
     */
    public void close()
    {
        s.close();
    }
    public static void clearConsole()
    {
        System.out.print("\033\143");
    }
    public static int currencyToInt(float input)
    {
        input *= 100f;
        return (int) input;
    }

    public static String ByteArrayToString(byte[] hash)
    {
        if(hash == null) return "";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < hash.length; i++)
        {
            sb.append(String.format("%02X ", hash[i]));
        }
        return sb.toString();
    }
}
