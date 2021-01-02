package org.sandboxpowered.silica.network.login.serverbound;

import org.sandboxpowered.silica.network.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class EncryptionResponse implements Packet {
    public byte[] sharedSecret;
    public byte[] verifyToken;

    private static Cipher setupCipher(int i, String string, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(string);
        cipher.init(i, key);
        return cipher;
    }

    private static byte[] cipherData(int i, Key key, byte[] bs) throws EncryptionException {
        try {
            return setupCipher(i, key.getAlgorithm(), key).doFinal(bs);
        } catch (Exception var4) {
            throw new EncryptionException(var4);
        }
    }

    public static byte[] decryptUsingKey(Key key, byte[] bs) throws EncryptionException {
        return cipherData(2, key, bs);
    }

    @Override
    public void read(PacketByteBuf buf) {
        sharedSecret = buf.readByteArray();
        verifyToken = buf.readByteArray();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByteArray(sharedSecret);
        buf.writeByteArray(verifyToken);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {
        connection.handleEncryptionResponse(this);
    }

    public SecretKey getSecretKey(Key key) throws EncryptionException {
        byte[] cs = decryptUsingKey(key, this.sharedSecret);

        try {
            return new SecretKeySpec(cs, "AES");
        } catch (Exception var4) {
            throw new EncryptionException(var4);
        }
    }

    public byte[] getVerificationToken(Key key) throws EncryptionException {
        return decryptUsingKey(key, this.verifyToken);
    }

    public Cipher getCipher(int i, Key key) throws EncryptionException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(i, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        } catch (Exception var3) {
            throw new EncryptionException(var3);
        }
    }
}