package benchmark.serializers

import benchmark.model.Fixture
import benchmark.model.v2.Document
import benchmark.model.v2.DocumentItem
import benchmark.model.v2.DocumentMeta
import benchmark.model.v2.Event
import benchmark.model.v2.EventAttr
import benchmark.model.v2.Message
import benchmark.model.v2.NestedRow
import benchmark.model.v2.Signal
import benchmark.model.v2.Strings
import benchmark.model.v2.TableRow
import benchmark.model.v2.Telemetry
import benchmark.v2.omnibuf.BatchDocumentOmniBuf
import benchmark.v2.omnibuf.BatchEventOmniBuf
import benchmark.v2.omnibuf.BatchMessageOmniBuf
import benchmark.v2.omnibuf.BatchNestedRowOmniBuf
import benchmark.v2.omnibuf.BatchSignalOmniBuf
import benchmark.v2.omnibuf.BatchStringsOmniBuf
import benchmark.v2.omnibuf.BatchTableOmniBuf
import benchmark.v2.omnibuf.BatchTelemetryOmniBuf
import benchmark.v2.omnibuf.DocumentOmniBuf
import benchmark.v2.omnibuf.EventOmniBuf
import benchmark.v2.omnibuf.MessageOmniBuf
import benchmark.v2.omnibuf.NestedRowOmniBuf
import benchmark.v2.omnibuf.OmniBufBatchDocument
import benchmark.v2.omnibuf.OmniBufBatchEvent
import benchmark.v2.omnibuf.OmniBufBatchMessage
import benchmark.v2.omnibuf.OmniBufBatchNestedRow
import benchmark.v2.omnibuf.OmniBufBatchSignal
import benchmark.v2.omnibuf.OmniBufBatchStrings
import benchmark.v2.omnibuf.OmniBufBatchTable
import benchmark.v2.omnibuf.OmniBufBatchTelemetry
import benchmark.v2.omnibuf.OmniBufDocument
import benchmark.v2.omnibuf.OmniBufDocumentItem
import benchmark.v2.omnibuf.OmniBufDocumentMeta
import benchmark.v2.omnibuf.OmniBufEvent
import benchmark.v2.omnibuf.OmniBufEventAttr
import benchmark.v2.omnibuf.OmniBufMessage
import benchmark.v2.omnibuf.OmniBufNestedItem
import benchmark.v2.omnibuf.OmniBufNestedMeta
import benchmark.v2.omnibuf.OmniBufNestedRow
import benchmark.v2.omnibuf.OmniBufSignal
import benchmark.v2.omnibuf.OmniBufSignalLeg
import benchmark.v2.omnibuf.OmniBufStrings
import benchmark.v2.omnibuf.OmniBufTable
import benchmark.v2.omnibuf.OmniBufTelemetry
import benchmark.v2.omnibuf.OpaqueBatchTable
import benchmark.v2.omnibuf.OpaqueTable
import benchmark.v2.omnibuf.SignalOmniBuf
import benchmark.v2.omnibuf.StringsOmniBuf
import benchmark.v2.omnibuf.TableOmniBuf
import benchmark.v2.omnibuf.TelemetryOmniBuf
import com.omnibuf.wire.OmniBufEncoder

/** OmniBuf static codecs (schemas/v2/protobuf/benchmark_v2.proto). */
class OmniBufSer : BenchSerializer {
    private enum class Kind {
        MESSAGE,
        DOCUMENT,
        TELEMETRY,
        STRINGS,
        EVENT,
        TABLE,
        TABLE_PROJECT,
        NESTED_TABLE,
        SIGNAL,
        BATCH_MESSAGE,
        BATCH_DOCUMENT,
        BATCH_TELEMETRY,
        BATCH_STRINGS,
        BATCH_EVENT,
        BATCH_TABLE,
        BATCH_TABLE_PROJECT,
        BATCH_NESTED_TABLE,
        BATCH_SIGNAL,
    }

    private val encoder = OmniBufEncoder(4096)
    private var kind: Kind = Kind.MESSAGE
    private var decodeTarget: Any? = null

    override fun name() = "omnibuf"

    override fun version() = Versions.of(OmniBufEncoder::class.java)

    override fun streamMode() = "adapted"

    override fun nativeKind() = "message"

