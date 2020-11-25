package com.example.bluetoothapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.utils.Writer
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.io.File
import java.util.*

class MonitorFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sleep_stage, container, false)
    }
    companion object {
        private val RANDOM = Random()
    }
    private var series1: LineGraphSeries<DataPoint>? = null
    private var lastX : Double = 0.0
    //private lateinit var line:List<String>
    private var i = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val graph = this.view?.findViewById<View>(R.id.graph) as GraphView
        series1 = LineGraphSeries()
        series1!!.color = Color.RED
        series1!!.backgroundColor = Color.YELLOW
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(4.0)
        graph.viewport.setMaxX(80.0)
        graph.viewport.isScalable = true
        graph.title = "Data from EEG"
        graph.addSeries(series1)
        graph.viewport
        //line  = Writer.readFile(this.view!!.context.filesDir,"data.txt")[0].replace("[","").replace("]","").split(",")

    }

    override fun onResume() {
        super.onResume()
        Thread {
            for (i in 0..9999) {
                activity?.runOnUiThread { addEntry() }
                try {
                    //Thread.sleep(250)
                } catch (e: InterruptedException) {
                    e.printStackTrace();
                }
            }
        }.start()
    }
    private fun addEntry() {
        series1!!.appendData(
            DataPoint(
                lastX++,
                nextDouble() * 10.0
            ), true, 100
        )
    }
    private fun nextDouble():Double{

        return (RANDOM.nextDouble()*5.0)
    }

}