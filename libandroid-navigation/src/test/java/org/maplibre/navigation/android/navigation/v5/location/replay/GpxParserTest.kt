package org.maplibre.navigation.android.navigation.v5.location.replay

import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import javax.xml.parsers.ParserConfigurationException

@RunWith(RobolectricTestRunner::class)
class GpxParserTest {
    @Test
    fun sanity() {
        val parser = GpxParser()

        Assert.assertNotNull(parser)
    }

    @Test
    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        ParseException::class,
        IOException::class
    )
    fun invalidGpxTags_returnsNullList() {
        val parser = GpxParser()
        val inputStream = buildTestGpxInputStream(TEST_INVALID_GPX)

        val parsedLocations = parser.parseGpx(inputStream)

        Assert.assertNull(parsedLocations)
    }

    @Test
    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        ParseException::class,
        IOException::class
    )
    fun validGpxFile_returnsPopulatedLocationList() {
        val parser = GpxParser()
        val inputStream = buildTestGpxInputStream(TEST_GPX)

        val parsedLocations = parser.parseGpx(inputStream)

        Assert.assertTrue(!parsedLocations!!.isEmpty())
    }

    @Test
    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        ParseException::class,
        IOException::class
    )
    fun validGpxFile_returnsCorrectAmountOfLocations() {
        val parser = GpxParser()
        val inputStream = buildTestGpxInputStream(TEST_GPX)

        val parsedLocations = parser.parseGpx(inputStream)

        Assert.assertEquals(3, parsedLocations!!.size)
    }

    @Test
    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        ParseException::class,
        IOException::class
    )
    fun firstLocationUpdate_returnsCorrectLatitude() {
        val parser = GpxParser()
        val inputStream = buildTestGpxInputStream(TEST_GPX)

        val parsedLocations = parser.parseGpx(inputStream)

        val actualFirstLatitude = parsedLocations!![FIRST_LOCATION].latitude
        Assert.assertEquals(FIRST_TEST_GPS_LATITUDE, actualFirstLatitude)
    }

    @Test
    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        ParseException::class,
        IOException::class
    )
    fun firstLocationUpdate_returnsCorrectLongitude() {
        val parser = GpxParser()
        val inputStream = buildTestGpxInputStream(TEST_GPX)

        val parsedLocations = parser.parseGpx(inputStream)

        val actualFirstLongitude = parsedLocations!![FIRST_LOCATION].longitude
        Assert.assertEquals(FIRST_TEST_GPS_LONGITUDE, actualFirstLongitude)
    }

    @Test
    @Throws(
        ParserConfigurationException::class,
        SAXException::class,
        ParseException::class,
        IOException::class
    )
    fun firstLocationUpdate_returnsCorrectTimeInMillis() {
        val parser = GpxParser()
        val inputStream = buildTestGpxInputStream(TEST_GPX)

        val parsedLocations = parser.parseGpx(inputStream)

        val actualFirstTime = parsedLocations!![FIRST_LOCATION].time
        Assert.assertEquals(FIRST_TEST_GPS_TIME.toDouble(), actualFirstTime.toDouble(), DELTA)
    }

    private fun buildTestGpxInputStream(gpxFileName: String): InputStream {
        val classLoader = checkNotNull(javaClass.classLoader)
        return classLoader.getResourceAsStream(gpxFileName)
    }

    companion object {
        private const val DELTA = 1E-10
        private const val TEST_GPX = "test.gpx"
        private const val TEST_INVALID_GPX = "test_invalid.gpx"
        private const val FIRST_TEST_GPS_LATITUDE = 47.644548
        private const val FIRST_TEST_GPS_LONGITUDE = -122.326897
        private const val FIRST_TEST_GPS_TIME = 1255804646000L
        private const val FIRST_LOCATION = 0
    }
}
