// OmniBuf C++20 static codecs (schemas/v2/protobuf/benchmark_v2.proto).

#include "bench/serializer.hpp"

#include "benchmark_v2.omnibuf.pb.hpp"

#include <cstddef>
#include <cstdint>
#include <memory>
#include <span>
#include <stdexcept>
#include <string>
#include <variant>
#include <vector>

#ifndef OMNIBUF_VERSION
#define OMNIBUF_VERSION "unknown"
#endif

namespace bench {
namespace {

namespace ob = ::omnibuf_bench::v2;

ob::Message to_ob_message(const Message& m) {
  ob::Message out;
  out.f_bool = m.f_bool;
  out.f_int32 = m.f_int32;
  out.f_int64 = m.f_int64;
  out.f_float64 = m.f_float64;
  out.f_string = m.f_string;
  out.f_bool_2 = m.f_bool_2;
  out.f_int32_2 = m.f_int32_2;
  out.f_string_2 = m.f_string_2;
  return out;
}

Message from_ob_message(const ob::Message& m) {
  Message out;
  out.f_bool = m.f_bool;
  out.f_int32 = m.f_int32;
  out.f_int64 = m.f_int64;
  out.f_float64 = m.f_float64;
  out.f_string = m.f_string;
  out.f_bool_2 = m.f_bool_2;
  out.f_int32_2 = m.f_int32_2;
  out.f_string_2 = m.f_string_2;
  return out;
}

ob::Document to_ob_document(const Document& d) {
  ob::Document out;
  out.id = d.id;
  out.status = d.status;
  out.meta.emplace();
  out.meta->region = d.meta.region;
  out.meta->version = d.meta.version;
  out.items.reserve(d.items.size());
  for (const auto& it : d.items) {
    ob::DocumentItem x;
    x.sku = it.sku;
    x.qty = it.qty;
    x.price_minor = it.price_minor;
    out.items.push_back(std::move(x));
  }
  return out;
}

Document from_ob_document(const ob::Document& d) {
  Document out;
  out.id = d.id;
  out.status = d.status;
  if (d.meta.has_value()) {
    out.meta.region = d.meta->region;
    out.meta.version = d.meta->version;
  }
  out.items.reserve(d.items.size());
  for (const auto& it : d.items) {
    DocumentItem x;
    x.sku = it.sku;
    x.qty = it.qty;
    x.price_minor = it.price_minor;
    out.items.push_back(std::move(x));
  }
  return out;
}

ob::Telemetry to_ob_telemetry(const Telemetry& t) {
  ob::Telemetry out;
  out.source = t.source;
  out.ts = t.ts;
  out.tags = t.tags;
  out.values = t.values;
  return out;
}

Telemetry from_ob_telemetry(const ob::Telemetry& t) {
  Telemetry out;
  out.source = t.source;
  out.ts = t.ts;
  out.tags = t.tags;
  out.values = t.values;
  return out;
}

ob::Strings to_ob_strings(const Strings& s) {
  ob::Strings out;
  out.items = s.items;
  return out;
}

Strings from_ob_strings(const ob::Strings& s) {
  Strings out;
  out.items = s.items;
  return out;
}

ob::Event to_ob_event(const Event& e) {
  ob::Event out;
  out.event_id = e.event_id;
  out.event_type = e.event_type;
  out.occurred_at = e.occurred_at;
  out.producer = e.producer;
  out.attrs.reserve(e.attrs.size());
  for (const auto& a : e.attrs) {
    ob::EventAttr x;
    x.key = a.key;
    x.value = a.value;
    out.attrs.push_back(std::move(x));
  }
  return out;
}

Event from_ob_event(const ob::Event& e) {
  Event out;
  out.event_id = e.event_id;
  out.event_type = e.event_type;
  out.occurred_at = e.occurred_at;
  out.producer = e.producer;
  out.attrs.reserve(e.attrs.size());
  for (const auto& a : e.attrs) {
    EventAttr x;
    x.key = a.key;
    x.value = a.value;
    out.attrs.push_back(std::move(x));
  }
  return out;
}

ob::Table to_ob_table(const Table& t) {
  ob::Table out;
  out.f_float_0 = t.f_float[0];
  out.f_float_1 = t.f_float[1];
  out.f_float_2 = t.f_float[2];
  out.f_float_3 = t.f_float[3];
  out.f_float_4 = t.f_float[4];
  out.f_float_5 = t.f_float[5];
  out.f_float_6 = t.f_float[6];
  out.f_float_7 = t.f_float[7];
  out.f_float_8 = t.f_float[8];
  out.f_float_9 = t.f_float[9];
  out.f_float_10 = t.f_float[10];
  out.f_float_11 = t.f_float[11];
  out.f_float_12 = t.f_float[12];
  out.f_float_13 = t.f_float[13];
  out.f_float_14 = t.f_float[14];
  out.f_float_15 = t.f_float[15];
  out.f_int_0 = t.f_int[0];
  out.f_int_1 = t.f_int[1];
  out.f_int_2 = t.f_int[2];
  out.f_int_3 = t.f_int[3];
  out.f_str_0 = t.f_str_0;
  out.f_str_1 = t.f_str_1;
  return out;
}

Table from_ob_table(const ob::Table& t) {
  Table out;
  out.f_float[0] = t.f_float_0;
  out.f_float[1] = t.f_float_1;
  out.f_float[2] = t.f_float_2;
  out.f_float[3] = t.f_float_3;
  out.f_float[4] = t.f_float_4;
  out.f_float[5] = t.f_float_5;
  out.f_float[6] = t.f_float_6;
  out.f_float[7] = t.f_float_7;
  out.f_float[8] = t.f_float_8;
  out.f_float[9] = t.f_float_9;
  out.f_float[10] = t.f_float_10;
  out.f_float[11] = t.f_float_11;
  out.f_float[12] = t.f_float_12;
  out.f_float[13] = t.f_float_13;
  out.f_float[14] = t.f_float_14;
  out.f_float[15] = t.f_float_15;
  out.f_int[0] = t.f_int_0;
  out.f_int[1] = t.f_int_1;
  out.f_int[2] = t.f_int_2;
  out.f_int[3] = t.f_int_3;
  out.f_str_0 = t.f_str_0;
  out.f_str_1 = t.f_str_1;
  return out;
}

ob::NestedRow to_ob_nested(const NestedRow& row) {
  ob::NestedRow out;
  out.id = row.id;
  out.status = row.status;
  out.meta.emplace();
  out.meta->region = row.meta.region;
  out.meta->version = row.meta.version;
  out.items.reserve(row.items.size());
  for (const auto& it : row.items) {
    ob::NestedItem x;
    x.sku = it.sku;
    x.qty = it.qty;
    x.price_minor = it.price_minor;
    out.items.push_back(std::move(x));
  }
  return out;
}

NestedRow from_ob_nested(const ob::NestedRow& row) {
  NestedRow out;
  out.id = row.id;
  out.status = row.status;
  if (row.meta.has_value()) {
    out.meta.region = row.meta->region;
    out.meta.version = row.meta->version;
  }
  out.items.reserve(row.items.size());
  for (const auto& it : row.items) {
    NestedItem x;
    x.sku = it.sku;
    x.qty = it.qty;
    x.price_minor = it.price_minor;
    out.items.push_back(std::move(x));
  }
  return out;
}

ob::Signal to_ob_signal(const Signal& s) {
  ob::Signal out;
  out.seq = s.seq;
  out.ts = s.ts;
  out.price_mantissa = s.price_mantissa;
  out.qty = s.qty;
  out.flags = s.flags;
  out.symbol = s.symbol;
  out.venue = s.venue;
  out.legs.reserve(s.legs.size());
  for (const auto& leg : s.legs) {
    ob::SignalLeg x;
    x.leg_id = leg.leg_id;
    x.leg_qty = leg.leg_qty;
    x.leg_pad = leg.leg_pad;
    out.legs.push_back(x);
  }
  return out;
}

Signal from_ob_signal(const ob::Signal& s) {
  Signal out;
  out.seq = s.seq;
  out.ts = s.ts;
  out.price_mantissa = s.price_mantissa;
  out.qty = s.qty;
  out.flags = s.flags;
  out.symbol = s.symbol;
  out.venue = s.venue;
  out.legs.reserve(s.legs.size());
  for (const auto& leg : s.legs) {
    SignalLeg x;
    x.leg_id = leg.leg_id;
    x.leg_qty = leg.leg_qty;
    x.leg_pad = leg.leg_pad;
    out.legs.push_back(x);
  }
  return out;
}

using ObMessage = std::variant<
    std::monostate,
    ob::Message, ob::BatchMessage,
    ob::Document, ob::BatchDocument,
    ob::Telemetry, ob::BatchTelemetry,
    ob::Strings, ob::BatchStrings,
    ob::Event, ob::BatchEvent,
    ob::Table, ob::BatchTable,
    ob::NestedRow, ob::BatchNestedRow,
    ob::Signal, ob::BatchSignal>;

template <typename ObBatch, typename DomainT, typename ConvFn>
ObBatch make_batch(const std::vector<DomainT>& rows, ConvFn&& conv) {
  ObBatch batch;
  batch.items.reserve(rows.size());
  for (const auto& row : rows) {
    batch.items.push_back(conv(row));
  }
  return batch;
}

template <typename DomainT, typename ObBatch, typename ConvFn>
std::vector<DomainT> unmake_batch(const ObBatch& batch, ConvFn&& conv) {
  std::vector<DomainT> out;
  out.reserve(batch.items.size());
  for (const auto& item : batch.items) {
    out.push_back(conv(item));
  }
  return out;
}

std::vector<double> project_table_f_float_0(const std::vector<uint8_t>& data, int n) {
  const std::span<const uint8_t> pkt(data);
  if (n <= 1) {
    ob::OpaqueTable row(pkt);
    auto r = row.try_get_f_float_0();
    if (!r) throw std::runtime_error(std::string("omnibuf: ") + r.error());
    return std::vector<double>{r.value()};
  }
  // Single-pass scan over BatchTable field 1 elements, projecting each row via OpaqueTable
  // (avoids OpaqueBatchTable::get_items(i) O(N^2) rescanning).
  std::vector<double> col;
  col.reserve(static_cast<size_t>(n));
  size_t pos = 0;
  while (true) {
    const auto res = ::omnibuf::next_container_field(pkt, pos, 1u, ::omnibuf::WireArray, 2);
    if (!res.valid) throw std::runtime_error("omnibuf: corrupt BatchTable");
    if (!res.matched) break;
    pos = res.next_pos;
    const auto hdr = ::omnibuf::parse_array_header(res.body);
    if (!hdr.ok || hdr.elem_wt != ::omnibuf::WireSubMsg) {
      throw std::runtime_error("omnibuf: invalid BatchTable array header");
    }
    size_t p = hdr.pos;
    for (size_t i = 0; i < hdr.count; ++i) {
      const auto es = ::omnibuf::read_len_slice_fast(res.body, p, 0);
      if (!es) throw std::runtime_error(std::string("omnibuf: ") + es.err);
      ob::OpaqueTable row(es.span());
      auto val = row.try_get_f_float_0();
      if (!val) throw std::runtime_error(std::string("omnibuf: ") + val.error());
      col.push_back(val.value());
      p = es.next_pos;
    }
    if (p != res.body.size()) {
      throw std::runtime_error("omnibuf: trailing bytes in BatchTable");
    }
  }
  return col;
}

class OmniBufSer final : public ISerializer {
 public:
  const char* name() const override { return "omnibuf"; }
  const char* version() const override { return OMNIBUF_VERSION; }
  const char* stream_mode() const override { return "adapted"; }
  const char* native_kind() const override { return "message"; }