    override fun supports(testDataName: String) = TypeUtil.originalOrColumnar(testDataName)

    override fun prepare(fx: Fixture) {
        kind = resolveKind(fx.name, TypeUtil.isList(fx.value))
        decodeTarget = newDecodeTarget(kind)
    }

    override fun serializeBytes(fx: Fixture): ByteArray {
        encoder.reset()
        encode(encoder, kind, fx.value)
        return encoder.toByteArray()
    }

    override fun deserializeBytes(data: ByteArray): Any =
        when (kind) {
            Kind.MESSAGE -> {
                val target = decodeTarget as OmniBufMessage
                MessageOmniBuf.unmarshal(data, target)
                fromMessage(target)
            }
            Kind.DOCUMENT -> {
                val target = decodeTarget as OmniBufDocument
                DocumentOmniBuf.unmarshal(data, target)
                fromDocument(target)
            }
            Kind.TELEMETRY -> {
                val target = decodeTarget as OmniBufTelemetry
                TelemetryOmniBuf.unmarshal(data, target)
                fromTelemetry(target)
            }
            Kind.STRINGS -> {
                val target = decodeTarget as OmniBufStrings
                StringsOmniBuf.unmarshal(data, target)
                fromStrings(target)
            }
            Kind.EVENT -> {
                val target = decodeTarget as OmniBufEvent
                EventOmniBuf.unmarshal(data, target)
                fromEvent(target)
            }
            Kind.TABLE -> {
                val target = decodeTarget as OmniBufTable
                TableOmniBuf.unmarshal(data, target)
                fromTable(target)
            }
            Kind.TABLE_PROJECT -> listOf(OpaqueTable(data).fFloat0)
            Kind.NESTED_TABLE -> {
                val target = decodeTarget as OmniBufNestedRow
                NestedRowOmniBuf.unmarshal(data, target)
                fromNested(target)
            }
            Kind.SIGNAL -> {
                val target = decodeTarget as OmniBufSignal
                SignalOmniBuf.unmarshal(data, target)
                fromSignal(target)
            }
            Kind.BATCH_MESSAGE -> {
                val target = decodeTarget as OmniBufBatchMessage
                BatchMessageOmniBuf.unmarshal(data, target)
                target.items.map { fromMessage(it) }.toMutableList()
            }
            Kind.BATCH_DOCUMENT -> {
                val target = decodeTarget as OmniBufBatchDocument
                BatchDocumentOmniBuf.unmarshal(data, target)
                target.items.map { fromDocument(it) }.toMutableList()
            }
            Kind.BATCH_TELEMETRY -> {
                val target = decodeTarget as OmniBufBatchTelemetry
                BatchTelemetryOmniBuf.unmarshal(data, target)
                target.items.map { fromTelemetry(it) }.toMutableList()
            }
            Kind.BATCH_STRINGS -> {
                val target = decodeTarget as OmniBufBatchStrings
                BatchStringsOmniBuf.unmarshal(data, target)
                target.items.map { fromStrings(it) }.toMutableList()
            }
            Kind.BATCH_EVENT -> {
                val target = decodeTarget as OmniBufBatchEvent
                BatchEventOmniBuf.unmarshal(data, target)
                target.items.map { fromEvent(it) }.toMutableList()
            }
            Kind.BATCH_TABLE -> {
                val target = decodeTarget as OmniBufBatchTable
                BatchTableOmniBuf.unmarshal(data, target)
                target.items.map { fromTable(it) }.toMutableList()
            }
            Kind.BATCH_TABLE_PROJECT -> {
                val items = OpaqueBatchTable(data).items
                val out = ArrayList<Double>(items.size)
                for (i in 0 until items.size) {
                    out.add(items[i].fFloat0)
                }
                out
            }
            Kind.BATCH_NESTED_TABLE -> {
                val target = decodeTarget as OmniBufBatchNestedRow
                BatchNestedRowOmniBuf.unmarshal(data, target)
                target.items.map { fromNested(it) }.toMutableList()
            }
            Kind.BATCH_SIGNAL -> {
                val target = decodeTarget as OmniBufBatchSignal
                BatchSignalOmniBuf.unmarshal(data, target)
                target.items.map { fromSignal(it) }.toMutableList()
            }
        }

