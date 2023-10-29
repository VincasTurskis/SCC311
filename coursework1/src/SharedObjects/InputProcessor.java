import java.util.Scanner;
import java.util.InputMismatchException;
public class InputProcessor {
    private Scanner s;
    public InputProcessor()
    {
        s = new Scanner(System.in);
    }
    public String ReadNextLine()
    {
        String result;
        try
        {
            result = s.nextLine();
            result = result.trim();
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
    public int ReadNextInt()
    {
        int result;
        try
        {
            result = s.nextInt();
            if(result < 0)
            {
                System.out.println("Input error: input a positive integer: ");
                return ReadNextInt();
            }
            return result;
        }
        catch(InputMismatchException e)
        {
            System.out.println("Input error: input a valid integer: ");
            s.nextLine();
            return ReadNextInt();
        }
        catch(Exception e)
        {
            System.out.println("Input Exception: ");
            e.printStackTrace();
            return -1;
        }
    }
    public float ReadNextFloat()
    {
        float result;
        try
        {
            result = s.nextFloat();
            if(result < 0)
            {
                System.out.println("Input error: input a positive number: ");
                return ReadNextFloat();
            }
            return result;
        }
        catch(InputMismatchException e)
        {
            System.out.println("Input error: input a valid number: ");
            s.nextLine();
            return ReadNextFloat();
        }
        catch(Exception e)
        {
            System.out.println("Input Exception: ");
            e.printStackTrace();
            return -1;
        }
    }
    public void close()
    {
        s.close();
    }
}
