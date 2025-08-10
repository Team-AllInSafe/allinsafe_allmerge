package de.blinkt.openvpn.ac1_applock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import de.blinkt.openvpn.databinding.TempActivityAlarmBinding

class AlarmActivity : ComponentActivity() {
    // 앱을 다시 켰을 때, 설정화면이 아닌 잠금 걸었던 락 페이지가 나오는 것을 막기 위한 플래그
    // 락 페이지 떴을 때 뒤로 갔다면, 다시 우리 앱이 켜졌을 때 mainactivity로 가게끔
    private var wasInBackground = false
    private lateinit var binding: TempActivityAlarmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= TempActivityAlarmBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.gotohome.setOnClickListener {
            // 홈 화면으로
            val home = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                //Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK //이거하면 오류나서 수정함
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(home)
        }

        //todo 잠금 푸는거 추가
    }

    override fun onStop() {
        super.onStop()
        wasInBackground = true
    }
    override fun onResume() {
        super.onResume()

        if (wasInBackground) {
            wasInBackground = false // 플래그 초기화
            finish()
        }
    }
}