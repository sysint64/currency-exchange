package ru.kabylin.andrey.currencyexchange.services.models

import com.google.gson.Gson
import junit.framework.Assert
import org.junit.Test
import ru.kabylin.andrey.currencyexchange.BaseTest
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.client.AccessError
import ru.kabylin.andrey.currencyexchange.client.AccessErrorReason
import java.nio.file.Paths

class ApiRatesResponseTest : BaseTest() {
    @Test
    fun `convertApiRatesResponseToRateResponseList success`() {
        val path = Paths.get(baseResPath.toString(), "services", "latest.json")
        val classloader = Thread.currentThread().contextClassLoader
        val json = String(classloader.getResourceAsStream(path.toString()).readBytes())
        val payload = Gson().fromJson(json, ApiRatesResponse::class.java)
        val list = convertApiRatesResponseToRateResponseList(payload)

        Assert.assertEquals("AUD", list[0].title)
        Assert.assertEquals("AUD", list[0].ref)
        Assert.assertEquals(R.string.description_currency_aud, list[0].description.res)
        Assert.assertEquals(R.mipmap.flag_aud, list[0].flag)
        Assert.assertEquals("1.6184", list[0].value)

        Assert.assertEquals("HRK", list[10].title)
        Assert.assertEquals("HRK", list[10].ref)
        Assert.assertEquals(R.string.description_currency_hrk, list[10].description.res)
        Assert.assertEquals(R.mipmap.flag_hrk, list[10].flag)
        Assert.assertEquals("7.4431", list[10].value)

        Assert.assertEquals("MXN", list[18].title)
        Assert.assertEquals("MXN", list[18].ref)
        Assert.assertEquals(R.string.description_currency_mxn, list[18].description.res)
        Assert.assertEquals(R.mipmap.flag_mxn, list[18].flag)
        Assert.assertEquals("22.392", list[18].value)

        Assert.assertEquals("THB", list[28].title)
        Assert.assertEquals("THB", list[28].ref)
        Assert.assertEquals(R.string.description_currency_thb, list[28].description.res)
        Assert.assertEquals(R.mipmap.flag_thb, list[28].flag)
        Assert.assertEquals("38.176", list[28].value)
    }

    @Test
    fun `convertApiRatesResponseToRateResponseList success convert with missing mapping`() {
        val path = Paths.get(baseResPath.toString(), "services", "latest_bad_mapping.json")
        val classloader = Thread.currentThread().contextClassLoader
        val json = String(classloader.getResourceAsStream(path.toString()).readBytes())
        val payload = Gson().fromJson(json, ApiRatesResponse::class.java)
        val list = convertApiRatesResponseToRateResponseList(payload)

        Assert.assertEquals("NEW_CURRENCY", list[0].title)
        Assert.assertEquals("NEW_CURRENCY", list[0].ref)
        Assert.assertEquals("-", list[0].description.string)
        Assert.assertEquals(R.mipmap.flag_unknown, list[0].flag)
        Assert.assertEquals("99.99", list[0].value)
    }

    @Test
    fun `convertApiRatesResponseToRateResponseList fail with BAD_RESPONSE for non valid json`() {
        val path = Paths.get(baseResPath.toString(), "services", "latest_invalid_json.json")
        val classloader = Thread.currentThread().contextClassLoader
        val json = String(classloader.getResourceAsStream(path.toString()).readBytes())

        try {
            val payload = safeTransform {
                Gson().fromJson(json, ApiRatesResponse::class.java)
            }

            convertApiRatesResponseToRateResponseList(payload)
            Assert.fail("Expected AccessError with BAD_RESPONSE, but success got")
        } catch (e: AccessError) {
            Assert.assertEquals(AccessErrorReason.BAD_RESPONSE, e.reason)
        }
    }
}
