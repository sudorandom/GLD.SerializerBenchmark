package benchmark.serializers;

import benchmark.model.Fixture;
import benchmark.model.v2.Document;
import benchmark.model.v2.Event;
import benchmark.model.v2.Message;
import benchmark.model.v2.NestedRow;
import benchmark.model.v2.Signal;
import benchmark.model.v2.Strings;
import benchmark.model.v2.TableRow;
import benchmark.model.v2.Telemetry;
import benchmark.v2.omnibuf.BatchDocumentOmniBuf;
import benchmark.v2.omnibuf.BatchEventOmniBuf;
import benchmark.v2.omnibuf.BatchMessageOmniBuf;
import benchmark.v2.omnibuf.BatchNestedRowOmniBuf;
import benchmark.v2.omnibuf.BatchSignalOmniBuf;
import benchmark.v2.omnibuf.BatchStringsOmniBuf;
import benchmark.v2.omnibuf.BatchTableOmniBuf;
import benchmark.v2.omnibuf.BatchTelemetryOmniBuf;
import benchmark.v2.omnibuf.DocumentOmniBuf;
import benchmark.v2.omnibuf.EventOmniBuf;
import benchmark.v2.omnibuf.MessageOmniBuf;
import benchmark.v2.omnibuf.NestedRowOmniBuf;
import benchmark.v2.omnibuf.OmniBufBatchDocument;
import benchmark.v2.omnibuf.OmniBufBatchEvent;
import benchmark.v2.omnibuf.OmniBufBatchMessage;
import benchmark.v2.omnibuf.OmniBufBatchNestedRow;
import benchmark.v2.omnibuf.OmniBufBatchSignal;
import benchmark.v2.omnibuf.OmniBufBatchStrings;
import benchmark.v2.omnibuf.OmniBufBatchTable;
import benchmark.v2.omnibuf.OmniBufBatchTelemetry;
import benchmark.v2.omnibuf.OmniBufDocument;
import benchmark.v2.omnibuf.OmniBufDocumentItem;
import benchmark.v2.omnibuf.OmniBufDocumentMeta;
import benchmark.v2.omnibuf.OmniBufEvent;
import benchmark.v2.omnibuf.OmniBufEventAttr;
import benchmark.v2.omnibuf.OmniBufMessage;
import benchmark.v2.omnibuf.OmniBufNestedItem;
import benchmark.v2.omnibuf.OmniBufNestedMeta;
import benchmark.v2.omnibuf.OmniBufNestedRow;
import benchmark.v2.omnibuf.OmniBufSignal;
import benchmark.v2.omnibuf.OmniBufSignalLeg;
import benchmark.v2.omnibuf.OmniBufStrings;
import benchmark.v2.omnibuf.OmniBufTable;
import benchmark.v2.omnibuf.OmniBufTelemetry;
import benchmark.v2.omnibuf.OpaqueBatchTable;
import benchmark.v2.omnibuf.OpaqueTable;
import benchmark.v2.omnibuf.SignalOmniBuf;
import benchmark.v2.omnibuf.StringsOmniBuf;
import benchmark.v2.omnibuf.TableOmniBuf;
import benchmark.v2.omnibuf.TelemetryOmniBuf;
import com.omnibuf.wire.OmniBufEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** OmniBuf static codecs (schemas/v2/protobuf/benchmark_v2.proto). */
public final class OmniBufSer implements BenchSerializer {
  private static final Set<String> SUPPORTED =
      Set.of(
          "message",
          "document",
          "telemetry",
          "strings",
          "event",
          "table",
          "table_project",
          "nested_table",
          "signal");

  private enum Kind {
    MESSAGE,
    BATCH_MESSAGE,
    DOCUMENT,
    BATCH_DOCUMENT,
    TELEMETRY,
    BATCH_TELEMETRY,
    STRINGS,
    BATCH_STRINGS,
    EVENT,
    BATCH_EVENT,
    TABLE,
    BATCH_TABLE,
    TABLE_PROJECT,
    BATCH_TABLE_PROJECT,
    NESTED_TABLE,
    BATCH_NESTED_TABLE,
    SIGNAL,
    BATCH_SIGNAL
  }

