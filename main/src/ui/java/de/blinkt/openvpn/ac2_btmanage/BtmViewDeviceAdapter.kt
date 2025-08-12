package de.blinkt.openvpn.ac2_btmanage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.R

// 기기 유형을 정의하는 enum 클래스
enum class DeviceType {
    TRUSTED, BLOCKED
}

// 기기 정보를 담는 데이터 클래스
data class Device(
    val name: String,
    val address: String,
    val type: DeviceType // 기기 유형을 나타내는 속성 추가
)

class BtmViewDeviceAdapter(private var devices: List<Device>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 뷰 타입 상수를 정의합니다.
    private val VIEW_TYPE_TRUSTED = 1
    private val VIEW_TYPE_BLOCKED = 2

    // 신뢰 기기 아이템의 뷰 홀더
    class TrustedDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.btm_trust_device_name)
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.btm_trust_device_address)
    }

    // 차단 기기 아이템의 뷰 홀더
    class BlockedDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.btm_block_device_name)
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.btm_block_device_address)
    }

    // 아이템의 뷰 타입을 반환하는 메서드를 오버라이드합니다.
    override fun getItemViewType(position: Int): Int {
        return when (devices[position].type) {
            DeviceType.TRUSTED -> VIEW_TYPE_TRUSTED
            DeviceType.BLOCKED -> VIEW_TYPE_BLOCKED
        }
    }

    // 뷰 타입에 따라 다른 뷰 홀더를 생성합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TRUSTED -> {
                val view = inflater.inflate(R.layout.ais23_btm_trust_device_item, parent, false)
                TrustedDeviceViewHolder(view)
            }
            VIEW_TYPE_BLOCKED -> {
                val view = inflater.inflate(R.layout.ais24_btm_block_device_item, parent, false)
                BlockedDeviceViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // 뷰 홀더에 데이터를 바인딩합니다. (뷰 타입에 따라 캐스팅)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val device = devices[position]
        when (holder.itemViewType) {
            VIEW_TYPE_TRUSTED -> {
                val trustedHolder = holder as TrustedDeviceViewHolder
                trustedHolder.deviceNameTextView.text = device.name
                trustedHolder.deviceAddressTextView.text = device.address
            }
            VIEW_TYPE_BLOCKED -> {
                val blockedHolder = holder as BlockedDeviceViewHolder
                blockedHolder.deviceNameTextView.text = device.name
                blockedHolder.deviceAddressTextView.text = device.address
            }
        }
    }

    // 아이템 수를 반환합니다.
    override fun getItemCount(): Int {
        return devices.size
    }

    // 데이터를 업데이트하는 함수입니다.
    fun updateData(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}