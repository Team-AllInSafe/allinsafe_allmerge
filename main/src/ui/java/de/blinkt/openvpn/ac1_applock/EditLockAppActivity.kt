package de.blinkt.openvpn.ac1_applock

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.blinkt.openvpn.ac1_applock.AppLockAccessibilityService
import de.blinkt.openvpn.databinding.Ais12SelectAppBinding
import de.blinkt.openvpn.databinding.Ais13SelectAppItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditLockAppActivity : ComponentActivity() {
    private lateinit var binding: Ais12SelectAppBinding
    data class AppInfo(
        val packageName: String,
        val name: String,
        val icon: Drawable,
        var isChecked: Boolean = false
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ais12SelectAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //초기화면에 앱 목록 짜넣기
        binding.btnBack.setOnClickListener{
            finish()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val apps = pm.getInstalledApplications(0)

            //사용자가 직접 실행할 수 있는 앱만 필터링 <- 시스템 앱 다 걸러짐
            val launchableApps = apps.filter {
                pm.getLaunchIntentForPackage(it.packageName) != null
            }

            // 시스템 앱 제외
//            val filteredApps = launchableApps.filterNot{ isSystemApp(it) or it.packageName.equals(applicationContext.packageName)}

            // 앱 이름 가나다 정렬?

            val appList = launchableApps.map {
                val name = pm.getApplicationLabel(it).toString()
                val packagename = it.packageName
                val icon = pm.getApplicationIcon(it)
                val isChecked = AppLockAccessibilityService.Companion.lockedPackageList.contains(it.packageName)
                AppInfo(packagename,name, icon,isChecked)
            }
            // ui 변경은 메인스레드가 하도록
            withContext(Dispatchers.Main) {
                //기존의 progressbar(로딩중 표시)를 삭제하였음.
//                binding.loadingCircle.visibility=View.GONE
//                binding.appListView.visibility=View.VISIBLE
                binding.listviewApp.adapter = AppListAdapter(this@EditLockAppActivity, appList)
            }
        }
//        val pm = packageManager
//        val apps = pm.getInstalledApplications(0)
//
//        //사용자가 직접 실행할 수 있는 앱만 필터링 <- 시스템 앱 다 걸러짐
//        val launchableApps = apps.filter {
//            pm.getLaunchIntentForPackage(it.packageName) != null
//        }
//
//        // 시스템 앱 제외 <- 여기에서 실사용 앱들 다 썰린다. 다 걸려서 안보임,근데 시스템 앱도 안보이게 하는건 맞음
//        val filteredApps = launchableApps.filterNot{ isSystemApp(it) or it.packageName.equals(applicationContext.packageName)}
//
//        // 앱 이름 가나다 정렬?
//
//        val appList = filteredApps.map {
//            val name = pm.getApplicationLabel(it).toString()
//            val packagename = it.packageName
//            val icon = pm.getApplicationIcon(it)
//            val isChecked = AppLockAccessibilityService.Companion.lockedPackageList.contains(it.packageName)
//            AppInfo(packagename,name, icon,isChecked)
//        }
//        binding.appListView.adapter = AppListAdapter(this, appList)

    }

    fun isSystemApp(appInfo: ApplicationInfo):Boolean{
        val flags = appInfo.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 && //플래그 없으면 0 : 시스템 앱 아님
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    class AppListAdapter(private val context: Context, private val apps: List<AppInfo>) : BaseAdapter() {
        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val binding: Ais13SelectAppItemBinding = if (convertView == null) {
                Ais13SelectAppItemBinding.inflate(LayoutInflater.from(context), parent, false)
            } else {
                Ais13SelectAppItemBinding.bind(convertView)
            }

            val app = apps[position]
            binding.checkbox.setOnCheckedChangeListener(null)
            binding.ivAppIcon.setImageDrawable(app.icon)
            binding.tvAppName.text = app.name
            binding.checkbox.isChecked = app.isChecked

            // 체크박스 상태 변경시 데이터 반영됨
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                app.isChecked = isChecked
                //AppLockAccessibilityService에 있는 잠금 목록 업데이트
                AppLockAccessibilityService.Companion.lockedPackageList =
                    apps.filter { it.isChecked }.map { it.packageName }

                saveCheckedApps(context, AppLockAccessibilityService.Companion.lockedPackageList)
            }

            return binding.root
        }

        fun saveCheckedApps(context: Context, checkedApps: List<String>) {
            val prefs = context.getSharedPreferences("AppPref", MODE_PRIVATE)
            prefs.edit()
                .putStringSet("locked_apps", checkedApps.toSet())
                .apply()
        }
    }
}