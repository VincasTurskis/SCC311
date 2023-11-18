import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.Signature;

public class SignedMessage<T extends Serializable> {

    private T _message;
    private byte[] _signature;
    public SignedMessage(T message, PrivateKey privateKey)
    {
        _message = message;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(objectToByteArray(message));
            _signature = signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
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
}
