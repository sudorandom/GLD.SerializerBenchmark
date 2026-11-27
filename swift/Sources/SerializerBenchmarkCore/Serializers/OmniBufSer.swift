import Foundation
import OmniBuf
import OmniBufGen

// MARK: - OmniBuf (https://github.com/omnibuf/omnibuf-swift)

public final class OmniBufSerializer: BenchSerializer {
    public let name = "omnibuf"
    public var version: String {
        PackageVersions.version(for: "omnibuf-swift", fallback: "0.1.4")
    }
    public let streamMode: StreamMode = .adapted
    public let nativeKind: NativeKind = .message

    private var writer = OmniBufWriter(initialCapacity: 4096)
    private var outputData = Data()
    private var encodeFn: ((inout OmniBufWriter, inout Data, Fixture) throws -> Data)?
    private var decodeFn: ((Data) throws -> Any)?

    public init() {}

    public func supports(testDataName: String) -> Bool {
        switch testDataName {
        case "message", "document", "telemetry", "strings", "event":
            return true
        default:
            return false
        }
    }

    public func prepare(_ fixture: Fixture) throws {
        let batch = fixture.instanceCount > 1
        switch (fixture.name, batch) {
        case ("message", false):
            bind(
                toNative: { try OmniBufBridge.message($0) },
                toDomain: { OmniBufBridge.messageDomain($0) }
            )
        case ("message", true):
            bind(
                toNative: { try OmniBufBridge.batchMessage($0) },
                toDomain: { OmniBufBridge.batchMessageDomain($0) }
            )
        case ("document", false):
            bind(
                toNative: { try OmniBufBridge.document($0) },
                toDomain: { OmniBufBridge.documentDomain($0) }
            )
        case ("document", true):
            bind(
                toNative: { try OmniBufBridge.batchDocument($0) },
                toDomain: { OmniBufBridge.batchDocumentDomain($0) }
            )
        case ("telemetry", false):
            bind(
                toNative: { try OmniBufBridge.telemetry($0) },
                toDomain: { OmniBufBridge.telemetryDomain($0) }
            )
        case ("telemetry", true):
            bind(
                toNative: { try OmniBufBridge.batchTelemetry($0) },
                toDomain: { OmniBufBridge.batchTelemetryDomain($0) }
            )
        case ("strings", false):
            bind(
                toNative: { try OmniBufBridge.strings($0) },
                toDomain: { OmniBufBridge.stringsDomain($0) }
            )
        case ("strings", true):
            bind(
                toNative: { try OmniBufBridge.batchStrings($0) },
                toDomain: { OmniBufBridge.batchStringsDomain($0) }
            )
        case ("event", false):
            bind(
                toNative: { try OmniBufBridge.event($0) },
                toDomain: { OmniBufBridge.eventDomain($0) }
            )
        case ("event", true):
            bind(
                toNative: { try OmniBufBridge.batchEvent($0) },
                toDomain: { OmniBufBridge.batchEventDomain($0) }
            )
        default:
            throw BenchError.unknownType(fixture.name)
        }
    }

    public func serializeBytes(_ fixture: Fixture) throws -> Data {
        guard let encodeFn else { throw BenchError.prepareRequired }
        return try encodeFn(&writer, &outputData, fixture)
    }

    public func deserializeBytes(_ data: Data) throws -> Any {
        guard let decodeFn else { throw BenchError.prepareRequired }
        return try decodeFn(data)
    }

    private func bind<T: OmniBufMessage>(
        toNative: @escaping (Fixture) throws -> T,
        toDomain: @escaping (T) -> Any
    ) {
        let maxBytes = DecodeOptions.default.maxMessageBytes

        encodeFn = { writer, outputData, fx in
            let msg = try toNative(fx)
            writer.truncate(to: 0)
            try msg.encodeOmniBufBody(into: &writer, options: .default)
            outputData.removeAll(keepingCapacity: true)
            writer.withUnsafeBytes { buf in
                if let base = buf.baseAddress, !buf.isEmpty {
                    outputData.append(base, count: buf.count)
                }
            }
            return outputData
        }

        decodeFn = { data in
            guard data.count <= maxBytes else {
                throw OmniBufError.allocationLimitExceeded(
                    "message length \(data.count) exceeds maxMessageBytes \(maxBytes)"
                )
            }
            var msg = T()
            var budget = DecodeBudget(options: .default)
            try data.withUnsafeBytes { raw in
                let buf = raw.bindMemory(to: UInt8.self)
                try msg.mergeOmniBuf(from: buf, depth: 0, budget: &budget)
            }
            return toDomain(msg)
        }
    }
}

/// Domain <-> generated OmniBufGen conversions.
enum OmniBufBridge {
    // MARK: Message

    static func message(_ fx: Fixture) throws -> OmniBufGen.Message {
        guard let m = fx.value as? Message else {
            throw BenchError.unsupported("omnibuf message")
        }
        return messageValue(m)
    }

    static func batchMessage(_ fx: Fixture) throws -> OmniBufGen.BatchMessage {
        guard let rows = fx.value as? [Message] else {
            throw BenchError.unsupported("omnibuf batch message")
        }
        return OmniBufGen.BatchMessage(items: rows.map(messageValue))
    }

    static func messageValue(_ m: Message) -> OmniBufGen.Message {
        OmniBufGen.Message(
            fBool: m.f_bool,
            fInt32: m.f_int32,
            fInt64: m.f_int64,
            fFloat64: m.f_float64,
            fString: m.f_string,
            fBool2: m.f_bool_2,
            fInt322: m.f_int32_2,
            fString2: m.f_string_2
        )
    }

