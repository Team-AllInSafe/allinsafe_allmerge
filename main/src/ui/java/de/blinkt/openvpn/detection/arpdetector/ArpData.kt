package de.blinkt.openvpn.detection.arpdetector

import java.net.InetAddress

data class ArpData(
    val senderIp: String,
    val senderMac: String,
    val targetIp: String,
    val targetMac: String
) {
    companion object {
        fun fromPacket(packet: ByteArray): ArpData? {
            return try {
                if (packet.size < 42) return null // 최소 길이 검사

                val senderIp = InetAddress.getByAddress(packet.copyOfRange(28, 32)).hostAddress ?: return null
                val senderMac = packet.copyOfRange(22, 28)
                    .joinToString(":") { "%02X".format(it) }.lowercase()

                val targetIp = InetAddress.getByAddress(packet.copyOfRange(38, 42)).hostAddress ?: return null
                val targetMac = packet.copyOfRange(32, 38)
                    .joinToString(":") { "%02X".format(it) }.lowercase()

                ArpData(senderIp, senderMac, targetIp, targetMac)
            } catch (e: Exception) {
                null
            }
        }
    }
    override fun toString(): String {
        return "[ARP] $senderIp → $targetIp | $senderMac → $targetMac"
    }
}
