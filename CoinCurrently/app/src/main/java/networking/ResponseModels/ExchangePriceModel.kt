package networking.ResponseModels

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(with = ExchangePriceModelSerializer::class)
data class ExchangePriceModel(
    val price: Double? = null,
    val timestamp: Double? = null
)

object ExchangePriceModelSerializer : KSerializer<ExchangePriceModel> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("ExchangePriceModel", StructureKind.LIST) {
            element<Double>("timestamp")
            element<String>("price")
        }

    override fun deserialize(decoder: Decoder): ExchangePriceModel {
        val input = decoder.beginStructure(descriptor)

        var timestamp: Double? = null
        var price: Double? = null

        var index = 0
        while (true) {
            when (val i = input.decodeElementIndex(descriptor)) {
                0 -> timestamp = input.decodeDoubleElement(descriptor, 0)
                1 -> {
                    val priceString =
                        input.decodeStringElement(descriptor, 1)
                    price = priceString.toDoubleOrNull()
                }
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index $i")
            }
            index++
        }

        input.endStructure(descriptor)

        return ExchangePriceModel(
            price = price,
            timestamp = timestamp
        )
    }

    override fun serialize(encoder: Encoder, value: ExchangePriceModel) {
        val output = encoder.beginStructure(descriptor)
        value.timestamp?.let { output.encodeDoubleElement(descriptor, 0, it) }
        value.price?.let { output.encodeStringElement(descriptor, 1, it.toString()) }
        output.endStructure(descriptor)
    }
}