// OmniBuf static codecs (@omnibuf/node, compact wire format).
import { Encoder, readFloat64, readSubMsgArrDecoded } from '@omnibuf/node';
import * as pb from '../generated/benchmark_v2_omnibuf.pb.js';
import { peerSupports, pkgVersion } from './common.js';

const SINGLE_CODECS = {
  message: { encode: pb.encodeMessage, decode: pb.decodeMessage },
  document: { encode: pb.encodeDocument, decode: pb.decodeDocument },
  telemetry: { encode: pb.encodeTelemetry, decode: pb.decodeTelemetry },
  strings: { encode: pb.encodeStrings, decode: pb.decodeStrings },
  event: { encode: pb.encodeEvent, decode: pb.decodeEvent },
  table: { encode: pb.encodeTable, decode: pb.decodeTable },
  table_project: { encode: pb.encodeTable, decode: pb.decodeTable },
  nested_table: { encode: pb.encodeNestedRow, decode: pb.decodeNestedRow },
  signal: { encode: pb.encodeSignal, decode: pb.decodeSignal },
};

const BATCH_CODECS = {
  message: { create: pb.createBatchMessage, encode: pb.encodeBatchMessage, decode: pb.decodeBatchMessage },
  document: { create: pb.createBatchDocument, encode: pb.encodeBatchDocument, decode: pb.decodeBatchDocument },
  telemetry: { create: pb.createBatchTelemetry, encode: pb.encodeBatchTelemetry, decode: pb.decodeBatchTelemetry },
  strings: { create: pb.createBatchStrings, encode: pb.encodeBatchStrings, decode: pb.decodeBatchStrings },
  event: { create: pb.createBatchEvent, encode: pb.encodeBatchEvent, decode: pb.decodeBatchEvent },
  table: { create: pb.createBatchTable, encode: pb.encodeBatchTable, decode: pb.decodeBatchTable },
  table_project: { create: pb.createBatchTable, encode: pb.encodeBatchTable, decode: pb.decodeBatchTable },
  nested_table: { create: pb.createBatchNestedRow, encode: pb.encodeBatchNestedRow, decode: pb.decodeBatchNestedRow },
  signal: { create: pb.createBatchSignal, encode: pb.encodeBatchSignal, decode: pb.decodeBatchSignal },
};

