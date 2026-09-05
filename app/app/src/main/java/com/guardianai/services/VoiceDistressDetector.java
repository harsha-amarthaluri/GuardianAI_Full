package com.guardianai.services;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class VoiceDistressDetector {

    private static final String TAG = "VoiceDistressDetector";
    private static final int SAMPLE_RATE = 44100;
    private static final int DISTRESS_DECIBEL_THRESHOLD = 82; // dB

    public interface OnDistressDetectedListener {
        void onDistressDetected(double decibels, double confidence);
    }

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private OnDistressDetectedListener listener;

    public void startListening(Context context, OnDistressDetectedListener listener) {
        this.listener = listener;
        if (isRecording) return;

        try {
            int bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            audioRecord.startRecording();
            isRecording = true;

            recordingThread = new Thread(() -> {
                short[] buffer = new short[bufferSize];
                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, bufferSize);
                    if (read > 0) {
                        double sum = 0;
                        for (int i = 0; i < read; i++) {
                            sum += buffer[i] * buffer[i];
                        }
                        double amplitude = Math.sqrt(sum / read);
                        double decibels = 20 * Math.log10(amplitude / 32767.0 + 1e-5) + 90;

                        if (decibels >= DISTRESS_DECIBEL_THRESHOLD) {
                            Log.w(TAG, "⚠️ Voice distress detected: " + decibels + " dB");
                            if (this.listener != null) {
                                double confidence = Math.min(1.0, (decibels - 70) / 30.0);
                                this.listener.onDistressDetected(decibels, confidence);
                            }
                        }
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ignored) {}
                }
            });
            recordingThread.start();
        } catch (SecurityException e) {
            Log.e(TAG, "Microphone permission required for Voice Distress Detection", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize VoiceDistressDetector", e);
        }
    }

    public void stopListening() {
        isRecording = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception ignored) {}
            audioRecord = null;
        }
    }
}
