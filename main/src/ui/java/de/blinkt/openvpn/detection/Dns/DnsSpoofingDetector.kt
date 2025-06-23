package de.blinkt.openvpn.detection.dns

import de.blinkt.openvpn.classforui.SpoofingDetectingStatusManager
import de.blinkt.openvpn.detection.common.AlertManager
import de.blinkt.openvpn.detection.common.LogManager
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class DnsSpoofingDetector(
    private val alertManager: AlertManager
) {
    private val TAG = "DNS_DETECTOR"

    private val trustedDnsServers = setOf(
        "8.8.8.8", "8.8.4.4",
        "1.1.1.1",
        "9.9.9.9",
        "2001:4860:4860::8888", "2001:4860:4860::8844",
        "2606:4700:4700::1111", "2606:4700:4700::1001"
    )

    private val pendingRequests = ConcurrentHashMap<Int, String>()
    private val warnedTxids = mutableSetOf<Int>()

    // ✅ 테스트용 dummy request 삽입 (DummyPacketInjector에서 사용)
    fun addDummyRequest(txid: Int, ip: String) {
        pendingRequests[txid] = ip
    }

    fun processPacket(buffer: ByteBuffer) {
        if (buffer.remaining() < 40) return

        val version = (buffer.get(0).toInt() shr 4) and 0xF
        val ipHeaderSize = if (version == 4) 20 else 40
        val udpHeaderSize = 8
        val dnsPayloadStart = ipHeaderSize + udpHeaderSize

        if (buffer.remaining() < dnsPayloadStart + 12) return

        val sourceIp = if (version == 4) {
            buffer.position(12)
            "${buffer.get().toInt() and 0xFF}.${buffer.get().toInt() and 0xFF}.${buffer.get().toInt() and 0xFF}.${buffer.get().toInt() and 0xFF}"
        } else {
            buffer.position(8)
            (0 until 8).joinToString(":") { String.format("%x", buffer.short) }
        }

        buffer.position(dnsPayloadStart)
        val txid = buffer.short.toInt() and 0xFFFF
        val flags = buffer.short.toInt() and 0xFFFF
        val isResponse = (flags and 0x8000) != 0

        // ✅ Request → 최초 1회만 기록
        if (!isResponse) {
            if (!pendingRequests.containsKey(txid)) {
                pendingRequests[txid] = sourceIp
                LogManager.log(TAG, "[DNS Request Logged] TXID: $txid, 요청 서버: $sourceIp")
            }
            return
        }

        LogManager.log(TAG, "[DNS Response Logged] TXID: $txid, 응답 서버: $sourceIp")

        var failedChecks = 0

        val expectedServer = pendingRequests[txid]
        if (expectedServer == null) {
            LogManager.log(TAG, "[TXID 검사 실패] TXID: $txid 는 등록되지 않은 요청입니다.")
            failedChecks++
        } else if (expectedServer != sourceIp) {
            LogManager.log(TAG, "[TXID 불일치] 요청: $expectedServer / 응답: $sourceIp")
            failedChecks++
        }

        val ttl = try {
            if (version == 4) {
                buffer.position(8)
                buffer.get().toInt() and 0xFF
            } else {
                buffer.position(7)
                buffer.get().toInt() and 0xFF
            }
        } catch (e: Exception) {
            -1
        }

        LogManager.log(TAG, "[TTL] 값: $ttl")
        if (ttl in 1..<10) failedChecks++

        if (sourceIp !in trustedDnsServers) {
            LogManager.log(TAG, "[신뢰되지 않은 DNS 서버] $sourceIp")
            failedChecks++
        }

        val alreadyWarned = txid in warnedTxids
        if (failedChecks >= 2 && !alreadyWarned) {
            warnedTxids.add(txid)
        }

        logResult(sourceIp, txid, failedChecks)
    }

    private fun logResult(sourceIp: String, txid: Int, failedChecks: Int) {
        when (failedChecks) {
            0, 1 -> {
                LogManager.log(TAG, "[OK] 정상적인 DNS 응답 (출처: $sourceIp, TXID: $txid)")
                SpoofingDetectingStatusManager.dnsSpoofingCompleted("OK")
            }
            2 -> {
                LogManager.log(TAG, "[WARNING] DNS 스푸핑 의심 (출처: $sourceIp, TXID: $txid)")
                alertManager.sendAlert("WARNING", "DNS 스푸핑 의심", "출처: $sourceIp, TXID: $txid")
                SpoofingDetectingStatusManager.dnsSpoofingCompleted("WARNING")
            }
            3 -> {
                LogManager.log(TAG, "[CRITICAL] 🚨🚨 DNS 스푸핑 감지 (출처: $sourceIp, TXID: $txid)")
                alertManager.sendAlert("CRITICAL", "DNS 스푸핑 감지", "출처: $sourceIp, TXID: $txid")
                SpoofingDetectingStatusManager.dnsSpoofingCompleted("CRITICAL")
            }
        }
    }
}
