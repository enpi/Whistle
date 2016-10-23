package com.codamasters.whistle.thread;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Juan on 21/10/2016.
 */

public class RecorderThread {

    private AudioRecord audioRecord;
    private int channelConfiguration;
    private int audioEncoding;
    private int sampleRate;
    private int frameByteSize;
    byte[] buffer;

    public RecorderThread() {
        sampleRate = 44100;
        frameByteSize = 1024 * 2;

        channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        int recBufSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                sampleRate, channelConfiguration, audioEncoding, recBufSize);
        buffer = new byte[frameByteSize];
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public boolean isRecording() {
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            return true;
        }

        return false;
    }

    public void startRecording() {
        try {
            audioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            audioRecord.stop();
            audioRecord.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getFrameBytes() {
        audioRecord.read(buffer, 0, frameByteSize);

        int totalAbsValue = 0;
        short sample = 0;
        float averageAbsValue = 0.0f;

        for (int i = 0; i < frameByteSize; i += 2) {
            sample = (short) ((buffer[i]) | buffer[i + 1] << 8);
            totalAbsValue += Math.abs(sample);
        }
        averageAbsValue = totalAbsValue / frameByteSize / 2;

        if (averageAbsValue < 30) {
            return null;
        }

        return buffer;
    }

}
