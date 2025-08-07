package de.blinkt.openvpn.ac1_applock

import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import de.blinkt.openvpn.databinding.Ac101ApplockMainBinding
import de.blinkt.openvpn.databinding.ActivityMainBinding

class Ac1_01_applock_init_main : ComponentActivity() { @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private lateinit var binding: Ac101ApplockMainBinding

    // binding 최소 api 가 33 이라며 오류 뜨길래 TargetApi 추가
    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ac101ApplockMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 사용자가 권한을 확인하면, 화면에 뭔가 바뀔 때 마다 생기는 이벤트를 받아서 그 앱 이름을 확인함
        // 앱 이름 확인하고 잠금 대상이면 종료하는 로직은 AppLockAccessibilityService 에 있음

        if(AppLockAccessibilityService.Companion.onoff){
            binding.onoffbtn.text="끄기"

        }else{
            binding.onoffbtn.text="켜기"
        }
        // ACTION_ACCESSIBILITY_SETTINGS 권한 확인
        if (!isAccessibilityServiceEnabled(this, AppLockAccessibilityService::class.java)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        binding.onoffbtn.setOnClickListener {
            val pref = getSharedPreferences("AppPref", MODE_PRIVATE)
            val edit = pref.edit()
            edit.putBoolean("onoff",!AppLockAccessibilityService.Companion.onoff) // 기존 상태 반전
            edit.apply()
            AppLockAccessibilityService.Companion.onoff =!AppLockAccessibilityService.Companion.onoff

            // 버튼 글자 바꾸기
            if(AppLockAccessibilityService.Companion.onoff){
                binding.onoffbtn.text="끄기"
            }else{
                binding.onoffbtn.text="켜기"
            }
        }

        // 잠금 앱 선택 및 변경하는 액티비티
        binding.editlockappsbtn.setOnClickListener {
            val intent= Intent(this, EditLockAppActivity::class.java)
            startActivity(intent)
        }
    }

    // ACTION_ACCESSIBILITY_SETTINGS 권한 확인 함수
    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(":").any {
            ComponentName.unflattenFromString(it) == expectedComponentName
        }
    }

}