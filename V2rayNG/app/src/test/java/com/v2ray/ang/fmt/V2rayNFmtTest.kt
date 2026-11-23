package com.v2ray.ang.fmt

import android.util.Base64
import android.util.Log
import com.v2ray.ang.enums.EConfigType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import java.util.Base64 as JavaBase64

class V2rayNFmtTest {

    private lateinit var mockBase64: MockedStatic<Base64>
    private lateinit var mockLog: MockedStatic<Log>

    @BeforeEach
    fun setUp() {
        mockLog = mockStatic(Log::class.java, Mockito.RETURNS_DEFAULTS)
        mockBase64 = mockStatic(Base64::class.java)
        mockBase64.`when`<ByteArray> {
            Base64.decode(Mockito.anyString(), Mockito.anyInt())
        }.thenAnswer { invocation ->
            JavaBase64.getDecoder().decode(invocation.arguments[0] as String)
        }
    }

    @AfterEach
    fun tearDown() {
        mockLog.close()
        mockBase64.close()
    }

    private fun shareLink(json: String): String =
        "v2rayn://" + JavaBase64.getEncoder().encodeToString(json.toByteArray())

    @Test
    fun proxyChainGetsMembersAsDescription() {
        val lines = listOf(
            shareLink("""{"IndexId":"a","ConfigType":4,"Remarks":"A","Address":"a.example.com","Port":1080}"""),
            shareLink("""{"IndexId":"b","ConfigType":4,"Remarks":"B","Address":"b.example.com","Port":1080}"""),
            shareLink("""{"IndexId":"c","ConfigType":102,"Remarks":"Chain","Port":0,"ProtoExtraObj":{"ChildItems":"a,b"}}"""),
        )

        val chain = V2rayNFmt.parse(lines, "sub")
            .single { it.configType == EConfigType.PROXYCHAIN }

        assertEquals(listOf("A", "B"), chain.proxyChainProfileList)
        assertEquals("A -> B", chain.description)
    }
}