  private final OmniBufEncoder encoder = new OmniBufEncoder(4096);
  private Kind kind;
  private Object decodeTarget;

  @Override
  public String name() {
    return "omnibuf";
  }

  @Override
  public String version() {
    return Versions.of(OmniBufEncoder.class);
  }

  @Override
  public String streamMode() {
    return "adapted";
  }

  @Override
  public String nativeKind() {
    return "message";
  }

  @Override
  public boolean supports(String testDataName) {
    return SUPPORTED.contains(testDataName);
  }

  @Override
  public void prepare(Fixture fx) {
    boolean batch = TypeUtil.isList(fx.value);
    kind = resolveKind(fx.name, batch);
    decodeTarget = newDecodeTarget(kind);
  }

  @Override
  public byte[] serializeBytes(Fixture fx) {
    encoder.reset();
    encode(fx.value);
    return encoder.toByteArray();
  }

  @Override
  public Object deserializeBytes(byte[] data) {
    return switch (kind) {
      case MESSAGE -> {
        OmniBufMessage dst = (OmniBufMessage) decodeTarget;
        MessageOmniBuf.unmarshal(data, dst);
        yield fromMessage(dst);
      }
      case BATCH_MESSAGE -> {
        OmniBufBatchMessage dst = (OmniBufBatchMessage) decodeTarget;
        BatchMessageOmniBuf.unmarshal(data, dst);
        List<OmniBufMessage> items = dst.getItems();
        List<Message> out = new ArrayList<>(items.size());
        for (OmniBufMessage m : items) out.add(fromMessage(m));
        yield out;
      }
      case DOCUMENT -> {
        OmniBufDocument dst = (OmniBufDocument) decodeTarget;
        DocumentOmniBuf.unmarshal(data, dst);
        yield fromDocument(dst);
      }
      case BATCH_DOCUMENT -> {
        OmniBufBatchDocument dst = (OmniBufBatchDocument) decodeTarget;
        BatchDocumentOmniBuf.unmarshal(data, dst);
        List<OmniBufDocument> items = dst.getItems();
        List<Document> out = new ArrayList<>(items.size());
        for (OmniBufDocument d : items) out.add(fromDocument(d));
        yield out;
      }
      case TELEMETRY -> {
        OmniBufTelemetry dst = (OmniBufTelemetry) decodeTarget;
        TelemetryOmniBuf.unmarshal(data, dst);
        yield fromTelemetry(dst);
      }
      case BATCH_TELEMETRY -> {
        OmniBufBatchTelemetry dst = (OmniBufBatchTelemetry) decodeTarget;
        BatchTelemetryOmniBuf.unmarshal(data, dst);
        List<OmniBufTelemetry> items = dst.getItems();
        List<Telemetry> out = new ArrayList<>(items.size());
        for (OmniBufTelemetry t : items) out.add(fromTelemetry(t));
        yield out;
      }
      case STRINGS -> {
        OmniBufStrings dst = (OmniBufStrings) decodeTarget;
        StringsOmniBuf.unmarshal(data, dst);
        yield fromStrings(dst);
      }
      case BATCH_STRINGS -> {
        OmniBufBatchStrings dst = (OmniBufBatchStrings) decodeTarget;
        BatchStringsOmniBuf.unmarshal(data, dst);
        List<OmniBufStrings> items = dst.getItems();
        List<Strings> out = new ArrayList<>(items.size());
        for (OmniBufStrings s : items) out.add(fromStrings(s));
        yield out;
      }
      case EVENT -> {
        OmniBufEvent dst = (OmniBufEvent) decodeTarget;
        EventOmniBuf.unmarshal(data, dst);
        yield fromEvent(dst);
      }
      case BATCH_EVENT -> {
        OmniBufBatchEvent dst = (OmniBufBatchEvent) decodeTarget;
        BatchEventOmniBuf.unmarshal(data, dst);
        List<OmniBufEvent> items = dst.getItems();
        List<Event> out = new ArrayList<>(items.size());
        for (OmniBufEvent e : items) out.add(fromEvent(e));
        yield out;
      }
      case TABLE -> {
        OmniBufTable dst = (OmniBufTable) decodeTarget;
        TableOmniBuf.unmarshal(data, dst);
        yield fromTable(dst);
      }
      case BATCH_TABLE -> {
        OmniBufBatchTable dst = (OmniBufBatchTable) decodeTarget;
        BatchTableOmniBuf.unmarshal(data, dst);
        List<OmniBufTable> items = dst.getItems();
        List<TableRow> out = new ArrayList<>(items.size());
        for (OmniBufTable t : items) out.add(fromTable(t));
        yield out;
      }
      case TABLE_PROJECT -> List.of(new OpaqueTable(data).getFFloat0());
      case BATCH_TABLE_PROJECT -> {
        List<OpaqueTable> rows = new OpaqueBatchTable(data).getItems();
        List<Double> out = new ArrayList<>(rows.size());
        for (int i = 0, n = rows.size(); i < n; i++) {
          out.add(rows.get(i).getFFloat0());
        }
        yield out;
      }
      case NESTED_TABLE -> {
        OmniBufNestedRow dst = (OmniBufNestedRow) decodeTarget;
        NestedRowOmniBuf.unmarshal(data, dst);
        yield fromNested(dst);
      }
      case BATCH_NESTED_TABLE -> {
        OmniBufBatchNestedRow dst = (OmniBufBatchNestedRow) decodeTarget;
        BatchNestedRowOmniBuf.unmarshal(data, dst);
        List<OmniBufNestedRow> items = dst.getItems();
        List<NestedRow> out = new ArrayList<>(items.size());
        for (OmniBufNestedRow n : items) out.add(fromNested(n));
        yield out;
      }
      case SIGNAL -> {
        OmniBufSignal dst = (OmniBufSignal) decodeTarget;
        SignalOmniBuf.unmarshal(data, dst);
        yield fromSignal(dst);
      }
      case BATCH_SIGNAL -> {
        OmniBufBatchSignal dst = (OmniBufBatchSignal) decodeTarget;
        BatchSignalOmniBuf.unmarshal(data, dst);
        List<OmniBufSignal> items = dst.getItems();
        List<Signal> out = new ArrayList<>(items.size());
        for (OmniBufSignal s : items) out.add(fromSignal(s));
        yield out;
      }
    };
  }

