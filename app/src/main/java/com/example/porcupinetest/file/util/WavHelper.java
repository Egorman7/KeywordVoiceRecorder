package com.example.porcupinetest.file.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavHelper {
    public static byte[] completeHeader(byte[] littleBytes){
        return new byte[]{
                'R', 'I', 'F', 'F',
                0, 0, 0, 0,
                'W', 'A', 'V', 'E',
                'f', 'm', 't', ' ',
                16, 0, 1, 0,
                1, 0,
                littleBytes[0], littleBytes[1],
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5],
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9],
                littleBytes[10], littleBytes[11],
                littleBytes[12], littleBytes[13],
                'd', 'a', 't', 'a',
                0, 0, 0, 0,
        };
    }

    public static void rawToWave(final int sampleRate, final File rawFile, final File waveFile) throws IOException {
        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try{
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if(input != null){
                input.close();
            }
        }
        DataOutputStream output = null;
        try{
            output = new DataOutputStream(new FileOutputStream(waveFile));
            output.writeByte('R');
            output.writeByte('I');
            output.writeByte('F');
            output.writeByte('F');
            writeInt(output, 36+rawData.length);
            output.writeByte('W');
            output.writeByte('A');
            output.writeByte('V');
            output.writeByte('E');
            output.writeByte('f');
            output.writeByte('m');
            output.writeByte('t');
            output.writeByte(' ');
            writeInt(output, 16);
            writeShort(output, (short)1);
            writeShort(output, (short)1);
            writeInt(output, sampleRate);
            writeInt(output,sampleRate*2);
            writeShort(output, (short)2);
            writeShort(output, (short)16);
            output.writeByte('d');
            output.writeByte('a');
            output.writeByte('t');
            output.writeByte('a');
            writeInt(output, rawData.length);

            short[] shorts = new short[rawData.length/2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length*2);
            for(short s : shorts){
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if(output != null){
                output.flush();
                output.close();
            }
        }
    }

    private static void writeInt(DataOutputStream stream, int value) throws IOException{
        stream.writeByte(value);
        stream.writeByte(value >> 8);
        stream.writeByte(value >> 16);
        stream.writeByte(value >> 24);
    }

    private static void writeShort(DataOutputStream stream, short value) throws IOException{
        stream.writeByte(value);
        stream.writeByte(value >> 8);
    }
}