function toOmniItem(dataName, v) {
  switch (dataName) {
    case 'message': {
      const m = pb.createMessage();
      m.fBool = Boolean(v.f_bool);
      m.fInt32 = v.f_int32 | 0;
      m.fInt64 = Number(v.f_int64);
      m.fFloat64 = Number(v.f_float64);
      m.fString = String(v.f_string ?? '');
      m.fBool2 = Boolean(v.f_bool_2);
      m.fInt322 = v.f_int32_2 | 0;
      m.fString2 = String(v.f_string_2 ?? '');
      return m;
    }
    case 'document':
    case 'nested_table': {
      const isNested = dataName === 'nested_table';
      const m = isNested ? pb.createNestedRow() : pb.createDocument();
      m.id = String(v.id ?? '');
      m.status = v.status | 0;
      const meta = isNested ? pb.createNestedMeta() : pb.createDocumentMeta();
      meta.region = String(v.meta?.region ?? '');
      meta.version = (v.meta?.version ?? 0) | 0;
      m.meta = meta;
      const srcItems = v.items || [];
      const createItem = isNested ? pb.createNestedItem : pb.createDocumentItem;
      const items = new Array(srcItems.length);
      for (let i = 0; i < srcItems.length; i++) {
        const src = srcItems[i];
        const it = createItem();
        it.sku = String(src.sku ?? '');
        it.qty = src.qty | 0;
        it.priceMinor = Number(src.price_minor);
        items[i] = it;
      }
      m.items = items;
      return m;
    }
    case 'telemetry': {
      const m = pb.createTelemetry();
      m.source = String(v.source ?? '');
      m.ts = Number(v.ts);
      m.tags = (v.tags || []).map(String);
      m.values = (v.values || []).map(Number);
      return m;
    }
    case 'strings': {
      const m = pb.createStrings();
      m.items = (v.items || []).map(String);
      return m;
    }
    case 'event': {
      const m = pb.createEvent();
      m.eventId = String(v.event_id ?? '');
      m.eventType = String(v.event_type ?? '');
      m.occurredAt = Number(v.occurred_at);
      m.producer = String(v.producer ?? '');
      const srcAttrs = v.attrs || [];
      const attrs = new Array(srcAttrs.length);
      for (let i = 0; i < srcAttrs.length; i++) {
        const src = srcAttrs[i];
        const a = pb.createEventAttr();
        a.key = String(src.key ?? '');
        a.value = String(src.value ?? '');
        attrs[i] = a;
      }
      m.attrs = attrs;
      return m;
    }
    case 'table':
    case 'table_project': {
      const m = pb.createTable();
      m.fFloat0 = Number(v.f_float_0);
      m.fFloat1 = Number(v.f_float_1);
      m.fFloat2 = Number(v.f_float_2);
      m.fFloat3 = Number(v.f_float_3);
      m.fFloat4 = Number(v.f_float_4);
      m.fFloat5 = Number(v.f_float_5);
      m.fFloat6 = Number(v.f_float_6);
      m.fFloat7 = Number(v.f_float_7);
      m.fFloat8 = Number(v.f_float_8);
      m.fFloat9 = Number(v.f_float_9);
      m.fFloat10 = Number(v.f_float_10);
      m.fFloat11 = Number(v.f_float_11);
      m.fFloat12 = Number(v.f_float_12);
      m.fFloat13 = Number(v.f_float_13);
      m.fFloat14 = Number(v.f_float_14);
      m.fFloat15 = Number(v.f_float_15);
      m.fInt0 = Number(v.f_int_0);
      m.fInt1 = Number(v.f_int_1);
      m.fInt2 = Number(v.f_int_2);
      m.fInt3 = Number(v.f_int_3);
      m.fStr0 = String(v.f_str_0 ?? '');
      m.fStr1 = String(v.f_str_1 ?? '');
      return m;
    }
    case 'signal': {
      const m = pb.createSignal();
      m.seq = Number(v.seq);
      m.ts = Number(v.ts);
      m.priceMantissa = Number(v.price_mantissa);
      m.qty = v.qty | 0;
      m.flags = v.flags | 0;
      m.symbol = String(v.symbol ?? '');
      m.venue = String(v.venue ?? '');
      const srcLegs = v.legs || [];
      const legs = new Array(srcLegs.length);
      for (let i = 0; i < srcLegs.length; i++) {
        const src = srcLegs[i];
        const leg = pb.createSignalLeg();
        leg.legId = Number(src.leg_id);
        leg.legQty = src.leg_qty | 0;
        leg.legPad = src.leg_pad | 0;
        legs[i] = leg;
      }
      m.legs = legs;
      return m;
    }
    default:
      throw new Error(`omnibuf: no mapping for ${dataName}`);
  }
}

