package com.example.bluetoothapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bluetoothapp.R
import kotlinx.android.synthetic.main.fragment_device.*


private const val EXTRA_MAC_ADDRESS = "extra_mac_address"
private const val TAG = "connectionFragment"

class DeviceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val macAddress = arguments?.getString(EXTRA_MAC_ADDRESS)
        connect.setOnClickListener(View.OnClickListener {
            var connectionFragment = ConnectionFragment()
            connectionFragment.arguments = arguments
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.entry_container, connectionFragment, "connectionFragment")
                ?.addToBackStack("connectionFragment")
                ?.commit()

        })
        discovery.setOnClickListener(View.OnClickListener {
            var discoveryFragment = ServiceDiscoveryFragment()
            discoveryFragment.arguments = arguments
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.entry_container, discoveryFragment, "discoveryFragment")
                ?.addToBackStack("discoveryFragment")
                ?.commit()
        })
    }



}