import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

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

    public static <T extends Serializable> T validateMessage(SignedMessage<T> message, PublicKey publicKey)
    {
        if(message == null || publicKey == null) return null;
        byte[] receivedHash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            receivedHash = digest.digest(objectToByteArray(message.getMessage()));
            System.out.println("    Message hash:\n" + InputProcessor.ByteArrayToString(message.getHash()));
            System.out.println("    Received hash:\n" +  InputProcessor.ByteArrayToString(receivedHash));
            if(!message.verify(publicKey))
            {
                System.out.println("!!!");
                System.out.println("    HASHES DO NOT MATCH!");
                System.out.println("!!!");
            }
            return message.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
