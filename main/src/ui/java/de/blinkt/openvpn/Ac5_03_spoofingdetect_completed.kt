package de.blinkt.openvpn

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.Ac5_04_spoofingdetect_detect_history
import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager
import de.blinkt.openvpn.databinding.Ac503SpoofingdetectCompletedBinding
import de.blinkt.openvpn.databinding.Ac506SpofingdetectItemLogBinding
import de.blinkt.openvpn.detection.common.LogManager


class Ac5_03_spoofingdetect_completed : ComponentActivity() {
    lateinit var activity502:Ac5_02_spoofingdetect_process
    private lateinit var binding: Ac503SpoofingdetectCompletedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Ac503SpoofingdetectCompletedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity502= Ac5_02_spoofingdetect_process.activity502!!
        val adapter =CompletedLogViewAdapter(LogManager.getLogs().toMutableList())
        binding.recyclerLog.adapter=adapter
        LogManager.addObserver { updatedLogs ->
            adapter.updateLogs(updatedLogs)
            binding.recyclerLog.scrollToPosition(adapter.itemCount - 1)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
////        //ui확인을 위한 임시 부분임
//        set_arp_normal(binding)
//        set_dns_normal(binding)
//        var arp_isnormal=true
//        var dns_isnormal=true
//        binding.arpBg.setOnClickListener {
//            //ui확인을 위한 임시 함수로, 실제 ui적용시 삭제할 예정임
//            if(arp_isnormal){
//                arp_isnormal=false
//                set_arp_abnormal(binding)
//            }
//            else{
//                arp_isnormal=true
//                set_arp_normal(binding)
//            }
//
//        }
//        binding.dnsView.setOnClickListener {
//            //ui확인을 위한 임시 함수로, 실제 ui적용시 삭제할 예정임
//            if(dns_isnormal){
//                dns_isnormal=false
//                set_dns_abnormal(binding)
//            }
//            else{
//                dns_isnormal=true
//                set_dns_normal(binding)
//            }
//        }
////        //ui확인을 위한 임시 부분임

        binding.backButton.setOnClickListener {
            activity502.finish()
            finish()
        }
        binding.btnShowDetectHistory.setOnClickListener {
            var intent = Intent(this, Ac5_04_spoofingdetect_detect_history::class.java)
            startActivity(intent)
        }

        if (SpoofingDetectingStatusManager.getArpSeverity() == "CRITICAL"
            || SpoofingDetectingStatusManager.getArpSeverity()=="WARNING") {
            set_arp_abnormal(binding)
        } else {
            set_arp_normal(binding)
        }
        if (SpoofingDetectingStatusManager.getDnsSeverity() == "CRITICAL"
            || SpoofingDetectingStatusManager.getDnsSeverity() == "WARNING") {
            set_dns_abnormal(binding)
        }
        else{
            set_dns_normal(binding)
        }
//        fun multiFinish(){
//            this@Ac5_03_spoofoingdetect_completed.finishAffinity()
//        }
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 실행시 실행할 동작코드 구현하기! (앱종료, 다이얼로그 띄우기 등등)
            activity502.finish()
            finish()
        }
    }
    fun set_arp_normal(binding: Ac503SpoofingdetectCompletedBinding){
        binding.arpBg.background = ContextCompat.getDrawable(binding.root.context, R.drawable.btn_round_green)
        binding.arpText.text="미탐지"
    }
    fun set_arp_abnormal(binding: Ac503SpoofingdetectCompletedBinding){
        binding.arpBg.background = ContextCompat.getDrawable(binding.root.context, R.drawable.btn_round_red)
        binding.arpText.text="탐지"
    }
    fun set_dns_normal(binding: Ac503SpoofingdetectCompletedBinding){
        binding.dnsView.background = ContextCompat.getDrawable(binding.root.context, R.drawable.btn_round_green)
        binding.dnsText.text="미탐지"
    }
    fun set_dns_abnormal(binding: Ac503SpoofingdetectCompletedBinding){
        binding.dnsView.background = ContextCompat.getDrawable(binding.root.context, R.drawable.btn_round_red)
        binding.dnsText.text="탐지"
    }
}

class CompletedLogViewHolder(var binding: Ac506SpofingdetectItemLogBinding): RecyclerView.ViewHolder(binding.root)
class CompletedLogViewAdapter(private val LogList: MutableList<String>) :
    RecyclerView.Adapter<LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding= Ac506SpofingdetectItemLogBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LogViewHolder(binding)
    }

    override fun getItemCount(): Int = LogList.size

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log=LogList[position]
        holder.binding.recyclerLog.text=log
    }
    fun updateLogs(newLogs: List<String>) {
        LogList.clear()
        LogList.addAll(newLogs)
        notifyDataSetChanged()
    }
}