package de.blinkt.openvpn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.R
import de.blinkt.openvpn.databinding.Ac504SpoofingdetectDetectHistoryBinding
import de.blinkt.openvpn.databinding.Ac505SpoofingdetectItemDetectHistoryBinding

class Ac5_04_spoofingdetect_detect_history : ComponentActivity() {
    private lateinit var binding: Ac504SpoofingdetectDetectHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=Ac504SpoofingdetectDetectHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            finish()
        }
        val dummyData = listOf(
            HistoryItem("2025-04-29 14:00", false,false),
            HistoryItem("2025-04-29 13:00", true,false),
            HistoryItem("2025-04-29 12:00", false,true)
        )

        val adapter = HistoryViewAdapter(dummyData)
        binding.recyclerDetectHistory.adapter = adapter
        Toast.makeText(this,"리사이클러뷰 코드 실행됨",Toast.LENGTH_LONG).show()
    }
}
data class HistoryItem(val date:String, val is_arp_spoofing_detected:Boolean, val is_dns_spoofing_detected:Boolean)
//혹시 문제생길까봐 날짜는 string으로 처리했습니다. 데모 이후 Date타입으로 변경 예정입니다.
//arp, dns spoofing은 스푸핑이 감지되었을때(스푸핑 당했을때) true, 아닌경우(정상) false입니다.

class HistoryViewHolder(val binding: Ac505SpoofingdetectItemDetectHistoryBinding): RecyclerView.ViewHolder(binding.root)
//class HistoryAdapter(val binding: Ac505SpoofingdetectItemDetectHistoryBinding):
//        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =HistoryViewHolder(
//        Ac505SpoofingdetectItemDetectHistoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
//
//    override fun getItemCount(): Int {
//        //return datas.size //이거 외 않됌?
//        return 1
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val binding=(holder as HistoryViewHolder).binding
//        //나는 여기 뭐 넣어야 되지?
//    }
//
//}
class HistoryViewAdapter(private val dataList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = Ac505SpoofingdetectItemDetectHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.detectDate.text = item.date      // textTime은 바인딩된 뷰의 ID에 맞춰 수정
        if (item.is_arp_spoofing_detected) {
            holder.binding.arpBg.background =
                ContextCompat.getDrawable(holder.binding.root.context, R.drawable.btn_round_red)
            holder.binding.arpText.text = "탐지"
        } else {
            holder.binding.arpBg.background =
                ContextCompat.getDrawable(holder.binding.root.context, R.drawable.btn_round_green)
            holder.binding.arpText.text = "미탐지"
        }
        if (item.is_dns_spoofing_detected) {
            holder.binding.dnsBg.background =
                ContextCompat.getDrawable(holder.binding.root.context, R.drawable.btn_round_red)
            holder.binding.dnsText.text = "탐지"
        } else {
            holder.binding.dnsBg.background =
                ContextCompat.getDrawable(holder.binding.root.context, R.drawable.btn_round_green)
            holder.binding.dnsText.text = "미탐지"
        }
    }
}
