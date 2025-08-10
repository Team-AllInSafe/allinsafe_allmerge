/*
 * Copyright (c) 2012-2025 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.allinsafemain

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import de.blinkt.openvpn.SpoofingMainActivity
//import de.blinkt.openvpn.SpoofingMainActivity
import de.blinkt.openvpn.activities.VpnMainActivity
import de.blinkt.openvpn.databinding.OldAc000MainInitMainBinding

class AllinsafeMainActivity :ComponentActivity() {
    private lateinit var binding:OldAc000MainInitMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=OldAc000MainInitMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.vpnButton.setOnClickListener {
            val intent= Intent(this,VpnMainActivity::class.java)
            startActivity(intent)
        }

        binding.spoofingDetectButton.setOnClickListener {
            val intent= Intent(this, SpoofingMainActivity::class.java)
            startActivity(intent)
        }

        binding.appLockButton.setOnClickListener {}

        binding.screenLockButton.setOnClickListener {}

        binding.bluetoothManageButton.setOnClickListener {}


    }
}