package de.blinkt.openvpn.ac1_applock

import de.blinkt.openvpn.R
import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.common.base.CharMatcher.invisible
import de.blinkt.openvpn.databinding.Ais11ApplockMainBinding

class Ac1_01_applock_init_main : ComponentActivity() { @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private lateinit var binding: Ais11ApplockMainBinding

    // binding 최소 api 가 33 이라며 오류 뜨길래 TargetApi 추가
    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ais11ApplockMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 사용자가 권한을 확인하면, 화면에 뭔가 바뀔 때 마다 생기는 이벤트를 받아서 그 앱 이름을 확인함
        // 앱 이름 확인하고 잠금 대상이면 종료하는 로직은 AppLockAccessibilityService 에 있음

        // 화면에 표시되는 버튼 text 동기화
        if(AppLockAccessibilityService.Companion.onoff){
            //binding.onoffbtn.text="끄기"
            turnoffUI(binding)

        }else{
            //binding.onoffbtn.text="켜기"
            turnonUI(binding)
        }
        // ACTION_ACCESSIBILITY_SETTINGS 권한 확인
        if (!isAccessibilityServiceEnabled(this, AppLockAccessibilityService::class.java)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener{
            finish()
        }
        binding.btnStartApplock.setOnClickListener {
            val pref = getSharedPreferences("AppPref", MODE_PRIVATE)
            val edit = pref.edit()
            edit.putBoolean("applock_onoff",!AppLockAccessibilityService.Companion.onoff) // 기존 상태 반전
            edit.apply()
            AppLockAccessibilityService.Companion.onoff =!AppLockAccessibilityService.Companion.onoff

            // 버튼 글자 바꾸기
            if(AppLockAccessibilityService.Companion.onoff){
                //binding.onoffbtn.text="끄기"
                turnoffUI(binding)
            }else{
               // binding.onoffbtn.text="켜기"
                turnonUI(binding)
            }
        }

        // 잠금 앱 선택 및 변경하는 액티비티
        binding.btnSelectApp.setOnClickListener {
            val intent= Intent(this, EditLockAppActivity::class.java)
            startActivity(intent)
        }

    }
    fun turnoffUI(binding: Ais11ApplockMainBinding){
        //바꿔야할거 : 위 활성화 표시, 버튼 색, 버튼 텍스트
        binding.topActive.visibility= View.VISIBLE
        binding.btnStartApplock.background=getDrawable(R.drawable.ais_round_white_full)
        binding.btnStartApplock.text="앱잠금 중단하기"
    }
    fun turnonUI(binding: Ais11ApplockMainBinding){
        //바꿔야할거 : 위 활성화 표시, 버튼 색, 버튼 텍스트
        binding.topActive.visibility= View.INVISIBLE
        binding.btnStartApplock.background=getDrawable(R.drawable.ais_round_mint_full)
        binding.btnStartApplock.text="앱잠금 실행하기"
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