  bool supports(const std::string& type_id) const override {
    return type_id == "message" || type_id == "document" || type_id == "telemetry" ||
           type_id == "strings" || type_id == "event" || type_id == "table" ||
           type_id == "table_project" || type_id == "nested_table" || type_id == "signal";
  }

  void prepare(const Fixture& fx) override {
    type_id_ = fx.type_id;
    n_ = fx.instance_count;
    project_ = (type_id_ == "table_project");
    if (n_ > 1) {
      if (type_id_ == "message") {
        prepared_ = make_batch<ob::BatchMessage>(std::get<std::vector<Message>>(fx.value), to_ob_message);
        dst_.emplace<ob::BatchMessage>();
      } else if (type_id_ == "document") {
        prepared_ = make_batch<ob::BatchDocument>(std::get<std::vector<Document>>(fx.value), to_ob_document);
        dst_.emplace<ob::BatchDocument>();
      } else if (type_id_ == "telemetry") {
        prepared_ = make_batch<ob::BatchTelemetry>(std::get<std::vector<Telemetry>>(fx.value), to_ob_telemetry);
        dst_.emplace<ob::BatchTelemetry>();
      } else if (type_id_ == "strings") {
        prepared_ = make_batch<ob::BatchStrings>(std::get<std::vector<Strings>>(fx.value), to_ob_strings);
        dst_.emplace<ob::BatchStrings>();
      } else if (type_id_ == "event") {
        prepared_ = make_batch<ob::BatchEvent>(std::get<std::vector<Event>>(fx.value), to_ob_event);
        dst_.emplace<ob::BatchEvent>();
      } else if (type_id_ == "table" || type_id_ == "table_project") {
        prepared_ = make_batch<ob::BatchTable>(std::get<std::vector<Table>>(fx.value), to_ob_table);
        dst_.emplace<ob::BatchTable>();
      } else if (type_id_ == "nested_table") {
        prepared_ = make_batch<ob::BatchNestedRow>(std::get<std::vector<NestedRow>>(fx.value), to_ob_nested);
        dst_.emplace<ob::BatchNestedRow>();
      } else if (type_id_ == "signal") {
        prepared_ = make_batch<ob::BatchSignal>(std::get<std::vector<Signal>>(fx.value), to_ob_signal);
        dst_.emplace<ob::BatchSignal>();
      } else {
        throw std::runtime_error("omnibuf: unsupported type " + type_id_);
      }
    } else {
      if (type_id_ == "message") {
        prepared_ = to_ob_message(std::get<Message>(fx.value));
        dst_.emplace<ob::Message>();
      } else if (type_id_ == "document") {
        prepared_ = to_ob_document(std::get<Document>(fx.value));
        dst_.emplace<ob::Document>();
      } else if (type_id_ == "telemetry") {
        prepared_ = to_ob_telemetry(std::get<Telemetry>(fx.value));
        dst_.emplace<ob::Telemetry>();
      } else if (type_id_ == "strings") {
        prepared_ = to_ob_strings(std::get<Strings>(fx.value));
        dst_.emplace<ob::Strings>();
      } else if (type_id_ == "event") {
        prepared_ = to_ob_event(std::get<Event>(fx.value));
        dst_.emplace<ob::Event>();
      } else if (type_id_ == "table" || type_id_ == "table_project") {
        prepared_ = to_ob_table(std::get<Table>(fx.value));
        dst_.emplace<ob::Table>();
      } else if (type_id_ == "nested_table") {
        prepared_ = to_ob_nested(std::get<NestedRow>(fx.value));
        dst_.emplace<ob::NestedRow>();
      } else if (type_id_ == "signal") {
        prepared_ = to_ob_signal(std::get<Signal>(fx.value));
        dst_.emplace<ob::Signal>();
      } else {
        throw std::runtime_error("omnibuf: unsupported type " + type_id_);
      }
    }
  }

