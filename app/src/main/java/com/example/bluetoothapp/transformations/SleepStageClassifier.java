package com.example.bluetoothapp.transformations;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

import classifier.FFT;

public class SleepStageClassifier {

    /*
    TODO: types to Objects ?
    TODO: double to integer ?
     */
    private double samplingFreq;
    private int inputLength;
    private int numSecondsInInput;
    private int fftLength;
    private FFT fftObject;
    private double[] basePSD;//values of PSD for awake state
    private double minDensity;//minimum threshold for density to be considered a peak


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void main(String[] args) throws Exception {
        /*
        !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Assume input is 4 seconds of data taken at sampling Rate = 1024 Hz
        so inputLength = 4 * 1024
        fftLength is 1024, since signal is periodic
        fs = 1024 (let it be same as original sampling Rate)
         */

        int inputLength = 4 * 1024;
        int fs = 1024;

        SleepStageClassifier clf = new SleepStageClassifier(fs, inputLength, 4);

        double[] calibrationSignal = new double[inputLength * 2];// 2 * 4 = 8 seconds of calibration
        for (int i = 0; i < inputLength * 2 ; i++) {
            // 13 Hz is frequency of sin wave, 2Hz is frequency of cos wave,
            calibrationSignal[i] = Math.sin(2 * Math.PI * 13 * i/fs) + Math.cos(2 * Math.PI * 2 * i/fs);
        }

        //here user waits for 8 seconds to calibrate software
        clf.calibrateBaseState(calibrationSignal, 8);

        double[] signal = new double[inputLength];//just inputLength for all other signals
        for (int i = 0; i < inputLength; i++) {
            // higher amplitude at 13 Hz and a 5Hz, frequency 20 freq sin is redundant -> nonREM
            signal[i] = 2 *Math.sin(2 * Math.PI * 13 * i/fs) + Math.cos(2 * Math.PI * 20 * i/fs) + Math.cos(2 * Math.PI * 5 * i/fs);
        }
        String state = clf.getSleepStage(signal);
        System.out.println(state);

    }

    public SleepStageClassifier(double samplingFreqFromSource, int inputLength, int numSecondsInInput ){
        this.samplingFreq = samplingFreqFromSource;
        this.inputLength = inputLength;
        this.numSecondsInInput = numSecondsInInput;
        this.fftLength = inputLength / numSecondsInInput;
        this.fftObject = new FFT(inputLength, fftLength, samplingFreq);
    }


    public void calibrateBaseState(double[] values, int seconds) throws Exception {
        /*
        set base PSD values for brain in some base state(awake)
        All comparisons will be made with this state
        @param values is input array of signal with properties given in constructor
        BUT with length = inputLength * (seconds / numSecondsInInput)
        @param seconds is num of seconds to calibrate base state (about 10 seconds of calm awake state)
        must be >= numSecondsInInput and be its multiply
        */
        if(seconds % numSecondsInInput > 0 || seconds < numSecondsInInput){
            throw new Exception("seconds must be >= numSecondsInInput and be its multiply");
        }

        if(values.length != inputLength * seconds / numSecondsInInput){
            throw new Exception("values.length != inputLength * (seconds / numSecondsInInput)");
        }

        ArrayList<double[]> basePSDs = new ArrayList<>();

        int numPSDs = values.length/inputLength;
        for(int split = 0; split < numPSDs; split++){
            double[] valuesPart = new double[inputLength];
            if (inputLength >= 0)
                System.arraycopy(values, split * inputLength, valuesPart, 0, inputLength);
            basePSDs.add(fftObject.computePSD(valuesPart));
        }

        int psdLen = basePSDs.get(0).length;
        basePSD = new double[psdLen];

        for(int i = 0; i < psdLen; i++){
            double curPSD = 0;
            for(double[] psd : basePSDs){
                curPSD += psd[i];
            }
            //basePSD[i] is mean of measured psds for ith frequency
            basePSD[i] = curPSD / basePSDs.size();
        }

        minDensity = 0;
        for(double ampl : basePSD){
            if (ampl > minDensity){
                minDensity = ampl;
            }
        }

    }

    /*
    We will possibly need to change input type to int[]
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getSleepStage(double[] values) throws Exception {

        if (basePSD == null){
            throw new Exception("Perform calibration by calling calibrateBaseState");
        }
        double[] psd = fftObject.computePSD(values);

        ArrayList<Integer> freqsWithHighDensities = new ArrayList<Integer>();
        for(int i = 0; i < psd.length; i++){
            if (psd[i] > basePSD[i] && psd[i] > minDensity){
                System.out.println("Freq " + i);
                System.out.println("BasePSD: " + basePSD[i]);
                System.out.println("GotPSD: " + psd[i]);
                System.out.println("minDensity: " +minDensity);
                freqsWithHighDensities.add(i);
            }


        }

        return classifySleepStage(freqsWithHighDensities);


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String classifySleepStage(ArrayList<Integer> arrayOfFreqsWithPeaks){
        // array [1, 3, 10] corresponds to peaks at 1Hz, 3Hz, 10Hz
        //array is given sorted in ascending order
        arrayOfFreqsWithPeaks.removeIf(x -> (x >= 17));//freqs >= 17 are useless now

        String[] states = {"awake", "REM", "nonREM"};
        System.out.println(arrayOfFreqsWithPeaks);
        if(arrayOfFreqsWithPeaks.size() == 0){
            return states[0];
        }

        String state = "awake";
        for(int maxf : arrayOfFreqsWithPeaks){
            if(0 < maxf && maxf < 7){ // indicator of REM - sleep
                state = states[1];
            }
            if(12 < maxf && maxf < 17){
                state = states[2];
            }
        }

        return state;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String classifyEyesStage(ArrayList<Integer> arrayOfFreqsWithPeaks){
        // array [1, 3, 10] corresponds to peaks at 1Hz, 3Hz, 10Hz
        //array is given sorted in ascending order
        arrayOfFreqsWithPeaks.removeIf(x -> (x >= 17));//freqs >= 17 are useless now

        String[] states = {"open", "closed"};
        System.out.println(arrayOfFreqsWithPeaks);
        if(arrayOfFreqsWithPeaks.size() == 0){
            return states[0];
        }

        String state = "awake";
        for(int maxf : arrayOfFreqsWithPeaks){
            if(8 < maxf && maxf < 15){ // indicator closed eyes
                state = states[1];
                break;
            }

        }

        return state;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getEyesStage(double[] values) throws Exception {

        if (basePSD == null){
            throw new Exception("Perform calibration by calling calibrateBaseState");
        }
        double[] psd = fftObject.computePSD(values);

        ArrayList<Integer> freqsWithHighDensities = new ArrayList<Integer>();
        for(int i = 0; i < psd.length; i++){
            if (psd[i] > basePSD[i] && psd[i] > minDensity){
                System.out.println("Freq " + i);
                System.out.println("BasePSD: " + basePSD[i]);
                System.out.println("GotPSD: " + psd[i]);
                System.out.println("minDensity: " +minDensity);
                freqsWithHighDensities.add(i);
            }


        }

        return classifySleepStage(freqsWithHighDensities);


    }
}

