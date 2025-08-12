package de.blinkt.openvpn.ac2_btmanage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.blinkt.openvpn.R

class BtmViewDeviceAdapter(
    private var devices: MutableList<Device>,
    private val onDeleteClick: (device: Device, position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TRUSTED = 1
    private val VIEW_TYPE_BLOCKED = 2

    // 신뢰 기기 아이템을 위한 뷰홀더
    class TrustedDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.btm_trust_device_name)
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.btm_trust_device_address)
        val btnDeviceDelete: ImageView = itemView.findViewById(R.id.btn_trust_delete)
    }

    // 차단 기기 아이템을 위한 뷰홀더
    class BlockedDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.btm_block_device_name)
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.btm_block_device_address)
        val btnDeviceDelete: ImageView = itemView.findViewById(R.id.btn_block_delete)
    }

    override fun getItemViewType(position: Int): Int {
        return when (devices[position].type) {
            DeviceType.TRUSTED -> VIEW_TYPE_TRUSTED
            DeviceType.BLOCKED -> VIEW_TYPE_BLOCKED
        }
    }

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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val device = devices[position]
        when (holder.itemViewType) {
            VIEW_TYPE_TRUSTED -> {
                val trustedHolder = holder as TrustedDeviceViewHolder
                trustedHolder.deviceNameTextView.text = device.name
                trustedHolder.deviceAddressTextView.text = device.address
                trustedHolder.btnDeviceDelete.setOnClickListener {
                    onDeleteClick(device, holder.adapterPosition)
                }
            }
            VIEW_TYPE_BLOCKED -> {
                val blockedHolder = holder as BlockedDeviceViewHolder
                blockedHolder.deviceNameTextView.text = device.name
                blockedHolder.deviceAddressTextView.text = device.address
                blockedHolder.btnDeviceDelete.setOnClickListener {
                    onDeleteClick(device, holder.adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int = devices.size

    // 전체 데이터 목록을 교체하고 UI를 갱신
    fun updateData(newDevices: List<Device>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    // 특정 아이템을 삭제하고 UI를 갱신
    fun removeItem(position: Int) {
        if (position in 0 until devices.size) {
            devices.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}