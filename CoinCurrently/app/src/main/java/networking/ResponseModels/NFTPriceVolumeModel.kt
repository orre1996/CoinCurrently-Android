package networking.ResponseModels

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Encoder

@Serializable
data class NFTPriceVolumeModel(
    val floor_price_usd: List<NftDataPoint>? = null,
    val floor_price_native: List<NftDataPoint>? = null,
    val h24_volume_usd: List<NftDataPoint>? = null,
    val h24_volume_native: List<NftDataPoint>? = null,
    val market_cap_usd: List<NftDataPoint>? = null,
    val market_cap_native: List<NftDataPoint>? = null
)

@Serializable(with = NftDataPointSerializer::class)
data class NftDataPoint(
    val timestamp: Double?,
    val price: Double?
)

object NftDataPointSerializer : KSerializer<NftDataPoint> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("NftDataPoint", StructureKind.LIST) {
            element<Double?>("timestamp")
            element<Double?>("price")
        }

    override fun serialize(
        encoder: Encoder,
        value: NftDataPoint
    ) {
        val output = encoder.beginStructure(descriptor)
        value.timestamp?.let { output.encodeDoubleElement(descriptor, 0, it) }
        value.price?.let { output.encodeDoubleElement(descriptor, 1, it) }
        output.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): NftDataPoint {
        val input = decoder.beginStructure(descriptor)

        var timestamp: Double? = null
        var price: Double? = null

        loop@ while (true) {
            when (val index = input.decodeElementIndex(descriptor)) {
                0 -> timestamp = input.decodeNullableSerializableElement(
                    descriptor,
                    0,
                    Double.serializer()
                )
                1 -> {
                    // price can be Double or String
                    price = try {
                        input.decodeDoubleElement(descriptor, 1)
                    } catch (_: Exception) {
                        input.decodeStringElement(descriptor, 1).toDoubleOrNull()
                    }
                }
                CompositeDecoder.DECODE_DONE -> break@loop
            }
        }

        input.endStructure(descriptor)
        return NftDataPoint(timestamp, price)
    }
}