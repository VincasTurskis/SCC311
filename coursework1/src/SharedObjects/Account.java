import java.io.Serializable;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Account implements Serializable {
    private String _name;
    private String _email;

    //Hashed and salted with PBKDF2 (because that's the best password hash that's directly implemented in java)
    private byte[] _password;
    private byte[] _salt;

    public Account(String name, String email, String password) throws InvalidPasswordException
    {
        _name = name;
        _email = email;
        SecureRandom random = new SecureRandom();
        _salt = new byte[16];
        random.nextBytes(_salt);
        _password = hashPassword(password);
    }

    private byte[] hashPassword(String password) throws InvalidPasswordException
    {
        try
        {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), _salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        }
        catch (Exception e){
            throw new InvalidPasswordException();
        }
    }
    public boolean validatePassword(String password) throws InvalidPasswordException
    {
        byte[] toTest = hashPassword(password);
        if(_password == null) throw new InvalidPasswordException();
        if(_password.length != toTest.length) return false;
        return Arrays.equals(_password, toTest);
    }
    public String getName()
    {
        return _name;
    }
    public String getEmail()
    {
        return _email;
    }
    @Override
    public boolean equals(Object o)
    {
        if(o == this) return true;
        if(!(o instanceof Account)) return false;
        Account a = (Account) o;
        if(a._name.equals(_name) && a._email.equals(_email) && Arrays.equals(a._password, _password)) return true;
        else return false;
    }
}
