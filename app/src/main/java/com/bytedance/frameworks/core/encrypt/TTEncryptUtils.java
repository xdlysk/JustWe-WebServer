package com.bytedance.frameworks.core.encrypt;

/**
 * Created by Jeqee on 2017/4/18.
 */

public class TTEncryptUtils {
    static
{
    try
    {
        System.loadLibrary("ttEncrypt");
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
        localUnsatisfiedLinkError.printStackTrace();
    }
}

    public static byte[] e(byte[] paramArrayOfByte, int paramInt)
    {
        try
        {
            paramArrayOfByte = ttEncrypt(paramArrayOfByte, paramInt);
            return paramArrayOfByte;
        }
        catch (Throwable paramArrayOfByte1) {
            paramArrayOfByte1.printStackTrace();
        }
        return null;
    }

    public static byte[] d(byte[] paramArrayOfByte, int paramInt)
    {
        try
        {
            paramArrayOfByte = ttDecrypt(paramArrayOfByte, paramInt);
            return paramArrayOfByte;
        }
        catch (Throwable paramArrayOfByte1) {}
        return null;
    }

    private static native byte[] ttDecrypt(byte[] paramArrayOfByte, int paramInt);

    private static native byte[] ttEncrypt(byte[] paramArrayOfByte, int paramInt);
}