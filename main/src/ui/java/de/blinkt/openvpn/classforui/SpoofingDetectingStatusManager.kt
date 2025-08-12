package de.blinkt.openvpn.classforui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import de.blinkt.openvpn.Ac5_03_spoofingdetect_completed

object SpoofingDetectingStatusManager {
    private var arpSpoofDetectResult=SpoofDetectResult("Arp",0,"","","")
    private var dnsSpoofDetectResult=SpoofDetectResult("Dns",0,"","","")
    private lateinit var context:Context
    private var isCompletedPageStart=false
    public var isCapturing=false

    //나중에 더 확장해야겠지만 지금은 이것만 넣겠습니다.
    fun init(context: Context){
        this.context =context
    }
    fun arpSpoofingCompleted(severity: String){
        Log.d("ui","ARP 스푸핑 완료됨")
        arpSpoofDetectResult.setType("Arp")
        arpSpoofDetectResult.setStatus(2)
        arpSpoofDetectResult.setSeverity(severity)
        checkIfAllCompleted()
    }
    fun dnsSpoofingCompleted(severity: String){
        Log.d("ui","DNS 스푸핑 완료됨")
        dnsSpoofDetectResult.setType("Dns")
        dnsSpoofDetectResult.setStatus(2)
        dnsSpoofDetectResult.setSeverity(severity)
        checkIfAllCompleted()
    }
    private fun checkIfAllCompleted() {
        if (arpSpoofDetectResult.getStatus() == 2 && dnsSpoofDetectResult.getStatus() == 2) {
            Log.d("ui","스푸핑 둘 다 완료됨")
            // 두 탐지 모두 완료됨 -> 다음 액티비티로 이동

            // 25.08.09 추가
            isCapturing=false

            //spooginEnd()랑 내용 똑같음
            val intent = Intent(context, Ac5_03_spoofingdetect_completed::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // context가 Activity가 아닐 수 있으므로
            completedPageStart()
            context.startActivity(intent)

            Log.d("ui","503액티비티 출력")
        }
    }
    fun spoofingEnd(){
        val intent = Intent(context, Ac5_03_spoofingdetect_completed::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // context가 Activity가 아닐 수 있으므로
        completedPageStart()
        context.startActivity(intent)
    }
    fun completedPageStart(){
        isCompletedPageStart=true
    }
    fun getIsCompletedPageStart(): Boolean{
        return isCompletedPageStart
    }
    fun getArpSeverity():String{
        return arpSpoofDetectResult.getSeverity()
    }
    fun getDnsSeverity():String{
        return dnsSpoofDetectResult.getSeverity()
    }
}

