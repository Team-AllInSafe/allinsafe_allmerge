package de.blinkt.openvpn.ac0_main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.blinkt.openvpn.R
import de.blinkt.openvpn.SpoofingMainActivity
import de.blinkt.openvpn.ac0_login.Ac0_01_login
import de.blinkt.openvpn.ac1_applock.Ac1_01_applock_init_main
import de.blinkt.openvpn.ac2_btmanage.Ac2_01_bluetooth_main
import de.blinkt.openvpn.ac4_screenlock.Ac4_01_screenlock_main
import de.blinkt.openvpn.activities.VpnMainActivity
import de.blinkt.openvpn.databinding.OldAc000MainInitMainBinding

class Ac0_00_main_init_main : ComponentActivity() {
    lateinit var binding: OldAc000MainInitMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= OldAc000MainInitMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vpnButton.setOnClickListener{
//            Toast.makeText(this,"vpn start", Toast.LENGTH_SHORT).show()
            val intent= Intent(this, VpnMainActivity::class.java)
            startActivity(intent)
        }
        binding.appLockButton.setOnClickListener {
            val intent= Intent(this, Ac1_01_applock_init_main::class.java)
            startActivity(intent)
        }
        binding.settingButton.setOnClickListener {
            val intent= Intent(this, Ac0_09_user_info::class.java)
            startActivity(intent)
        }
        binding.bluetoothManageButton.setOnClickListener {
//            Toast.makeText(this,"bluetoothManagement start", Toast.LENGTH_SHORT).show()
            // TODO: btmanage메인 액티비티 이동
            val intent=Intent(this@Ac0_00_main_init_main, Ac2_01_bluetooth_main::class.java)
            startActivity(intent)
        }
        binding.screenLockButton.setOnClickListener {
            // TODO: screenlock메인 액티비티 이동
            val intent=Intent(this@Ac0_00_main_init_main, Ac4_01_screenlock_main::class.java)
            startActivity(intent)
        }
        binding.spoofingDetectButton.setOnClickListener {
            val intent= Intent(this, SpoofingMainActivity::class.java)
            startActivity(intent)
//            Toast.makeText(this,"spoofingDetect start", Toast.LENGTH_SHORT).show()
            // TODO: 스푸핑 메인 액티비티 이동
        }
    }
}