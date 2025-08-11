package de.blinkt.openvpn

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import de.blinkt.openvpn.databinding.Ais53SpoofCompletedBinding

class TempActivity : AppCompatActivity() {
    lateinit var binding: Ais53SpoofCompletedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ais53SpoofCompletedBinding.inflate(layoutInflater)
        //binding.imageView2.setImageResource(R.drawable.ais_ic_spoof_danger_red)
    }
}