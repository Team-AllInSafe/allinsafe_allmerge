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
import de.blinkt.openvpn.ac1_applock.AppLockAccessibilityService
import de.blinkt.openvpn.databinding.Ac102ActivityEditLockAppBinding
import de.blinkt.openvpn.databinding.Ac103AppListItemBinding

class EditLockAppActivity : ComponentActivity() {
    private lateinit var binding: Ac102ActivityEditLockAppBinding
    data class AppInfo(
        val packageName: String,
        val name: String,
        val icon: Drawable,
        var isChecked: Boolean = false
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= Ac102ActivityEditLockAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //초기화면에 앱 목록 짜넣기
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        // 시스템 앱 제외
        val userapps = apps.filterNot{ isSystemApp(it) or it.packageName.equals(applicationContext.packageName)}

        val appList = userapps.map {
            val name = pm.getApplicationLabel(it).toString()
            val packagename = it.packageName
            val icon = pm.getApplicationIcon(it)
            val isChecked = AppLockAccessibilityService.Companion.lockedPackageList.contains(it.packageName)
            AppInfo(packagename,name, icon,isChecked)
        }
        binding.appListView.adapter = AppListAdapter(this, appList)

    }

    fun isSystemApp(appInfo: ApplicationInfo):Boolean{
        val flags = appInfo.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM == 0) &&
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0)
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    class AppListAdapter(private val context: Context, private val apps: List<AppInfo>) : BaseAdapter() {
        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val binding: Ac103AppListItemBinding = if (convertView == null) {
                Ac103AppListItemBinding.inflate(LayoutInflater.from(context), parent, false)
            } else {
                Ac103AppListItemBinding.bind(convertView)
            }

            val app = apps[position]
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.appIcon.setImageDrawable(app.icon)
            binding.appName.text = app.name
            binding.checkBox.isChecked = app.isChecked

            // 체크박스 상태 변경시 데이터 반영됨
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                app.isChecked = isChecked
                //AppLockAccessibilityService에 있는 잠금 목록 업데이트
//                Log.d("sua", "[잠금 목록 업데이트 전] lockedPackageList : $lockedPackageList")
                AppLockAccessibilityService.Companion.lockedPackageList =
                    apps.filter { it.isChecked }.map { it.packageName }

//                Log.d("sua", "[잠금 목록 업데이트 후] lockedPackageList : $lockedPackageList")
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