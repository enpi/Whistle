package com.codamasters.whistle.thread;

/**
 * Created by Juan on 21/10/2016.
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;

import java.util.LinkedList;

public class DetectorThread extends Thread {

    private RecorderThread recorder;
    private WaveHeader waveHeader;
    private WhistleApi whistleApi;
    private Thread _thread;

    private LinkedList<Boolean> whistleResultList = new LinkedList<Boolean>();
    private int numWhistles;
    private int totalWhistlesDetected = 0;
    private int whistleCheckLength = 3;
    private int whistlePassScore = 3;

    private OnWhistleListener onWhistleListener;

    public DetectorThread(RecorderThread recorder) {
        this.recorder = recorder;
        AudioRecord audioRecord = recorder.getAudioRecord();

        int bitsPerSample = 0;
        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bitsPerSample = 16;
        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
            bitsPerSample = 8;
        }

        int channel = 0;

        if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
            channel = 1;
        }

        waveHeader = new WaveHeader();
        waveHeader.setChannels(channel);
        waveHeader.setBitsPerSample(bitsPerSample);
        waveHeader.setSampleRate(audioRecord.getSampleRate());
        whistleApi = new WhistleApi(waveHeader);
    }

    private void initBuffer() {
        numWhistles = 0;
        whistleResultList.clear();

        for (int i = 0; i < whistleCheckLength; i++) {
            whistleResultList.add(false);
        }
    }

    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void stopDetection() {
        _thread = null;
    }

    @Override
    public void run() {

        try {
            byte[] buffer;
            initBuffer();

            Thread thisThread = Thread.currentThread();
            while (_thread == thisThread) {

                buffer = recorder.getFrameBytes();

                if (buffer != null) {

                    try {
                        boolean isWhistle = whistleApi.isWhistle(buffer);

                        if (whistleResultList.getFirst()) {
                            numWhistles--;
                        }

                        whistleResultList.removeFirst();
                        whistleResultList.add(isWhistle);

                        if (isWhistle) {
                            numWhistles++;
                        }

                        if (numWhistles >= whistlePassScore) {
                            initBuffer();
                            totalWhistlesDetected++;

                            if (onWhistleListener != null) {
                                onWhistleListener.onWhistle();
                            }
                        }
                    } catch (Exception e) {
                    }
                } else {
                    if (whistleResultList.getFirst()) {
                        numWhistles--;
                    }
                    whistleResultList.removeFirst();
                    whistleResultList.add(false);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnWhistleListener(OnWhistleListener onWhistleListener) {
        this.onWhistleListener = onWhistleListener;
    }

    public interface OnWhistleListener {
        void onWhistle();
    }

    public int getTotalWhistlesDetected() {
        return totalWhistlesDetected;
    }
}