    static func messageDomain(_ p: OmniBufGen.Message) -> Message {
        Message(
            f_bool: p.fBool,
            f_int32: p.fInt32,
            f_int64: p.fInt64,
            f_float64: p.fFloat64,
            f_string: p.fString,
            f_bool_2: p.fBool2,
            f_int32_2: p.fInt322,
            f_string_2: p.fString2
        )
    }

    static func batchMessageDomain(_ b: OmniBufGen.BatchMessage) -> [Message] {
        b.items.map(messageDomain)
    }

    // MARK: Document

    static func document(_ fx: Fixture) throws -> OmniBufGen.Document {
        guard let d = fx.value as? Document else {
            throw BenchError.unsupported("omnibuf document")
        }
        return documentValue(d)
    }

    static func batchDocument(_ fx: Fixture) throws -> OmniBufGen.BatchDocument {
        guard let rows = fx.value as? [Document] else {
            throw BenchError.unsupported("omnibuf batch document")
        }
        return OmniBufGen.BatchDocument(items: rows.map(documentValue))
    }

    static func documentValue(_ d: Document) -> OmniBufGen.Document {
        OmniBufGen.Document(
            id: d.id,
            status: d.status,
            meta: OmniBufGen.DocumentMeta(region: d.meta.region, version: d.meta.version),
            items: d.items.map {
                OmniBufGen.DocumentItem(sku: $0.sku, qty: $0.qty, priceMinor: $0.price_minor)
            }
        )
    }

    static func documentDomain(_ p: OmniBufGen.Document) -> Document {
        Document(
            id: p.id,
            status: p.status,
            meta: DocumentMeta(region: p.meta?.region ?? "", version: p.meta?.version ?? 0),
            items: p.items.map {
                DocumentItem(sku: $0.sku, qty: $0.qty, price_minor: $0.priceMinor)
            }
        )
    }

    static func batchDocumentDomain(_ b: OmniBufGen.BatchDocument) -> [Document] {
        b.items.map(documentDomain)
    }

    // MARK: Telemetry

    static func telemetry(_ fx: Fixture) throws -> OmniBufGen.Telemetry {
        guard let t = fx.value as? Telemetry else {
            throw BenchError.unsupported("omnibuf telemetry")
        }
        return telemetryValue(t)
    }

    static func batchTelemetry(_ fx: Fixture) throws -> OmniBufGen.BatchTelemetry {
        guard let rows = fx.value as? [Telemetry] else {
            throw BenchError.unsupported("omnibuf batch telemetry")
        }
        return OmniBufGen.BatchTelemetry(items: rows.map(telemetryValue))
    }

    static func telemetryValue(_ t: Telemetry) -> OmniBufGen.Telemetry {
        OmniBufGen.Telemetry(source: t.source, ts: t.ts, tags: t.tags, values: t.values)
    }

    static func telemetryDomain(_ p: OmniBufGen.Telemetry) -> Telemetry {
        Telemetry(source: p.source, ts: p.ts, tags: p.tags, values: p.values)
    }

    static func batchTelemetryDomain(_ b: OmniBufGen.BatchTelemetry) -> [Telemetry] {
        b.items.map(telemetryDomain)
    }

    // MARK: Strings

    static func strings(_ fx: Fixture) throws -> OmniBufGen.Strings {
        guard let s = fx.value as? Strings else {
            throw BenchError.unsupported("omnibuf strings")
        }
        return stringsValue(s)
    }

    static func batchStrings(_ fx: Fixture) throws -> OmniBufGen.BatchStrings {
        guard let rows = fx.value as? [Strings] else {
            throw BenchError.unsupported("omnibuf batch strings")
        }
        return OmniBufGen.BatchStrings(items: rows.map(stringsValue))
    }

    static func stringsValue(_ s: Strings) -> OmniBufGen.Strings {
        OmniBufGen.Strings(items: s.items)
    }

    static func stringsDomain(_ p: OmniBufGen.Strings) -> Strings {
        Strings(items: p.items)
    }

    static func batchStringsDomain(_ b: OmniBufGen.BatchStrings) -> [Strings] {
        b.items.map(stringsDomain)
    }

    // MARK: Event

    static func event(_ fx: Fixture) throws -> OmniBufGen.Event {
        guard let e = fx.value as? Event else {
            throw BenchError.unsupported("omnibuf event")
        }
        return eventValue(e)
    }

    static func batchEvent(_ fx: Fixture) throws -> OmniBufGen.BatchEvent {
        guard let rows = fx.value as? [Event] else {
            throw BenchError.unsupported("omnibuf batch event")
        }
        return OmniBufGen.BatchEvent(items: rows.map(eventValue))
    }

    static func eventValue(_ e: Event) -> OmniBufGen.Event {
        OmniBufGen.Event(
            eventId: e.event_id,
            eventType: e.event_type,
            occurredAt: e.occurred_at,
            producer: e.producer,
            attrs: e.attrs.map { OmniBufGen.EventAttr(key: $0.key, value: $0.value) }
        )
    }

    static func eventDomain(_ p: OmniBufGen.Event) -> Event {
        Event(
            event_id: p.eventId,
            event_type: p.eventType,
            occurred_at: p.occurredAt,
            producer: p.producer,
            attrs: p.attrs.map { EventAttr(key: $0.key, value: $0.value) }
        )
    }

    static func batchEventDomain(_ b: OmniBufGen.BatchEvent) -> [Event] {
        b.items.map(eventDomain)
    }
}