  private void encode(Object value) {
    switch (kind) {
      case MESSAGE -> MessageOmniBuf.encode(encoder, toMessage((Message) value), false);
      case BATCH_MESSAGE -> {
        List<?> list = (List<?>) value;
        List<OmniBufMessage> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toMessage((Message) o));
        BatchMessageOmniBuf.encode(encoder, new OmniBufBatchMessage().setItems(items), false);
      }
      case DOCUMENT -> DocumentOmniBuf.encode(encoder, toDocument((Document) value), false);
      case BATCH_DOCUMENT -> {
        List<?> list = (List<?>) value;
        List<OmniBufDocument> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toDocument((Document) o));
        BatchDocumentOmniBuf.encode(encoder, new OmniBufBatchDocument().setItems(items), false);
      }
      case TELEMETRY -> TelemetryOmniBuf.encode(encoder, toTelemetry((Telemetry) value), false);
      case BATCH_TELEMETRY -> {
        List<?> list = (List<?>) value;
        List<OmniBufTelemetry> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toTelemetry((Telemetry) o));
        BatchTelemetryOmniBuf.encode(encoder, new OmniBufBatchTelemetry().setItems(items), false);
      }
      case STRINGS -> StringsOmniBuf.encode(encoder, toStrings((Strings) value), false);
      case BATCH_STRINGS -> {
        List<?> list = (List<?>) value;
        List<OmniBufStrings> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toStrings((Strings) o));
        BatchStringsOmniBuf.encode(encoder, new OmniBufBatchStrings().setItems(items), false);
      }
      case EVENT -> EventOmniBuf.encode(encoder, toEvent((Event) value), false);
      case BATCH_EVENT -> {
        List<?> list = (List<?>) value;
        List<OmniBufEvent> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toEvent((Event) o));
        BatchEventOmniBuf.encode(encoder, new OmniBufBatchEvent().setItems(items), false);
      }
      case TABLE, TABLE_PROJECT -> TableOmniBuf.encode(encoder, toTable((TableRow) value), false);
      case BATCH_TABLE, BATCH_TABLE_PROJECT -> {
        List<?> list = (List<?>) value;
        List<OmniBufTable> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toTable((TableRow) o));
        BatchTableOmniBuf.encode(encoder, new OmniBufBatchTable().setItems(items), false);
      }
      case NESTED_TABLE -> NestedRowOmniBuf.encode(encoder, toNested((NestedRow) value), false);
      case BATCH_NESTED_TABLE -> {
        List<?> list = (List<?>) value;
        List<OmniBufNestedRow> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toNested((NestedRow) o));
        BatchNestedRowOmniBuf.encode(encoder, new OmniBufBatchNestedRow().setItems(items), false);
      }
      case SIGNAL -> SignalOmniBuf.encode(encoder, toSignal((Signal) value), false);
      case BATCH_SIGNAL -> {
        List<?> list = (List<?>) value;
        List<OmniBufSignal> items = new ArrayList<>(list.size());
        for (Object o : list) items.add(toSignal((Signal) o));
        BatchSignalOmniBuf.encode(encoder, new OmniBufBatchSignal().setItems(items), false);
      }
    }
  }

  private static Kind resolveKind(String typeId, boolean batch) {
    return switch (typeId) {
      case "message" -> batch ? Kind.BATCH_MESSAGE : Kind.MESSAGE;
      case "document" -> batch ? Kind.BATCH_DOCUMENT : Kind.DOCUMENT;
      case "telemetry" -> batch ? Kind.BATCH_TELEMETRY : Kind.TELEMETRY;
      case "strings" -> batch ? Kind.BATCH_STRINGS : Kind.STRINGS;
      case "event" -> batch ? Kind.BATCH_EVENT : Kind.EVENT;
      case "table" -> batch ? Kind.BATCH_TABLE : Kind.TABLE;
      case "table_project" -> batch ? Kind.BATCH_TABLE_PROJECT : Kind.TABLE_PROJECT;
      case "nested_table" -> batch ? Kind.BATCH_NESTED_TABLE : Kind.NESTED_TABLE;
      case "signal" -> batch ? Kind.BATCH_SIGNAL : Kind.SIGNAL;
      default -> throw new IllegalArgumentException(typeId);
    };
  }

  private static Object newDecodeTarget(Kind kind) {
    return switch (kind) {
      case MESSAGE -> new OmniBufMessage();
      case BATCH_MESSAGE -> new OmniBufBatchMessage();
      case DOCUMENT -> new OmniBufDocument();
      case BATCH_DOCUMENT -> new OmniBufBatchDocument();
      case TELEMETRY -> new OmniBufTelemetry();
      case BATCH_TELEMETRY -> new OmniBufBatchTelemetry();
      case STRINGS -> new OmniBufStrings();
      case BATCH_STRINGS -> new OmniBufBatchStrings();
      case EVENT -> new OmniBufEvent();
      case BATCH_EVENT -> new OmniBufBatchEvent();
      case TABLE -> new OmniBufTable();
      case BATCH_TABLE -> new OmniBufBatchTable();
      case TABLE_PROJECT, BATCH_TABLE_PROJECT -> null;
      case NESTED_TABLE -> new OmniBufNestedRow();
      case BATCH_NESTED_TABLE -> new OmniBufBatchNestedRow();
      case SIGNAL -> new OmniBufSignal();
      case BATCH_SIGNAL -> new OmniBufBatchSignal();
    };
  }

  private static OmniBufMessage toMessage(Message m) {
    return new OmniBufMessage()
        .setFBool(m.fBool)
        .setFInt32(m.fInt32)
        .setFInt64(m.fInt64)
        .setFFloat64(m.fFloat64)
        .setFString(nullToEmpty(m.fString))
        .setFBool2(m.fBool2)
        .setFInt322(m.fInt32_2)
        .setFString2(nullToEmpty(m.fString2));
  }

  private static Message fromMessage(OmniBufMessage m) {
    return new Message(
        m.getFBool(),
        m.getFInt32(),
        m.getFInt64(),
        m.getFFloat64(),
        m.getFString(),
        m.getFBool2(),
        m.getFInt322(),
        m.getFString2());
  }

  private static OmniBufDocument toDocument(Document d) {
    OmniBufDocument out = new OmniBufDocument().setId(nullToEmpty(d.id)).setStatus(d.status);
    if (d.meta != null) {
      out.setMeta(
          new OmniBufDocumentMeta()
              .setRegion(nullToEmpty(d.meta.region))
              .setVersion(d.meta.version));
    }
    if (d.items != null && !d.items.isEmpty()) {
      List<OmniBufDocumentItem> items = new ArrayList<>(d.items.size());
      for (Document.DocumentItem it : d.items) {
        items.add(
            new OmniBufDocumentItem()
                .setSku(nullToEmpty(it.sku))
                .setQty(it.qty)
                .setPriceMinor(it.priceMinor));
      }
      out.setItems(items);
    }
    return out;
  }

  private static Document fromDocument(OmniBufDocument d) {
    Document.DocumentMeta meta =
        d.hasMeta()
            ? new Document.DocumentMeta(d.getMeta().getRegion(), d.getMeta().getVersion())
            : new Document.DocumentMeta("", 0);
    List<OmniBufDocumentItem> rawItems = d.getItems();
    List<Document.DocumentItem> items = new ArrayList<>(rawItems.size());
    for (OmniBufDocumentItem it : rawItems) {
      items.add(new Document.DocumentItem(it.getSku(), it.getQty(), it.getPriceMinor()));
    }
    return new Document(d.getId(), d.getStatus(), meta, items);
  }

  private static OmniBufTelemetry toTelemetry(Telemetry t) {
    OmniBufTelemetry out = new OmniBufTelemetry().setSource(nullToEmpty(t.source)).setTs(t.ts);
    if (t.tags != null) {
      out.setTags(new ArrayList<>(t.tags));
    }
    if (t.values != null && t.values.length > 0) {
      List<Double> vals = new ArrayList<>(t.values.length);
      for (double v : t.values) vals.add(v);
      out.setValues(vals);
    }
    return out;
  }

  private static Telemetry fromTelemetry(OmniBufTelemetry t) {
    List<Double> rawVals = t.getValues();
    double[] vals = new double[rawVals.size()];
    for (int i = 0; i < vals.length; i++) vals[i] = rawVals.get(i);
    return new Telemetry(t.getSource(), t.getTs(), new ArrayList<>(t.getTags()), vals);
  }

  private static OmniBufStrings toStrings(Strings s) {
    OmniBufStrings out = new OmniBufStrings();
    if (s.items != null) {
      out.setItems(new ArrayList<>(s.items));
    }
    return out;
  }

  private static Strings fromStrings(OmniBufStrings s) {
    return new Strings(new ArrayList<>(s.getItems()));
  }

  private static OmniBufEvent toEvent(Event e) {
    OmniBufEvent out =
        new OmniBufEvent()
            .setEventId(nullToEmpty(e.eventId))
            .setEventType(nullToEmpty(e.eventType))
            .setOccurredAt(e.occurredAt)
            .setProducer(nullToEmpty(e.producer));
    if (e.attrs != null && !e.attrs.isEmpty()) {
      List<OmniBufEventAttr> attrs = new ArrayList<>(e.attrs.size());
      for (Event.EventAttr a : e.attrs) {
        attrs.add(new OmniBufEventAttr().setKey(nullToEmpty(a.key)).setValue(nullToEmpty(a.value)));
      }
      out.setAttrs(attrs);
    }
    return out;
  }

  private static Event fromEvent(OmniBufEvent e) {
    List<OmniBufEventAttr> rawAttrs = e.getAttrs();
    List<Event.EventAttr> attrs = new ArrayList<>(rawAttrs.size());
    for (OmniBufEventAttr a : rawAttrs) {
      attrs.add(new Event.EventAttr(a.getKey(), a.getValue()));
    }
    return new Event(e.getEventId(), e.getEventType(), e.getOccurredAt(), e.getProducer(), attrs);
  }

  private static OmniBufTable toTable(TableRow row) {
    return new OmniBufTable()
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
        .setFStr0(nullToEmpty(row.fStr0))
        .setFStr1(nullToEmpty(row.fStr1));
  }

  private static TableRow fromTable(OmniBufTable t) {
    TableRow row = new TableRow();
    row.fFloat0 = t.getFFloat0();
    row.fFloat1 = t.getFFloat1();
    row.fFloat2 = t.getFFloat2();
    row.fFloat3 = t.getFFloat3();
    row.fFloat4 = t.getFFloat4();
    row.fFloat5 = t.getFFloat5();
    row.fFloat6 = t.getFFloat6();
    row.fFloat7 = t.getFFloat7();
    row.fFloat8 = t.getFFloat8();
    row.fFloat9 = t.getFFloat9();
    row.fFloat10 = t.getFFloat10();
    row.fFloat11 = t.getFFloat11();
    row.fFloat12 = t.getFFloat12();
    row.fFloat13 = t.getFFloat13();
    row.fFloat14 = t.getFFloat14();
    row.fFloat15 = t.getFFloat15();
    row.fInt0 = t.getFInt0();
    row.fInt1 = t.getFInt1();
    row.fInt2 = t.getFInt2();
    row.fInt3 = t.getFInt3();
    row.fStr0 = t.getFStr0();
    row.fStr1 = t.getFStr1();
    return row;
  }

  private static OmniBufNestedRow toNested(NestedRow row) {
    OmniBufNestedRow out =
        new OmniBufNestedRow().setId(nullToEmpty(row.id)).setStatus(row.status);
    if (row.meta != null) {
      out.setMeta(
          new OmniBufNestedMeta()
              .setRegion(nullToEmpty(row.meta.region))
              .setVersion(row.meta.version));
    }
    if (row.items != null && !row.items.isEmpty()) {
      List<OmniBufNestedItem> items = new ArrayList<>(row.items.size());
      for (NestedRow.NestedItem it : row.items) {
        items.add(
            new OmniBufNestedItem()
                .setSku(nullToEmpty(it.sku))
                .setQty(it.qty)
                .setPriceMinor(it.priceMinor));
      }
      out.setItems(items);
    }
    return out;
  }

  private static NestedRow fromNested(OmniBufNestedRow n) {
    NestedRow.NestedMeta meta =
        n.hasMeta()
            ? new NestedRow.NestedMeta(n.getMeta().getRegion(), n.getMeta().getVersion())
            : new NestedRow.NestedMeta("", 0);
    List<OmniBufNestedItem> rawItems = n.getItems();
    List<NestedRow.NestedItem> items = new ArrayList<>(rawItems.size());
    for (OmniBufNestedItem it : rawItems) {
      items.add(new NestedRow.NestedItem(it.getSku(), it.getQty(), it.getPriceMinor()));
    }
    return new NestedRow(n.getId(), n.getStatus(), meta, items);
  }

  private static OmniBufSignal toSignal(Signal s) {
    OmniBufSignal out =
        new OmniBufSignal()
            .setSeq(s.seq)
            .setTs(s.ts)
            .setPriceMantissa(s.priceMantissa)
            .setQty(s.qty)
            .setFlags(s.flags)
            .setSymbol(nullToEmpty(s.symbol))
            .setVenue(nullToEmpty(s.venue));
    if (s.legs != null && !s.legs.isEmpty()) {
      List<OmniBufSignalLeg> legs = new ArrayList<>(s.legs.size());
      for (Signal.SignalLeg leg : s.legs) {
        legs.add(
            new OmniBufSignalLeg()
                .setLegId(leg.legId)
                .setLegQty(leg.legQty)
                .setLegPad(leg.legPad));
      }
      out.setLegs(legs);
    }
    return out;
  }

  private static Signal fromSignal(OmniBufSignal s) {
    List<OmniBufSignalLeg> rawLegs = s.getLegs();
    List<Signal.SignalLeg> legs = new ArrayList<>(rawLegs.size());
    for (OmniBufSignalLeg leg : rawLegs) {
      legs.add(new Signal.SignalLeg(leg.getLegId(), leg.getLegQty(), leg.getLegPad()));
    }
    return new Signal(
        s.getSeq(),
        s.getTs(),
        s.getPriceMantissa(),
        s.getQty(),
        s.getFlags(),
        s.getSymbol(),
        s.getVenue(),
        legs);
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }
}
