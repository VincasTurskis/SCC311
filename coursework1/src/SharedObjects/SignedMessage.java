import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

import org.jgroups.Address;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

public class SignedMessage<T extends Serializable> implements Serializable {

    private T _message;
    private byte[] _signature;
    private byte[] _hash;
    public SignedMessage(T message, PrivateKey privateKey)
    {
        _message = message;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            _hash = digest.digest(objectToByteArray(_message));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(objectToByteArray(_message));
            _signature = signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This constructor is not really safe to have;
    //It wouldn't be there if I wasn't trying to test the fake server
    public SignedMessage(T message, byte[] signature)
    {
        _message = message;
        _signature = signature;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            _hash = digest.digest(objectToByteArray(_message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verify(PublicKey publicKey)
    {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(objectToByteArray(_message));
            return signature.verify(_signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public T getMessage()
    {
        return _message;
    }

    public byte[] getSignature()
    {
        return _signature;
    }

    public byte[] getHash()
    {
        return _hash;
    }
    @Override
    public String toString()
    {
        return _message.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == this) return true;
        if(!(o instanceof SignedMessage))
        {
            return false;
        }
        SignedMessage<?> sm = (SignedMessage<?>) o;
        if(sm._message.equals(_message) && Arrays.equals(sm._signature, _signature) && Arrays.equals(sm._hash, _hash))
        {
            return true;
        }
        else return false;
    }

    public static <O extends Serializable> byte[] objectToByteArray(O o)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);   
            out.writeObject(o);
            out.flush();
            byte[] bytes = bos.toByteArray();
            bos.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }  
    }

    public static <T extends Serializable> T validateMessage(SignedMessage<T> message, PublicKey publicKey, boolean printHash)
    {
        if(message == null || publicKey == null) return null;

        boolean badPaddingException = false;

        byte[] receivedHash, decipheredHash;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            decipheredHash = cipher.doFinal(message.getSignature());
            byte[] id = new byte[] { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };
            byte[] decipheredID = new byte[id.length];
            System.arraycopy(decipheredHash, 0, decipheredID, 0, id.length);
            if(!Arrays.equals(id, decipheredID))
            {
                System.out.println("Manual check fo padding does not match");
                throw new BadPaddingException();
            }
            receivedHash = new byte[decipheredHash.length - id.length];
            System.arraycopy(decipheredHash, id.length, receivedHash, 0, decipheredHash.length - id.length);
        } catch (BadPaddingException e){
            System.out.println("Cannot print decrypted hash - BadPaddingException caught");
            System.out.println("This likely means the keys do not match");
            badPaddingException = true;
            receivedHash = new byte[0];
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if(printHash && !badPaddingException)
        {
            System.out.println("    Message hash:\n" + InputProcessor.ByteArrayToString(message.getHash()));
            System.out.println("    Received hash:\n" +  InputProcessor.ByteArrayToString(receivedHash));
        }
        if(!message.verify(publicKey))
        {
            System.out.println("!!!");
            System.out.println("    SIGNATURE VERIFICATION FAILED!");
            System.out.println("!!!");
            if(!printHash && !badPaddingException)
            {
                System.out.println("    Message hash:\n" + InputProcessor.ByteArrayToString(message.getHash()));
                System.out.println("    Received hash:\n" +  InputProcessor.ByteArrayToString(receivedHash));   
            }
        }
        return message.getMessage();       
    }
    public static <T extends Serializable> SignedMessage<T> GetConsensusMessage(RspList<SignedMessage<T>> messages) throws Exception
    {
        if (messages == null) return null;
        if (messages.isEmpty() == true)
        {
            return null;
        }
        boolean hasException = false;
        Rsp<SignedMessage<T>> prevMessage = null;
        for(Entry<Address, Rsp<SignedMessage<T>>> messageEntry : messages.entrySet())
        {
            
            Rsp<SignedMessage<T>> message = messageEntry.getValue();
            if(message.hasException() == true)
            {
                hasException = true;
            }
            if(prevMessage != null)
            {                
                if(message.hasException() != prevMessage.hasException())
                {
                    throw new NoConsensusException();
                }
                if(hasException)
                {
                    if(!prevMessage.getException().equals(message.getException()))
                    {
                        throw new NoConsensusException();
                    }
                }
                else
                {
                    if(!prevMessage.getValue().equals(message.getValue()))
                    {
                        System.out.println(prevMessage.getValue());
                        System.out.println(message.getValue());
                        throw new NoConsensusException();
                    }
                }
            }
            prevMessage = message;
        }
        if(hasException)
        {
            Exception e = new Exception(prevMessage.getException());
            throw e;
        }
        else
        {
            return prevMessage.getValue();
        }
    }
}