    companion object {
        private fun resolveKind(typeId: String, batch: Boolean): Kind =
            if (batch) {
                when (typeId) {
                    "message" -> Kind.BATCH_MESSAGE
                    "document" -> Kind.BATCH_DOCUMENT
                    "telemetry" -> Kind.BATCH_TELEMETRY
                    "strings" -> Kind.BATCH_STRINGS
                    "event" -> Kind.BATCH_EVENT
                    "table" -> Kind.BATCH_TABLE
                    "table_project" -> Kind.BATCH_TABLE_PROJECT
                    "nested_table" -> Kind.BATCH_NESTED_TABLE
                    "signal" -> Kind.BATCH_SIGNAL
                    else -> throw IllegalArgumentException(typeId)
                }
            } else {
                when (typeId) {
                    "message" -> Kind.MESSAGE
                    "document" -> Kind.DOCUMENT
                    "telemetry" -> Kind.TELEMETRY
                    "strings" -> Kind.STRINGS
                    "event" -> Kind.EVENT
                    "table" -> Kind.TABLE
                    "table_project" -> Kind.TABLE_PROJECT
                    "nested_table" -> Kind.NESTED_TABLE
                    "signal" -> Kind.SIGNAL
                    else -> throw IllegalArgumentException(typeId)
                }
            }

        private fun newDecodeTarget(kind: Kind): Any? =
            when (kind) {
                Kind.MESSAGE -> OmniBufMessage()
                Kind.DOCUMENT -> OmniBufDocument()
                Kind.TELEMETRY -> OmniBufTelemetry()
                Kind.STRINGS -> OmniBufStrings()
                Kind.EVENT -> OmniBufEvent()
                Kind.TABLE -> OmniBufTable()
                Kind.TABLE_PROJECT -> null
                Kind.NESTED_TABLE -> OmniBufNestedRow()
                Kind.SIGNAL -> OmniBufSignal()
                Kind.BATCH_MESSAGE -> OmniBufBatchMessage()
                Kind.BATCH_DOCUMENT -> OmniBufBatchDocument()
                Kind.BATCH_TELEMETRY -> OmniBufBatchTelemetry()
                Kind.BATCH_STRINGS -> OmniBufBatchStrings()
                Kind.BATCH_EVENT -> OmniBufBatchEvent()
                Kind.BATCH_TABLE -> OmniBufBatchTable()
                Kind.BATCH_TABLE_PROJECT -> null
                Kind.BATCH_NESTED_TABLE -> OmniBufBatchNestedRow()
                Kind.BATCH_SIGNAL -> OmniBufBatchSignal()
            }

        private fun encode(enc: OmniBufEncoder, kind: Kind, value: Any) {
            when (kind) {
                Kind.MESSAGE -> MessageOmniBuf.encode(enc, toMessage(value as Message), false)
                Kind.DOCUMENT -> DocumentOmniBuf.encode(enc, toDocument(value as Document), false)
                Kind.TELEMETRY -> TelemetryOmniBuf.encode(enc, toTelemetry(value as Telemetry), false)
                Kind.STRINGS -> StringsOmniBuf.encode(enc, toStrings(value as Strings), false)
                Kind.EVENT -> EventOmniBuf.encode(enc, toEvent(value as Event), false)
                Kind.TABLE, Kind.TABLE_PROJECT -> TableOmniBuf.encode(enc, toTable(value as TableRow), false)
                Kind.NESTED_TABLE -> NestedRowOmniBuf.encode(enc, toNested(value as NestedRow), false)
                Kind.SIGNAL -> SignalOmniBuf.encode(enc, toSignal(value as Signal), false)
                Kind.BATCH_MESSAGE -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufMessage>(src.size)
                    for (o in src) list.add(toMessage(o as Message))
                    BatchMessageOmniBuf.encode(enc, OmniBufBatchMessage().setItems(list), false)
                }
                Kind.BATCH_DOCUMENT -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufDocument>(src.size)
                    for (o in src) list.add(toDocument(o as Document))
                    BatchDocumentOmniBuf.encode(enc, OmniBufBatchDocument().setItems(list), false)
                }
                Kind.BATCH_TELEMETRY -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufTelemetry>(src.size)
                    for (o in src) list.add(toTelemetry(o as Telemetry))
                    BatchTelemetryOmniBuf.encode(enc, OmniBufBatchTelemetry().setItems(list), false)
                }
                Kind.BATCH_STRINGS -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufStrings>(src.size)
                    for (o in src) list.add(toStrings(o as Strings))
                    BatchStringsOmniBuf.encode(enc, OmniBufBatchStrings().setItems(list), false)
                }
                Kind.BATCH_EVENT -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufEvent>(src.size)
                    for (o in src) list.add(toEvent(o as Event))
                    BatchEventOmniBuf.encode(enc, OmniBufBatchEvent().setItems(list), false)
                }
                Kind.BATCH_TABLE, Kind.BATCH_TABLE_PROJECT -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufTable>(src.size)
                    for (o in src) list.add(toTable(o as TableRow))
                    BatchTableOmniBuf.encode(enc, OmniBufBatchTable().setItems(list), false)
                }
                Kind.BATCH_NESTED_TABLE -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufNestedRow>(src.size)
                    for (o in src) list.add(toNested(o as NestedRow))
                    BatchNestedRowOmniBuf.encode(enc, OmniBufBatchNestedRow().setItems(list), false)
                }
                Kind.BATCH_SIGNAL -> {
                    val src = value as List<*>
                    val list = ArrayList<OmniBufSignal>(src.size)
                    for (o in src) list.add(toSignal(o as Signal))
                    BatchSignalOmniBuf.encode(enc, OmniBufBatchSignal().setItems(list), false)
                }
            }
        }

        private fun toMessage(m: Message): OmniBufMessage =
            OmniBufMessage()
                .setFBool(m.fBool)
                .setFInt32(m.fInt32)
                .setFInt64(m.fInt64)
                .setFFloat64(m.fFloat64)
                .setFString(m.fString)
                .setFBool2(m.fBool2)
                .setFInt322(m.fInt32_2)
                .setFString2(m.fString2)

        private fun fromMessage(m: OmniBufMessage): Message =
            Message(m.fBool, m.fInt32, m.fInt64, m.fFloat64, m.fString, m.fBool2, m.fInt322, m.fString2)

        private fun toDocument(d: Document): OmniBufDocument {
            val items = ArrayList<OmniBufDocumentItem>(d.items.size)
            for (it in d.items) {
                items.add(
                    OmniBufDocumentItem()
                        .setSku(it.sku)
                        .setQty(it.qty)
                        .setPriceMinor(it.priceMinor),
                )
            }
            return OmniBufDocument()
                .setId(d.id)
                .setStatus(d.status)
                .setMeta(
                    OmniBufDocumentMeta()
                        .setRegion(d.meta.region)
                        .setVersion(d.meta.version),
                )
                .setItems(items)
        }

        private fun fromDocument(d: OmniBufDocument): Document {
            val meta =
                if (d.hasMeta()) DocumentMeta(d.meta.region, d.meta.version) else DocumentMeta("", 0)
            val items = d.items.map { DocumentItem(it.sku, it.qty, it.priceMinor) }.toMutableList()
            return Document(d.id, d.status, meta, items)
        }

        private fun toTelemetry(t: Telemetry): OmniBufTelemetry =
            OmniBufTelemetry()
                .setSource(t.source)
                .setTs(t.ts)
                .setTags(ArrayList(t.tags))
                .setValues(ArrayList(t.values))

        private fun fromTelemetry(t: OmniBufTelemetry): Telemetry =
            Telemetry(t.source, t.ts, ArrayList(t.tags), ArrayList(t.values))

        private fun toStrings(s: Strings): OmniBufStrings =
            OmniBufStrings().setItems(ArrayList(s.items))

        private fun fromStrings(s: OmniBufStrings): Strings =
            Strings(ArrayList(s.items))

        private fun toEvent(e: Event): OmniBufEvent {
            val attrs = ArrayList<OmniBufEventAttr>(e.attrs.size)
            for (a in e.attrs) {
                attrs.add(OmniBufEventAttr().setKey(a.key).setValue(a.value))
            }
            return OmniBufEvent()
                .setEventId(e.eventId)
                .setEventType(e.eventType)
                .setOccurredAt(e.occurredAt)
                .setProducer(e.producer)
                .setAttrs(attrs)
        }

        private fun fromEvent(e: OmniBufEvent): Event =
            Event(
                e.eventId,
                e.eventType,
                e.occurredAt,
                e.producer,
                e.attrs.map { EventAttr(it.key, it.value) }.toMutableList(),
            )

        private fun toTable(row: TableRow): OmniBufTable =
            OmniBufTable()
                .setFFloat0(row.fFloat0)
                .setFFloat1(row.fFloat1)
                .setFFloat2(row.fFloat2)
                .setFFloat3(row.fFloat3)
                .setFFloat4(row.fFloat4)
                .setFFloat5(row.fFloat5)
                .setFFloat6(row.fFloat6)
                .setFFloat7(row.fFloat7)
                .setFFloat8(row.fFloat8)
                .setFFloat9(row.fFloat9)
                .setFFloat10(row.fFloat10)
                .setFFloat11(row.fFloat11)
                .setFFloat12(row.fFloat12)
                .setFFloat13(row.fFloat13)
                .setFFloat14(row.fFloat14)
                .setFFloat15(row.fFloat15)
                .setFInt0(row.fInt0)
                .setFInt1(row.fInt1)
                .setFInt2(row.fInt2)
                .setFInt3(row.fInt3)
                .setFStr0(row.fStr0)
                .setFStr1(row.fStr1)

        private fun fromTable(t: OmniBufTable): TableRow =
            TableRow(
                fFloat0 = t.fFloat0,
                fFloat1 = t.fFloat1,
                fFloat2 = t.fFloat2,
                fFloat3 = t.fFloat3,
                fFloat4 = t.fFloat4,
                fFloat5 = t.fFloat5,
                fFloat6 = t.fFloat6,
                fFloat7 = t.fFloat7,
                fFloat8 = t.fFloat8,
                fFloat9 = t.fFloat9,
                fFloat10 = t.fFloat10,
                fFloat11 = t.fFloat11,
                fFloat12 = t.fFloat12,
                fFloat13 = t.fFloat13,
                fFloat14 = t.fFloat14,
                fFloat15 = t.fFloat15,
                fInt0 = t.fInt0,
                fInt1 = t.fInt1,
                fInt2 = t.fInt2,
                fInt3 = t.fInt3,
                fStr0 = t.fStr0,
                fStr1 = t.fStr1,
            )

        private fun toNested(row: NestedRow): OmniBufNestedRow {
            val items = ArrayList<OmniBufNestedItem>(row.items.size)
            for (it in row.items) {
                items.add(
                    OmniBufNestedItem()
                        .setSku(it.sku)
                        .setQty(it.qty)
                        .setPriceMinor(it.priceMinor),
                )
            }
            return OmniBufNestedRow()
                .setId(row.id)
                .setStatus(row.status)
                .setMeta(
                    OmniBufNestedMeta()
                        .setRegion(row.meta.region)
                        .setVersion(row.meta.version),
                )
                .setItems(items)
        }

        private fun fromNested(n: OmniBufNestedRow): NestedRow {
            val meta =
                if (n.hasMeta()) NestedRow.NestedMeta(n.meta.region, n.meta.version) else NestedRow.NestedMeta("", 0)
            val items =
                n.items.map { NestedRow.NestedItem(it.sku, it.qty, it.priceMinor) }.toMutableList()
            return NestedRow(n.id, n.status, meta, items)
        }

        private fun toSignal(row: Signal): OmniBufSignal {
            val legs = ArrayList<OmniBufSignalLeg>(row.legs.size)
            for (leg in row.legs) {
                legs.add(
                    OmniBufSignalLeg()
                        .setLegId(leg.legId)
                        .setLegQty(leg.legQty)
                        .setLegPad(leg.legPad),
                )
            }
            return OmniBufSignal()
                .setSeq(row.seq)
                .setTs(row.ts)
                .setPriceMantissa(row.priceMantissa)
                .setQty(row.qty)
                .setFlags(row.flags)
                .setSymbol(row.symbol)
                .setVenue(row.venue)
                .setLegs(legs)
        }

        private fun fromSignal(s: OmniBufSignal): Signal =
            Signal(
                s.seq,
                s.ts,
                s.priceMantissa,
                s.qty,
                s.flags,
                s.symbol,
                s.venue,
                s.legs.map { Signal.SignalLeg(it.legId, it.legQty, it.legPad) }.toMutableList(),
            )
    }
}
