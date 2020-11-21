package classifier

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.min

/******************************************************************************
 *  Compilation:  javac FFT.java
 *  Execution:    java FFT n
 *  Dependencies: Complex.java
 *
 *  Compute the FFT and inverse FFT of a length n complex sequence
 *  using the radix 2 Cooley-Tukey algorithm.

 *  Bare bones implementation that runs in O(n log n) time. Our goal
 *  is to optimize the clarity of the code, rather than performance.
 *
 *  Limitations
 *  -----------
 *   -  assumes n is a power of 2
 *
 *   -  not the most memory efficient algorithm (because it uses
 *      an object type for representing complex numbers and because
 *      it re-allocates memory for the subarray, instead of doing
 *      in-place or reusing a single temporary array)
 *
 *  For an in-place radix 2 Cooley-Tukey FFT, see
 *  https://introcs.cs.princeton.edu/java/97data/InplaceFFT.java.html
 *
 ******************************************************************************/
// For log10
// For printing arrays when debugging
/*
This class uses the DoubleFFT_1D object from JTransforms to
compute the DFT of an array of fixed size.
Args:
inputLength (int) : length of the input signal (number of samples)
fftLength (int) : FFT length; if different than inputLength, the input
will be zero-padded (larger) or truncated (smaller)
samplingRate (double) : sampling frequency of the input signal, in Hz.
used to define frequency bins
The use of an encapsulated class (rather than using JTransforms
directly in the Android graph code) is meant to simplify
interpretation of the code and allow an eventual custom
FFT implementation.
*/
class FFT(// ------------------------------------------------------------------------
    // Variables
    private val inputLength: Int, private val fftLength: Int, private val samplingFrequency: Double
) {
    private var nbFFTPoints = 0
    private var even = false
    private var zeroPad = false
    private val real: DoubleArray
    private val imag: DoubleArray
    private val logpower: DoubleArray
    private var Y: DoubleArray
    val freqBins: DoubleArray
    private val hammingWin: DoubleArray
    private val complexMagnitude: DoubleArray
    private val fft_1D: DoubleFFT_1D

    // ------------------------------------------------------------------------
    // Methods
    fun computePSD(x: DoubleArray): DoubleArray {
        // Compute PSD of x
        // TODO: Improve efficiency by merging for loops
        require(x.size == inputLength) { "Input has " + x.size + " elements instead of " + inputLength + "." }
        if (zeroPad) {
            Y = DoubleArray(fftLength) // Re-initialize to have zeros at the end
        }

        // Compute mean of the window
        var winMean = 0.0
        for (i in 0 until inputLength) {
            winMean += x[i]
        }
        winMean /= inputLength.toDouble()

        // De-mean and apply Hamming window
        for (i in 0 until min(inputLength, fftLength)) {
            Y[i] = hammingWin[i] * (x[i] - winMean)
        }

        // Compute DFT
        fft_1D.realForward(Y)

        // Get real and imaginary parts
        for (i in 0 until nbFFTPoints - 1) {
            real[i] = Y[2 * i]
            imag[i] = Y[2 * i + 1]
        }
        imag[0] = 0.0

        // Get first and/or last points depending on length of FFT (Specific to JTransforms library)
        if (even) {
            real[nbFFTPoints - 1] = Y[1]
        } else {
            imag[nbFFTPoints - 1] = Y[1]
            real[nbFFTPoints - 1] = Y[fftLength - 1]
        }

        // Compute complex number?
        for (i in 0 until nbFFTPoints) {
            complexMagnitude[i] = real[i] * real[i] + imag[i] * imag[i] // log squared
            // complex magnitude
        }
        return complexMagnitude
    }

    fun computeLogPSD(x: DoubleArray): DoubleArray {
        // Compute log10(PSD) of x
        // TODO: Improve efficiency by merging for loops

        //Log.w("computingPSD", "received " + Arrays.toString(x));
        require(x.size == inputLength) { "Input has " + x.size + " elements instead of " + inputLength + "." }
        if (zeroPad) {
            Y = DoubleArray(fftLength) // Re-initialize to have zeros at the end
        }

        // Compute mean of the window
        var winMean = 0.0
        for (i in 0 until inputLength) {
            winMean += x[i]
        }
        winMean /= inputLength.toDouble()

        // De-mean and apply Hamming window
        for (i in 0 until min(inputLength, fftLength)) {
            Y[i] = hammingWin[i] * (x[i] - winMean)
        }

        // Compute DFT
        fft_1D.realForward(Y)

        // Get real and imaginary parts
        for (i in 0 until nbFFTPoints - 1) {
            real[i] = Y[2 * i]
            imag[i] = Y[2 * i + 1]
        }
        imag[0] = 0.0

        // Get first and/or last points depending on length of FFT (Specific to JTransforms library)
        if (even) {
            real[nbFFTPoints - 1] = Y[1]
        } else {
            imag[nbFFTPoints - 1] = Y[1]
            real[nbFFTPoints - 1] = Y[fftLength - 1]
        }

        // Compute log-power
        for (i in 0 until nbFFTPoints) {
            logpower[i] = log10(real[i] * real[i] + imag[i] * imag[i]) // log squared
            // complex magnitude
        }
        return logpower
    }

    private fun hamming(L: Int): DoubleArray {
        // Compute Hamming window coefficients.
        //
        // See [http://www.mathworks.com/help/signal/ref/hamming.html]
        val w = DoubleArray(L)
        for (n in 0 until L) {
            w[n] = 0.54 - 0.46 * cos(2 * Math.PI * n / (L - 1))
        }
        return w
    }

    companion object {
        // Example main for testing and using this FFT class
        @JvmStatic
        fun main(args: Array<String>) {
            val inputLength = 4096 //16
            val fftLength = 128 //32
            val fs = 4096.0 //16

            // Instantiate FFT object
            val fft = FFT(inputLength, fftLength, fs)

            // Create fake time series of size `inputLength`
            val values = DoubleArray(inputLength)
            for (i in 0 until inputLength) {
                values[i] = cos(i * 60 * 0.01745)
            }
            println(values.contentToString())

            // Compute log PSD
            val logPower = fft.computeLogPSD(values) //LogPSD

            // Print values
            println(logPower.contentToString())
            println(fft.freqBins.contentToString())
            println(fft.freqBins.contentToString().length)
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    init {

        // Parameters

        // Find out if zero-padding or truncating is necessary
        if (fftLength > inputLength) { // zero-padding
            zeroPad = true
        }

        // Compute the number of points in the FFT
        if (fftLength % 2 == 0) {
            nbFFTPoints = fftLength / 2
            even = true
        } else {
            nbFFTPoints = (fftLength / 2) + 1
            even = false
        }

        // Initialize arrays to hold internal values
        Y = DoubleArray(fftLength)
        real = DoubleArray(nbFFTPoints)
        imag = DoubleArray(nbFFTPoints)
        logpower = DoubleArray(nbFFTPoints)
        complexMagnitude = DoubleArray(nbFFTPoints)


        // Initialize FFT transform
        fft_1D = DoubleFFT_1D(fftLength.toLong())

        // Define frequency bins
        freqBins = DoubleArray(nbFFTPoints)
        for (i in 0 until nbFFTPoints) {
            freqBins[i] = samplingFrequency * i / fftLength
        }

        // Initialize Hamming window
        hammingWin = hamming(inputLength)
    }
}