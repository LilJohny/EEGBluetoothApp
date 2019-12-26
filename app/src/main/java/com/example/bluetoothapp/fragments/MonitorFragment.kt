package com.example.bluetoothapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import com.example.bluetoothapp.activities.PlotActivity
import com.example.bluetoothapp.activities.ScanActivity

class MonitorFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var intent : Intent = Intent(activity, PlotActivity::class.java)
        startActivity(intent)
        return inflater.inflate(R.layout.activity_plot, container, false)
    }
}