  std::vector<uint8_t> serialize_bytes(const Fixture&) override {
    std::vector<uint8_t> out;
    marshal_into(out);
    return out;
  }

  Value deserialize_bytes(const std::vector<uint8_t>& data) override {
    if (project_) {
      return project_table_f_float_0(data, n_);
    }
    std::visit(
        [&data](auto& dst) {
          using T = std::decay_t<decltype(dst)>;
          if constexpr (std::is_same_v<T, std::monostate>) {
            throw std::runtime_error("omnibuf: prepare required");
          } else {
            auto res = dst.unmarshal_omnibuf(data);
            if (!res) {
              throw std::runtime_error(std::string("omnibuf: ") + res.error());
            }
          }
        },
        dst_);
    // Timed path ends at generated OmniBuf struct; domain conversion is untimed in to_domain.
    return Message{};
  }

  Value to_domain(Value decoded) override {
    if (project_) {
      return decoded;
    }
    return std::visit(
        [](const auto& dst) -> Value {
          using T = std::decay_t<decltype(dst)>;
          if constexpr (std::is_same_v<T, std::monostate>) {
            return Message{};
          } else if constexpr (std::is_same_v<T, ob::Message>) {
            return from_ob_message(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchMessage>) {
            return unmake_batch<Message>(dst, from_ob_message);
          } else if constexpr (std::is_same_v<T, ob::Document>) {
            return from_ob_document(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchDocument>) {
            return unmake_batch<Document>(dst, from_ob_document);
          } else if constexpr (std::is_same_v<T, ob::Telemetry>) {
            return from_ob_telemetry(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchTelemetry>) {
            return unmake_batch<Telemetry>(dst, from_ob_telemetry);
          } else if constexpr (std::is_same_v<T, ob::Strings>) {
            return from_ob_strings(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchStrings>) {
            return unmake_batch<Strings>(dst, from_ob_strings);
          } else if constexpr (std::is_same_v<T, ob::Event>) {
            return from_ob_event(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchEvent>) {
            return unmake_batch<Event>(dst, from_ob_event);
          } else if constexpr (std::is_same_v<T, ob::Table>) {
            return from_ob_table(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchTable>) {
            return unmake_batch<Table>(dst, from_ob_table);
          } else if constexpr (std::is_same_v<T, ob::NestedRow>) {
            return from_ob_nested(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchNestedRow>) {
            return unmake_batch<NestedRow>(dst, from_ob_nested);
          } else if constexpr (std::is_same_v<T, ob::Signal>) {
            return from_ob_signal(dst);
          } else if constexpr (std::is_same_v<T, ob::BatchSignal>) {
            return unmake_batch<Signal>(dst, from_ob_signal);
          }
        },
        dst_);
  }

 private:
  void marshal_into(std::vector<uint8_t>& out) const {
    std::visit(
        [&out](const auto& msg) {
          using T = std::decay_t<decltype(msg)>;
          if constexpr (std::is_same_v<T, std::monostate>) {
            throw std::runtime_error("omnibuf: prepare required");
          } else {
            msg.marshal_omnibuf_append(out);
          }
        },
        prepared_);
  }

  std::string type_id_;
  int n_ = 1;
  bool project_ = false;
  ObMessage prepared_;
  ObMessage dst_;
};

}  // namespace

SerializerPtr make_omnibuf() { return std::make_unique<OmniBufSer>(); }

}  // namespace bench
