package de.blinkt.openvpn.detection.packettest

import de.blinkt.openvpn.detection.arpdetector.ArpData
import de.blinkt.openvpn.detection.arpdetector.ArpSpoofingDetector
import de.blinkt.openvpn.detection.common.LogManager
import de.blinkt.openvpn.detection.dns.DnsSpoofingDetector
import java.nio.ByteBuffer

object DummyPacketInjector {

    private const val TAG = "DummyPacketInjector"
    private lateinit var dns_detector: DnsSpoofingDetector
    private lateinit var arp_detector: ArpSpoofingDetector

    fun dns_init(detector: DnsSpoofingDetector) {
        this.dns_detector = detector
    }

    fun arp_init(detector: ArpSpoofingDetector) {
        this.arp_detector = detector
    }

    // 🔹 DNS 더미 패킷 삽입 (테스트용)
    fun injectDummyDnsPacket(detector: DnsSpoofingDetector) {
        LogManager.log(TAG, "🚀 테스트용 더미 DNS 패킷 삽입...")
        detector.addDummyRequest(0, "8.8.8.8")  // ✅ 변경: addDummyRequest 사용

        val dummyBuffer = ByteBuffer.allocate(60)

        dummyBuffer.put(0x45.toByte())
        dummyBuffer.put(0)
        dummyBuffer.putShort(60)
        dummyBuffer.putShort(0)
        dummyBuffer.putShort(0)
        dummyBuffer.put(64.toByte())
        dummyBuffer.put(17.toByte())
        dummyBuffer.putShort(0)

        dummyBuffer.put(byteArrayOf(192.toByte(), 168.toByte(), 1.toByte(), 100.toByte()))
        dummyBuffer.put(byteArrayOf(10.toByte(), 0.toByte(), 0.toByte(), 1.toByte()))

        dummyBuffer.putShort(53)
        dummyBuffer.putShort(5353)
        dummyBuffer.putShort(20)
        dummyBuffer.putShort(0)

        dummyBuffer.putShort(0.toShort())
        dummyBuffer.putShort(0x8180.toShort())

        dummyBuffer.position(8)
        dummyBuffer.put(5.toByte())

        dummyBuffer.rewind()
        detector.processPacket(dummyBuffer)
    }

    // 🔹 ARP 더미 패킷 삽입
    fun injectDummyArpData(detector: ArpSpoofingDetector) {
        LogManager.log(TAG, "🧪 테스트용 더미 ARP 데이터 삽입...")

        val dummyArp = ArpData(
            senderIp = "192.168.78.1",
            senderMac = "00-11-22-33-44-66",
            targetIp = "192.168.152.254",
            targetMac = "00-50-56-f5-b8-cc"
        )
        detector.analyzePacket(dummyArp)
    }

    // 🔹 DNS 더미 패킷 삽입 (dns_detector 사용)
    fun injectDummyDnsPacket() {
        LogManager.log(TAG, "🚀 테스트용 더미 DNS 패킷 삽입...")
        dns_detector.addDummyRequest(0, "8.8.8.8")  // ✅ 변경: addDummyRequest 사용

        val dummyBuffer = ByteBuffer.allocate(60)

        dummyBuffer.put(0x45.toByte())
        dummyBuffer.put(0)
        dummyBuffer.putShort(60)
        dummyBuffer.putShort(0)
        dummyBuffer.putShort(0)
        dummyBuffer.put(64.toByte())
        dummyBuffer.put(17.toByte())
        dummyBuffer.putShort(0)

        dummyBuffer.put(byteArrayOf(192.toByte(), 168.toByte(), 1.toByte(), 100.toByte()))
        dummyBuffer.put(byteArrayOf(10.toByte(), 0.toByte(), 0.toByte(), 1.toByte()))

        dummyBuffer.putShort(53)
        dummyBuffer.putShort(5353)
        dummyBuffer.putShort(20)
        dummyBuffer.putShort(0)

        dummyBuffer.putShort(0.toShort())
        dummyBuffer.putShort(0x8180.toShort())

        dummyBuffer.position(8)
        dummyBuffer.put(5.toByte())

        dummyBuffer.rewind()
        dns_detector.processPacket(dummyBuffer)
    }

    // 🔹 ARP 더미 패킷 삽입 (arp_detector 사용)
    fun injectDummyArpData() {
        LogManager.log(TAG, "🚀 테스트용 더미 ARP 패킷 삽입...")

        val dummyArp = ArpData(
            senderIp = "192.168.78.1",
            senderMac = "00-11-22-33-44-66",
            targetIp = "192.168.152.254",
            targetMac = "00-50-56-f5-b8-cc"
        )
        arp_detector.analyzePacket(dummyArp)
    }
}
