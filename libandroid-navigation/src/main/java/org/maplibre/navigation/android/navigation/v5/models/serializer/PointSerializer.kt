package org.maplibre.navigation.android.navigation.v5.models.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.geojson.Point

class PointSerializer : KSerializer<Point> {
    private val delegateSerializer = DoubleArraySerializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("Point", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Point) {
        val data = doubleArrayOf(
            value.longitude(),
            value.latitude()
        )
        encoder.encodeSerializableValue(delegateSerializer, data)
    }

    override fun deserialize(decoder: Decoder): Point {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        return Point.fromLngLat(array[0], array[1])
    }
}