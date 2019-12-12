package com.example.bluetoothapp.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothapp.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.*


class PlotActivity : AppCompatActivity() {
    companion object {
        fun newInstance(context: Context) = Intent(context, PlotActivity::class.java)
        private val RANDOM = Random()
    }
    private var series1: LineGraphSeries<DataPoint>? = null
    private var lastX : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plot)
        val x: Double
        var y: Double
        x = 0.0
        val graph = findViewById<View>(R.id.graph) as GraphView
        val graph2 = findViewById<View>(R.id.graph2) as GraphView
        series1 = LineGraphSeries()
        //        int numDataPoints = 500;
//        for (int i=0; i<numDataPoints; i++) {
//            x = x + 0.1;
//            y = x + 10;
//            series1.appendData(new DataPoint(x,y), true, 100);
//        }
        series1!!.color = Color.RED
        series1!!.backgroundColor = Color.YELLOW
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(4.0)
        graph.viewport.setMaxX(80.0)
        // enable scaling and scrolling
        graph.viewport.isScalable = true
        graph.title = "Data from EEG"
        graph.addSeries(series1)
        graph2.addSeries(series1)
        val viewport = graph.viewport
    }

    override fun onResume() {
        super.onResume()
        Thread(Runnable {
            for (i in 0..9999) {
                runOnUiThread { addEntry() }
                try {
                    Thread.sleep(250)
                } catch (e: InterruptedException) { //                        e.printStackTrace();
                }
            }
        }).start()
    }

    private fun addEntry() {
        series1!!.appendData(
            DataPoint(
                lastX++,
                RANDOM.nextDouble() * 10.0
            ), true, 100
        )
    }
}
