package com.hakura.GlassRevive.Glass;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import com.google.protobuf.MessageLite;

public class GlassProtocol {
    private static int sizeOfSize(int paramInt) {
        if ((paramInt & 0xFFFFFF80) == 0) {
            return 1;
        }
        if ((paramInt & 0xFFFFC000) == 0) {
            return 2;
        }
        if ((0xFFE00000 & paramInt) == 0) {
            return 3;
        }
        if ((0xF0000000 & paramInt) == 0) {
            return 4;
        }
        return 5;
    }

    private static int getSizeToRead(int paramInt, InputStream paramInputStream) throws IOException {
        if ((paramInt & 0x80) == 0) {
            return paramInt;
        }
        paramInt &= 0x7F;
        for (int i = 7; i < 64; i += 7) {
            int b = paramInputStream.read();

            if (b == -1) {
                throw new IOException();
            }
            paramInt |= ((b & 0x7F) << i);
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return paramInt;
    }

    public static MessageLite readMessage(MessageLite paramMessageNano, InputStream paramInputStream)
            throws IOException {
        int firstByte = paramInputStream.read();
        if (firstByte == -1) {
            return null;
        }
        int sizeToRead = getSizeToRead(firstByte, paramInputStream);
        byte[] arrayOfByte = new byte[sizeToRead];
        int readSoFar = 0;
        while (readSoFar < sizeToRead) {
            int m = paramInputStream.read(arrayOfByte, readSoFar, sizeToRead - readSoFar);
            if (m == -1) {
                throw new IOException();
            }
            readSoFar += m;
        }
        return paramMessageNano.toBuilder().mergeFrom(arrayOfByte).build();
    }

    private static void writeSize(int paramInt, OutputStream paramOutputStream) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                paramOutputStream.write(paramInt);
                return;
            }
            paramOutputStream.write(0x80 | paramInt & 0x7F);
            paramInt >>>= 7;
        }
    }

    public static void writeMessage(MessageLite paramMessageNano, OutputStream paramOutputStream) throws IOException {
        int cachedSize = paramMessageNano.getSerializedSize();
        BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(paramOutputStream,
                getSizeToWrite(cachedSize + sizeOfSize(cachedSize)));
        writeSize(cachedSize, localBufferedOutputStream);
        Log.d("GlassRevive|Write",paramMessageNano.toString());
        localBufferedOutputStream.write(paramMessageNano.toByteArray());
        localBufferedOutputStream.flush();
    }

    private static int getSizeToWrite(int paramInt) {
        if (paramInt > 4096) {
            paramInt = 4096;
        }
        return paramInt;
    }
}