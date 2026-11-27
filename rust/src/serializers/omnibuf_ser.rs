//! OmniBuf path: schemas/v2/protobuf/benchmark_v2.proto.

use crate::data::{
    Document, DocumentItem, DocumentMeta, Event, EventAttr, Fixture, Message, NestedItem,
    NestedMeta, NestedRow, Signal, SignalLeg, Strings, TableRow, Telemetry,
};
use anyhow::{anyhow, Result};
use omnibuf::Message as OmniMessage;
use std::io::Write;

use super::{ver, BenchSerializer, NativeKind};

#[path = "../../gen/omnibuf/benchmark_v2_omnibuf.rs"]
#[allow(dead_code, clippy::all)]
pub mod ob;

fn message_to_ob(m: &Message) -> ob::Message {
    ob::Message {
        f_bool: m.f_bool,
        f_int32: m.f_int32,
        f_int64: m.f_int64,
        f_float64: m.f_float64,
        f_string: m.f_string.clone(),
        f_bool_2: m.f_bool_2,
        f_int32_2: m.f_int32_2,
        f_string_2: m.f_string_2.clone(),
    }
}
fn message_from_ob(m: ob::Message) -> Message {
    Message {
        f_bool: m.f_bool,
        f_int32: m.f_int32,
        f_int64: m.f_int64,
        f_float64: m.f_float64,
        f_string: m.f_string,
        f_bool_2: m.f_bool_2,
        f_int32_2: m.f_int32_2,
        f_string_2: m.f_string_2,
    }
}

fn document_to_ob(d: &Document) -> ob::Document {
    ob::Document {
        id: d.id.clone(),
        status: d.status,
        meta: Some(ob::DocumentMeta {
            region: d.meta.region.clone(),
            version: d.meta.version,
        }),
        items: d
            .items
            .iter()
            .map(|it| ob::DocumentItem {
                sku: it.sku.clone(),
                qty: it.qty,
                price_minor: it.price_minor,
            })
            .collect(),
    }
}
fn document_from_ob(d: ob::Document) -> Document {
    let meta = d.meta.unwrap_or_default();
    Document {
        id: d.id,
        status: d.status,
        meta: DocumentMeta {
            region: meta.region,
            version: meta.version,
        },
        items: d
            .items
            .into_iter()
            .map(|it| DocumentItem {
                sku: it.sku,
                qty: it.qty,
                price_minor: it.price_minor,
            })
            .collect(),
    }
}

fn telemetry_to_ob(t: &Telemetry) -> ob::Telemetry {
    ob::Telemetry {
        source: t.source.clone(),
        ts: t.ts,
        tags: t.tags.clone(),
        values: t.values.clone(),
    }
}
fn telemetry_from_ob(t: ob::Telemetry) -> Telemetry {
    Telemetry {
        source: t.source,
        ts: t.ts,
        tags: t.tags,
        values: t.values,
    }
}

fn strings_to_ob(s: &Strings) -> ob::Strings {
    ob::Strings {
        items: s.items.clone(),
    }
}
fn strings_from_ob(s: ob::Strings) -> Strings {
    Strings { items: s.items }
}

fn event_to_ob(e: &Event) -> ob::Event {
    ob::Event {
        event_id: e.event_id.clone(),
        event_type: e.event_type.clone(),
        occurred_at: e.occurred_at,
        producer: e.producer.clone(),
        attrs: e
            .attrs
            .iter()
            .map(|a| ob::EventAttr {
                key: a.key.clone(),
                value: a.value.clone(),
            })
            .collect(),
    }
}
fn event_from_ob(e: ob::Event) -> Event {
    Event {
        event_id: e.event_id,
        event_type: e.event_type,
        occurred_at: e.occurred_at,
        producer: e.producer,
        attrs: e
            .attrs
            .into_iter()
            .map(|a| EventAttr {
                key: a.key,
                value: a.value,
            })
            .collect(),
    }
}

