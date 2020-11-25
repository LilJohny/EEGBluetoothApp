package com.example.bluetoothapp.transformations
import kotlinx.android.synthetic.main.fragment_eyes_state.*
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import classifier.FFT
import com.example.bluetoothapp.R
import com.example.bluetoothapp.SampleApplication
import kotlinx.android.synthetic.main.fragment_eyes_state.view.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class SleepStageClassifier(/*
    TODO: types to Objects ?
    TODO: double to integer ?
     */
    private val samplingFreq: Double,
    private val inputLength: Int,
    private val numSecondsInInput: Int
) {
    private val fftLength: Int = inputLength / numSecondsInInput
    private val fftObject: FFT
    private var basePSD //values of PSD for awake state
            : DoubleArray? = null
    private var minDensity //minimum threshold for density to be considered a peak
            = 0.0

    @Throws(Exception::class)
    fun calibrateBaseState(values: DoubleArray, seconds: Int) {
        /*
        set base PSD values for brain in some base state(awake)
        All comparisons will be made with this state
        @param values is input array of signal with properties given in constructor
        BUT with length = inputLength * (seconds / numSecondsInInput)
        @param seconds is num of seconds to calibrate base state (about 10 seconds of calm awake state)
        must be >= numSecondsInInput and be its multiply
        */
        if (seconds % numSecondsInInput > 0 || seconds < numSecondsInInput) {
            throw Exception("seconds must be >= numSecondsInInput and be its multiply")
        }
        if (values.size != inputLength * seconds / numSecondsInInput) {
            throw Exception("values.length != inputLength * (seconds / numSecondsInInput)")
        }
        val basePSDs = ArrayList<DoubleArray>()
        val numPSDs = values.size / inputLength
        for (split in 0 until numPSDs) {
            val valuesPart = DoubleArray(inputLength)
            if (inputLength >= 0) System.arraycopy(
                values,
                split * inputLength,
                valuesPart,
                0,
                inputLength
            )
            basePSDs.add(fftObject.computePSD(valuesPart))
        }
        val psdLen: Int = basePSDs[0].size
        basePSD = DoubleArray(psdLen)
        for (i in 0 until psdLen) {
            var curPSD = 0.0
            for (psd in basePSDs) {
                curPSD += psd[i]
            }
            //basePSD[i] is mean of measured psds for ith frequency
            basePSD!![i] = curPSD / basePSDs.size
        }
        minDensity = 0.0
        for (ampl in basePSD!!) {
            if (ampl > minDensity) {
                minDensity = ampl
            }
        }
    }

    /*
    We will possibly need to change input type to int[]
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Throws(Exception::class)
    fun getSleepStage(values: DoubleArray?): String {
        if (basePSD == null) {
            throw Exception("Perform calibration by calling calibrateBaseState")
        }
        val psd = fftObject.computePSD(values!!)
        val freqsWithHighDensities = ArrayList<Int>()
        for (i in psd.indices) {
            if (psd[i] > basePSD!![i] && psd[i] > minDensity) {
                println("Freq $i")
                println("BasePSD: " + basePSD!![i])
                println("GotPSD: " + psd[i])
                println("minDensity: $minDensity")
                freqsWithHighDensities.add(i)
            }
        }
        return classifySleepStage(freqsWithHighDensities)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun classifySleepStage(arrayOfFreqsWithPeaks: ArrayList<Int>): String {
        // array [1, 3, 10] corresponds to peaks at 1Hz, 3Hz, 10Hz
        //array is given sorted in ascending order
        arrayOfFreqsWithPeaks.removeIf { x: Int -> x >= 17 } //freqs >= 17 are useless now
        val states = arrayOf("awake", "REM", "nonREM")
        println(arrayOfFreqsWithPeaks)
        if (arrayOfFreqsWithPeaks.size == 0) {
            return states[0]
        }
        var state = "awake"
        for (maxf in arrayOfFreqsWithPeaks) {
            if (maxf in 1..6) { // indicator of REM - sleep
                state = states[1]
            }
            if (maxf in 13..16) {
                state = states[2]
            }
        }
        return state
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun classifyEyesStage(arrayOfFreqsWithPeaks: ArrayList<Int>): String {
        // array [1, 3, 10] corresponds to peaks at 1Hz, 3Hz, 10Hz
        //array is given sorted in ascending order
        arrayOfFreqsWithPeaks.removeIf { x: Int -> x >= 17 } //freqs >= 17 are useless now
        val states = arrayOf("open", "closed")
        println(arrayOfFreqsWithPeaks)
        if (arrayOfFreqsWithPeaks.size == 0) {
            return states[0]
        }
        var state = "awake"
        for (maxf in arrayOfFreqsWithPeaks) {
            if (maxf in 9..14) { // indicator closed eyes
                state = states[1]
                break
            }
        }
        SampleApplication.eyesOpen = state
        return state
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Throws(Exception::class)
    fun getEyesStage(values: DoubleArray?): String {
        if (basePSD == null) {
            throw Exception("Perform calibration by calling calibrateBaseState")
        }
        val psd = fftObject.computePSD(values!!)
        val freqsWithHighDensities = ArrayList<Int>()
        for (i in psd.indices) {
            if (psd[i] > basePSD!![i] && psd[i] > minDensity) {
                println("Freq $i")
                println("BasePSD: " + basePSD!![i])
                println("GotPSD: " + psd[i])
                println("minDensity: $minDensity")
                freqsWithHighDensities.add(i)
            }
        }
        return classifySleepStage(freqsWithHighDensities)
    }

    companion object {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            /*
        !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Assume input is 4 seconds of data taken at sampling Rate = 1024 Hz
        so inputLength = 4 * 1024
        fftLength is 1024, since signal is periodic
        fs = 1024 (let it be same as original sampling Rate)
         */
            val inputLength = 4 * 1024
            val fs = 1024
            val clf = SleepStageClassifier(fs.toDouble(), inputLength, 4)
            val calibrationSignal = DoubleArray(inputLength * 2) // 2 * 4 = 8 seconds of calibration
            for (i in 0 until inputLength * 2) {
                // 13 Hz is frequency of sin wave, 2Hz is frequency of cos wave,
                calibrationSignal[i] =
                    sin(2 * Math.PI * 13 * i / fs) + cos(2 * Math.PI * 2 * i / fs)
            }

            //here user waits for 8 seconds to calibrate software
            clf.calibrateBaseState(calibrationSignal, 8)
            val signal = DoubleArray(inputLength) //just inputLength for all other signals
            for (i in 0 until inputLength) {
                // higher amplitude at 13 Hz and a 5Hz, frequency 20 freq sin is redundant -> nonREM
                signal[i] =
                    2 * sin(2 * Math.PI * 13 * i / fs) + cos(2 * Math.PI * 20 * i / fs) + cos(
                        2 * Math.PI * 5 * i / fs
                    )
            }
            val state = clf.getSleepStage(signal)
            println(state)
        }
    }

    init {
        fftObject = FFT(inputLength, fftLength, samplingFreq)
    }
}