function fromOmniItem(dataName, m) {
  switch (dataName) {
    case 'message':
      return {
        f_bool: Boolean(m.fBool),
        f_int32: Number(m.fInt32),
        f_int64: Number(m.fInt64),
        f_float64: Number(m.fFloat64),
        f_string: String(m.fString ?? ''),
        f_bool_2: Boolean(m.fBool2),
        f_int32_2: Number(m.fInt322),
        f_string_2: String(m.fString2 ?? ''),
      };
    case 'document':
    case 'nested_table':
      return {
        id: String(m.id ?? ''),
        status: Number(m.status),
        meta: {
          region: String(m.meta?.region ?? ''),
          version: Number(m.meta?.version ?? 0),
        },
        items: (m.items || []).map((it) => ({
          sku: String(it.sku ?? ''),
          qty: Number(it.qty),
          price_minor: Number(it.priceMinor),
        })),
      };
    case 'telemetry':
      return {
        source: String(m.source ?? ''),
        ts: Number(m.ts),
        tags: (m.tags || []).map(String),
        values: (m.values || []).map(Number),
      };
    case 'strings':
      return { items: (m.items || []).map(String) };
    case 'event':
      return {
        event_id: String(m.eventId ?? ''),
        event_type: String(m.eventType ?? ''),
        occurred_at: Number(m.occurredAt),
        producer: String(m.producer ?? ''),
        attrs: (m.attrs || []).map((a) => ({
          key: String(a.key ?? ''),
          value: String(a.value ?? ''),
        })),
      };
    case 'table':
    case 'table_project':
      return {
        f_float_0: Number(m.fFloat0),
        f_float_1: Number(m.fFloat1),
        f_float_2: Number(m.fFloat2),
        f_float_3: Number(m.fFloat3),
        f_float_4: Number(m.fFloat4),
        f_float_5: Number(m.fFloat5),
        f_float_6: Number(m.fFloat6),
        f_float_7: Number(m.fFloat7),
        f_float_8: Number(m.fFloat8),
        f_float_9: Number(m.fFloat9),
        f_float_10: Number(m.fFloat10),
        f_float_11: Number(m.fFloat11),
        f_float_12: Number(m.fFloat12),
        f_float_13: Number(m.fFloat13),
        f_float_14: Number(m.fFloat14),
        f_float_15: Number(m.fFloat15),
        f_int_0: Number(m.fInt0),
        f_int_1: Number(m.fInt1),
        f_int_2: Number(m.fInt2),
        f_int_3: Number(m.fInt3),
        f_str_0: String(m.fStr0 ?? ''),
        f_str_1: String(m.fStr1 ?? ''),
      };
    case 'signal':
      return {
        seq: Number(m.seq),
        ts: Number(m.ts),
        price_mantissa: Number(m.priceMantissa),
        qty: Number(m.qty),
        flags: Number(m.flags),
        symbol: String(m.symbol ?? ''),
        venue: String(m.venue ?? ''),
        legs: (m.legs || []).map((leg) => ({
          leg_id: Number(leg.legId),
          leg_qty: Number(leg.legQty),
          leg_pad: Number(leg.legPad),
        })),
      };
    default:
      return m;
  }
}

function projectSingleFFloat0(u8) {
  return [readFloat64(u8, 1)[0]];
}

function projectBatchFFloat0(u8) {
  return readSubMsgArrDecoded(u8, 1, (sub) => readFloat64(sub, 1)[0])[0];
}

const encoder = new Encoder(4096);
let activeDataName = null;
let activeIsBatch = false;
let activeEncode = null;
let activeDecode = null;
let preparedMsg = null;

export const omnibufSer = {
  name: 'omnibuf',
  version: pkgVersion('@omnibuf/core') || pkgVersion('@omnibuf/node'),
  category: 'schema',
  supports: peerSupports,
  prepare(dataName, value) {
    activeDataName = dataName;
    activeIsBatch = Array.isArray(value);
    if (activeIsBatch) {
      const entry = BATCH_CODECS[dataName];
      if (!entry) throw new Error(`omnibuf: no batch codec for ${dataName}`);
      const batch = entry.create();
      const items = new Array(value.length);
      for (let i = 0; i < value.length; i++) items[i] = toOmniItem(dataName, value[i]);
      batch.items = items;
      preparedMsg = batch;
      activeEncode = entry.encode;
      activeDecode = dataName === 'table_project' ? projectBatchFFloat0 : entry.decode;
    } else {
      const entry = SINGLE_CODECS[dataName];
      if (!entry) throw new Error(`omnibuf: no single codec for ${dataName}`);
      preparedMsg = toOmniItem(dataName, value);
      activeEncode = entry.encode;
      activeDecode = dataName === 'table_project' ? projectSingleFFloat0 : entry.decode;
    }
    // Warm encoder slab capacity outside the timed loop.
    encoder.reset();
    activeEncode(encoder, preparedMsg);
  },
  serialize(_value) {
    encoder.reset();
    activeEncode(encoder, preparedMsg);
    return encoder.bytesView();
  },
  deserialize(buf) {
    const u8 =
      buf instanceof Uint8Array
        ? buf
        : new Uint8Array(buf.buffer, buf.byteOffset, buf.byteLength);
    return activeDecode(u8);
  },
  toDomain(decoded) {
    if (activeDataName === 'table_project') return decoded;
    if (activeIsBatch) {
      const items = decoded.items || [];
      const out = new Array(items.length);
      for (let i = 0; i < items.length; i++) out[i] = fromOmniItem(activeDataName, items[i]);
      return out;
    }
    return fromOmniItem(activeDataName, decoded);
  },
};

export function omnibufSerializers() {
  return [omnibufSer];
}