fn table_to_ob(row: &TableRow) -> ob::Table {
    ob::Table {
        f_float_0: row.f_float_0,
        f_float_1: row.f_float_1,
        f_float_2: row.f_float_2,
        f_float_3: row.f_float_3,
        f_float_4: row.f_float_4,
        f_float_5: row.f_float_5,
        f_float_6: row.f_float_6,
        f_float_7: row.f_float_7,
        f_float_8: row.f_float_8,
        f_float_9: row.f_float_9,
        f_float_10: row.f_float_10,
        f_float_11: row.f_float_11,
        f_float_12: row.f_float_12,
        f_float_13: row.f_float_13,
        f_float_14: row.f_float_14,
        f_float_15: row.f_float_15,
        f_int_0: row.f_int_0,
        f_int_1: row.f_int_1,
        f_int_2: row.f_int_2,
        f_int_3: row.f_int_3,
        f_str_0: row.f_str_0.clone(),
        f_str_1: row.f_str_1.clone(),
    }
}
fn table_from_ob(row: ob::Table) -> TableRow {
    TableRow {
        f_float_0: row.f_float_0,
        f_float_1: row.f_float_1,
        f_float_2: row.f_float_2,
        f_float_3: row.f_float_3,
        f_float_4: row.f_float_4,
        f_float_5: row.f_float_5,
        f_float_6: row.f_float_6,
        f_float_7: row.f_float_7,
        f_float_8: row.f_float_8,
        f_float_9: row.f_float_9,
        f_float_10: row.f_float_10,
        f_float_11: row.f_float_11,
        f_float_12: row.f_float_12,
        f_float_13: row.f_float_13,
        f_float_14: row.f_float_14,
        f_float_15: row.f_float_15,
        f_int_0: row.f_int_0,
        f_int_1: row.f_int_1,
        f_int_2: row.f_int_2,
        f_int_3: row.f_int_3,
        f_str_0: row.f_str_0,
        f_str_1: row.f_str_1,
    }
}

fn nested_to_ob(row: &NestedRow) -> ob::NestedRow {
    ob::NestedRow {
        id: row.id.clone(),
        status: row.status,
        meta: Some(ob::NestedMeta {
            region: row.meta.region.clone(),
            version: row.meta.version,
        }),
        items: row
            .items
            .iter()
            .map(|it| ob::NestedItem {
                sku: it.sku.clone(),
                qty: it.qty,
                price_minor: it.price_minor,
            })
            .collect(),
    }
}
fn nested_from_ob(row: ob::NestedRow) -> NestedRow {
    let meta = row.meta.unwrap_or_default();
    NestedRow {
        id: row.id,
        status: row.status,
        meta: NestedMeta {
            region: meta.region,
            version: meta.version,
        },
        items: row
            .items
            .into_iter()
            .map(|it| NestedItem {
                sku: it.sku,
                qty: it.qty,
                price_minor: it.price_minor,
            })
            .collect(),
    }
}

fn signal_to_ob(sig: &Signal) -> ob::Signal {
    ob::Signal {
        seq: sig.seq,
        ts: sig.ts,
        price_mantissa: sig.price_mantissa,
        qty: sig.qty,
        flags: sig.flags,
        symbol: sig.symbol.clone(),
        venue: sig.venue.clone(),
        legs: sig
            .legs
            .iter()
            .map(|leg| ob::SignalLeg {
                leg_id: leg.leg_id,
                leg_qty: leg.leg_qty,
                leg_pad: leg.leg_pad,
            })
            .collect(),
    }
}
fn signal_from_ob(sig: ob::Signal) -> Signal {
    Signal {
        seq: sig.seq,
        ts: sig.ts,
        price_mantissa: sig.price_mantissa,
        qty: sig.qty,
        flags: sig.flags,
        symbol: sig.symbol,
        venue: sig.venue,
        legs: sig
            .legs
            .into_iter()
            .map(|leg| SignalLeg {
                leg_id: leg.leg_id,
                leg_qty: leg.leg_qty,
                leg_pad: leg.leg_pad,
            })
            .collect(),
    }
}

fn tables_from(fixtures: &[Fixture]) -> Result<Vec<ob::Table>> {
    fixtures
        .iter()
        .map(|f| match f {
            Fixture::Table(r) | Fixture::TableProject(r) => Ok(table_to_ob(r)),
            _ => Err(anyhow!("omnibuf: expected table row")),
        })
        .collect()
}

