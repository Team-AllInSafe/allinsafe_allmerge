/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package de.blinkt.openvpn.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import de.blinkt.openvpn.R
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.IOpenVPNServiceInternal
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.databinding.MainActivityBinding
import de.blinkt.openvpn.databinding.Ac301VpnInitMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class MainActivity : BaseActivity() {
//    lateinit var binding:MainActivityBinding
    lateinit var binding:Ac301VpnInitMainBinding
    private var mSelectedProfile: VpnProfile? = null
    private var mSelectedProfileReason: String? = null
    private var isError = 1
    private var isOk = 0
    private var START_VPN_PROFILE = 11 // 그냥 아무 숫자 넣음

    private var vpnService: IOpenVPNServiceInternal? = null

    private val vpnServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            vpnService = IOpenVPNServiceInternal.Stub.asInterface(service)
            // 이제 vpnService?.stopVPN(false) 등 호출 가능
            Log.d("allinsafevpn", "VPN 서비스 연결됨")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            vpnService = null
            Log.d("allinsafevpn", "VPN 서비스 연결 끊김")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //기존 ui
        //binding=MainActivityBinding.inflate(layoutInflater)
        binding=Ac301VpnInitMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //기존의 vpnConnect 버튼
        binding.btnVpnStart.setOnClickListener {
            // 입력창에서 id pw 추출
            val id=binding.vpnid.text.toString()
            val pw=binding.vpnpw.text.toString()

            // 접속을 위한 정보 들어있는 파일 ovpn 불러오기(앱 내장)
            // assets에 있는 실제 파일을 읽어들여서 data 폴더에 저장(앱 내부용 저장소)
            val inputStream = assets.open("client.ovpn")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val parser = ConfigParser()
            parser.parseConfig(reader)

            //openvpn 형식에 맞는 vpn 프로필 만들고, 값 넣어서 초기화 시키기
            val profile = parser.convertProfile()
            if (profile.uuid == null) {
                profile.uuid = UUID.randomUUID()
            }
            profile.mName = "MyVPN"
            profile.mProfileCreator = packageName
            profile.mUsername = id // 입력창에서 받은 text
            profile.mPassword = pw

            // profile 저장.
            // 과정은 de.blinkt.openvpn.activities.VPNPrefervence.addProfile 함수 참고
            val pm = ProfileManager.getInstance(applicationContext) //checkInstance 로 instant 초기화

            //profiles 해시맵 형태의 리스트? 같은 것에 추가
            pm.addProfile(profile)

            // preference에 해시맵 저장. 원래 저장되어있던 옛날 버전 덮어쓰는 과정
            pm.saveProfileList(applicationContext)

            // 새로 저장된 vpn 프로필을 파일로 만드는 것 같음.
            // 왜 하는진 잘 모르겠으나, 기존의 코드에서 이렇게 처리하길래 넣음. 결국에 연결까지는 잘 되니 된거아니겠음~ 25.06.07
            ProfileManager.saveProfile(applicationContext, profile)

            val reprofile = ProfileManager.get(applicationContext,profile.uuidString)

            val profileToConnect = ProfileManager.getInstance(applicationContext).getProfileByName(reprofile.name)
            val startReason = intent.getStringExtra(OpenVPNService.EXTRA_START_REASON)

            if (profileToConnect == null) {
                Log.d("allinsafevpn","profileToConnect is null")
            } else {
                mSelectedProfile = profileToConnect
                mSelectedProfileReason = startReason
                launchVPN()
            }
            {//            실제 연결을 하는 Vpnprepare를 불러오는 LaunchVPN 로 intent
//            val intent = Intent(applicationContext, LaunchVPN::class.java)
//            intent.action = Intent.ACTION_MAIN
//            intent.putExtra(LaunchVPN.EXTRA_KEY, reprofile.uuidString)
//            startActivity(intent)
            }

        }

        //기존의 vpnDisconnect 버튼
        binding.btnVpnStop.setOnClickListener {

            //연결 끊기
//            이렇게 하면 disconnect,log 선택하는 창이 뜸
//
//            val intent = Intent(
//                getActivity(this),
//                DisconnectVPN::class.java
//            )
//            startActivity(intent)


            //todo vpnService가 null 이란다 여기서부터 시작!!!!!!!!!!!!!!!!!!!!!!!!!!

            Log.d("allinsafevpn","disconnect btn onclick")
            ProfileManager.setConntectedVpnProfileDisconnected(applicationContext)
            Log.d("allinsafevpn",vpnService.toString())
            vpnService?.stopVPN(false)
            Log.d("allinsafevpn","stopvpn 실행됨")
        }
    }



    private fun launchVPN() {
        val isOkProfile = mSelectedProfile!!.checkProfile(applicationContext);
        if (isOkProfile != R.string.no_error_found){
            return
        }
        val intent = VpnService.prepare(this)

        if(intent != null){
//            VpnStatus.updateStateString(
//                "USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
//                ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT
//            )
            startActivityForResult(intent, START_VPN_PROFILE)
        }else{
            onActivityResult(START_VPN_PROFILE, RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == START_VPN_PROFILE) {
            if (resultCode == RESULT_OK) {
                // 이 vpn profile의 최근 사용 시간 업데이트
                ProfileManager.updateLRU(applicationContext, mSelectedProfile)

                //VPNLaunchHelper.startOpenVpn 함수 내용을 직접 호출
                val profileToConnect = ProfileManager.getInstance(applicationContext).getProfileByName(mSelectedProfile?.name)
                val startReason = intent.getStringExtra(OpenVPNService.EXTRA_START_REASON)
                val replace_running_vpn = true
                val startVPN: Intent = profileToConnect.getStartServiceIntent(applicationContext, startReason, replace_running_vpn)
                Log.d("allinsafevpn", "startVPN intent : $startVPN")
                if (startVPN != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        applicationContext.startForegroundService(startVPN)
                    }else applicationContext.startService(startVPN)
                }
                val bindIntent = Intent(this, OpenVPNService::class.java)
                bindService(bindIntent, vpnServiceConnection, Context.BIND_AUTO_CREATE)
            }
        } else if (resultCode == RESULT_CANCELED) {
            // User does not want us to start, so we just vanish
//            VpnStatus.updateStateString(
//                "USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
//                ConnectionStatus.LEVEL_NOTCONNECTED
//            )
        }
    }


}