enum PreparedOb {
    Message(Vec<ob::Message>),
    Document(Vec<ob::Document>),
    Telemetry(Vec<ob::Telemetry>),
    Strings(Vec<ob::Strings>),
    Event(Vec<ob::Event>),
    Table(Vec<ob::Table>),
    TableBatch(ob::BatchTable),
    Nested(Vec<ob::NestedRow>),
    NestedBatch(ob::BatchNestedRow),
    Signal(Vec<ob::Signal>),
    SignalBatch(ob::BatchSignal),
}

pub struct OmniBufSer {
    kind: &'static str,
    batch: bool,
    prepared: PreparedOb,
    enc_i: usize,
}

impl Default for OmniBufSer {
    fn default() -> Self {
        Self {
            kind: "message",
            batch: false,
            prepared: PreparedOb::Message(Vec::new()),
            enc_i: 0,
        }
    }
}

#[inline]
fn encode_msg<M: OmniMessage>(msg: &M, out: &mut Vec<u8>) -> Result<()> {
    // Default compact wire format (self_describing = false, deterministic = false).
    msg.encode_omnibuf_append(out, false)?;
    Ok(())
}

impl BenchSerializer for OmniBufSer {
    fn name(&self) -> &'static str {
        "omnibuf"
    }
    fn version(&self) -> &'static str {
        ver("omnibuf")
    }
    fn native_kind(&self) -> NativeKind {
        NativeKind::Message
    }
    fn supports(&self, test_data_name: &str) -> bool {
        matches!(
            test_data_name,
            "message"
                | "document"
                | "telemetry"
                | "strings"
                | "event"
                | "table"
                | "table_project"
                | "nested_table"
                | "signal"
        )
    }
    fn prepare(&mut self, fixture: &Fixture) -> Result<()> {
        self.prepare_many(std::slice::from_ref(fixture))
    }
    fn prepare_many(&mut self, fixtures: &[Fixture]) -> Result<()> {
        if fixtures.is_empty() {
            return Err(anyhow!("omnibuf: empty cell"));
        }
        self.kind = fixtures[0].name();
        self.enc_i = 0;
        self.batch = fixtures.len() > 1 && crate::data::is_columnar_id(self.kind);
        self.prepared = match fixtures[0] {
            Fixture::Message(_) => PreparedOb::Message(
                fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::Message(m) = f else {
                            return Err(anyhow!("omnibuf: expected Message"));
                        };
                        Ok(message_to_ob(m))
                    })
                    .collect::<Result<Vec<_>>>()?,
            ),
            Fixture::Document(_) => PreparedOb::Document(
                fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::Document(d) = f else {
                            return Err(anyhow!("omnibuf: expected Document"));
                        };
                        Ok(document_to_ob(d))
                    })
                    .collect::<Result<Vec<_>>>()?,
            ),
            Fixture::Telemetry(_) => PreparedOb::Telemetry(
                fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::Telemetry(t) = f else {
                            return Err(anyhow!("omnibuf: expected Telemetry"));
                        };
                        Ok(telemetry_to_ob(t))
                    })
                    .collect::<Result<Vec<_>>>()?,
            ),
            Fixture::Strings(_) => PreparedOb::Strings(
                fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::Strings(s) = f else {
                            return Err(anyhow!("omnibuf: expected Strings"));
                        };
                        Ok(strings_to_ob(s))
                    })
                    .collect::<Result<Vec<_>>>()?,
            ),
            Fixture::Event(_) => PreparedOb::Event(
                fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::Event(e) = f else {
                            return Err(anyhow!("omnibuf: expected Event"));
                        };
                        Ok(event_to_ob(e))
                    })
                    .collect::<Result<Vec<_>>>()?,
            ),
            Fixture::Table(_) | Fixture::TableProject(_) => {
                let tables = tables_from(fixtures)?;
                if self.batch {
                    PreparedOb::TableBatch(ob::BatchTable { items: tables })
                } else {
                    PreparedOb::Table(tables)
                }
            }
            Fixture::NestedTable(_) => {
                let rows = fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::NestedTable(row) = f else {
                            return Err(anyhow!("omnibuf: expected NestedTable"));
                        };
                        Ok(nested_to_ob(row))
                    })
                    .collect::<Result<Vec<_>>>()?;
                if self.batch {
                    PreparedOb::NestedBatch(ob::BatchNestedRow { items: rows })
                } else {
                    PreparedOb::Nested(rows)
                }
            }
            Fixture::Signal(_) => {
                let rows = fixtures
                    .iter()
                    .map(|f| {
                        let Fixture::Signal(row) = f else {
                            return Err(anyhow!("omnibuf: expected Signal"));
                        };
                        Ok(signal_to_ob(row))
                    })
                    .collect::<Result<Vec<_>>>()?;
                if self.batch {
                    PreparedOb::SignalBatch(ob::BatchSignal { items: rows })
                } else {
                    PreparedOb::Signal(rows)
                }
            }
            Fixture::Rows(_)
            | Fixture::Projected(_)
            | Fixture::Graph(_)
            | Fixture::Grid(_)
            | Fixture::Tensor(_)
            | Fixture::TensorView(_)
            | Fixture::Prompt(_)
            | Fixture::Shard(_)
            | Fixture::Request(_) => {
                return Err(anyhow!("omnibuf: unexpected fixture shape"));
            }
        };
        Ok(())
    }
    fn serialize_fixtures(&mut self, fixtures: &[Fixture], out: &mut Vec<u8>) -> Result<()> {
        if crate::data::is_columnar_id(self.kind) {
            if fixtures.is_empty() {
                return Err(anyhow!("omnibuf: empty cell"));
            }
            self.begin_cell_encode();
            return self.serialize_into(&fixtures[0], out);
        }
        if fixtures.len() == 1 {
            return self.serialize_into(&fixtures[0], out);
        }
        let rows = Fixture::Rows(fixtures.to_vec());
        self.serialize_into(&rows, out)
    }
    fn begin_cell_encode(&mut self) {
        self.enc_i = 0;
    }
    fn serialize_stream(&mut self, fixture: &Fixture, w: &mut dyn Write) -> Result<usize> {
        // Each stream call is its own pass. The bytes cell path resets in
        // begin_cell_encode / serialize_fixtures; this matches that for N=1.
        self.begin_cell_encode();
        let mut data = Vec::with_capacity(4096);
        self.serialize_into(fixture, &mut data)?;
        w.write_all(&data)?;
        Ok(data.len())
    }
    fn serialize_into(&mut self, _fixture: &Fixture, out: &mut Vec<u8>) -> Result<()> {
        let i = self.enc_i;
        self.enc_i += 1;
        match &self.prepared {
            PreparedOb::Message(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::Document(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::Telemetry(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::Strings(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::Event(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::Table(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::TableBatch(msg) => encode_msg(msg, out),
            PreparedOb::Nested(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::NestedBatch(msg) => encode_msg(msg, out),
            PreparedOb::Signal(v) => {
                encode_msg(v.get(i).ok_or_else(|| anyhow!("omnibuf: encode index"))?, out)
            }
            PreparedOb::SignalBatch(msg) => encode_msg(msg, out),
        }
    }
    fn deserialize_bytes(&mut self, data: &[u8]) -> Result<Fixture> {
        match self.kind {
            "message" => {
                let m = ob::Message::from_omnibuf(data)?;
                Ok(Fixture::Message(message_from_ob(m)))
            }
            "document" => {
                let m = ob::Document::from_omnibuf(data)?;
                Ok(Fixture::Document(document_from_ob(m)))
            }
            "telemetry" => {
                let m = ob::Telemetry::from_omnibuf(data)?;
                Ok(Fixture::Telemetry(telemetry_from_ob(m)))
            }
            "strings" => {
                let m = ob::Strings::from_omnibuf(data)?;
                Ok(Fixture::Strings(strings_from_ob(m)))
            }
            "event" => {
                let m = ob::Event::from_omnibuf(data)?;
                Ok(Fixture::Event(event_from_ob(m)))
            }
            "table" => {
                if self.batch {
                    let m = ob::BatchTable::from_omnibuf(data)?;
                    let rows = m
                        .items
                        .into_iter()
                        .map(|t| Fixture::Table(table_from_ob(t)))
                        .collect();
                    Ok(Fixture::Rows(rows))
                } else {
                    let m = ob::Table::from_omnibuf(data)?;
                    Ok(Fixture::Table(table_from_ob(m)))
                }
            }
            "table_project" => {
                let values = if self.batch {
                    let view = ob::OpaqueBatchTable::new(data);
                    view.iter_items().map(|row| row.get_f_float_0()).collect()
                } else {
                    let view = ob::OpaqueTable::new(data);
                    vec![view.get_f_float_0()]
                };
                Ok(Fixture::Projected(values))
            }
            "nested_table" => {
                if self.batch {
                    let m = ob::BatchNestedRow::from_omnibuf(data)?;
                    let rows = m
                        .items
                        .into_iter()
                        .map(|row| Fixture::NestedTable(nested_from_ob(row)))
                        .collect();
                    Ok(Fixture::Rows(rows))
                } else {
                    let m = ob::NestedRow::from_omnibuf(data)?;
                    Ok(Fixture::NestedTable(nested_from_ob(m)))
                }
            }
            "signal" => {
                if self.batch {
                    let m = ob::BatchSignal::from_omnibuf(data)?;
                    let rows = m
                        .items
                        .into_iter()
                        .map(|row| Fixture::Signal(signal_from_ob(row)))
                        .collect();
                    Ok(Fixture::Rows(rows))
                } else {
                    let m = ob::Signal::from_omnibuf(data)?;
                    Ok(Fixture::Signal(signal_from_ob(m)))
                }
            }
            other => Err(anyhow!("omnibuf: unsupported kind {other}")),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::data::{check_cell_fidelity, make_one, TypeConfig};

    #[test]
    fn omnibuf_roundtrips_all_suite_and_columnar_types() {
        let cfg = TypeConfig::default();
        let mut ser = OmniBufSer::default();
        assert_eq!(ser.name(), "omnibuf");
        assert!(!ser.version().is_empty(), "omnibuf version missing");
        assert!(!ser.supports("graph"));
        assert!(!ser.supports("grid"));
        assert!(!ser.supports("tensor"));

        for kind in [
            "message",
            "document",
            "telemetry",
            "strings",
            "event",
            "table",
            "table_project",
            "nested_table",
            "signal",
        ] {
            assert!(ser.supports(kind));
            let one = make_one(kind, 42, 0, &cfg).unwrap();
            ser.prepare(&one).unwrap();
            let mut buf = Vec::with_capacity(4096);
            ser.serialize_into(&one, &mut buf).unwrap();
            assert!(!buf.is_empty(), "empty bytes for {kind}");
            let got = ser.deserialize_bytes(&buf).unwrap();
            check_cell_fidelity(kind, std::slice::from_ref(&one), &[got]).unwrap();

            // Stream roundtrip for N=1
            let mut stream = Vec::new();
            let n = ser.serialize_stream(&one, &mut stream).unwrap();
            assert_eq!(n, buf.len());
            let mut cur = std::io::Cursor::new(stream);
            let got_stream = ser.deserialize_stream(&mut cur).unwrap();
            check_cell_fidelity(kind, std::slice::from_ref(&one), &[got_stream]).unwrap();
        }

        // Columnar N=100 batches (bytes + stream)
        for kind in ["table", "table_project", "nested_table", "signal"] {
            let many: Vec<Fixture> = (0..100).map(|i| make_one(kind, 7, i, &cfg).unwrap()).collect();
            ser.prepare_many(&many).unwrap();
            let mut buf = Vec::with_capacity(4096);
            ser.serialize_fixtures(&many, &mut buf).unwrap();
            assert!(!buf.is_empty(), "empty batch for {kind}");
            let got = ser.deserialize_bytes(&buf).unwrap();
            check_cell_fidelity(kind, &many, &[got]).unwrap();

            // Reuse buffer on second encode
            let cap = buf.capacity();
            buf.clear();
            ser.serialize_fixtures(&many, &mut buf).unwrap();
            assert!(buf.capacity() >= cap);
            let got2 = ser.deserialize_bytes(&buf).unwrap();
            check_cell_fidelity(kind, &many, &[got2]).unwrap();

            let mut stream = Vec::new();
            let n = ser.serialize_fixtures_stream(&many, &mut stream).unwrap();
            assert_eq!(n, buf.len());
            let mut cur = std::io::Cursor::new(stream);
            let got_s = ser.deserialize_stream(&mut cur).unwrap();
            check_cell_fidelity(kind, &many, &[got_s]).unwrap();
        }
    }
}
