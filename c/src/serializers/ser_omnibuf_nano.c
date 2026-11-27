#include "ser_common.h"
#include "ob_common.h"
#include "ob_decode.h"
#include "ob_encode.h"
#include "ob_varint.h"
#include "benchmark_v2.ob.h"

#include <stdlib.h>
#include <string.h>

/* OmniBuf Nano (C99 static-bound codec; N>1 uses Batch<Type> wire framing). */

typedef union {
    benchmark_v2_Message message;
    benchmark_v2_Document document;
    benchmark_v2_Telemetry telemetry;
    benchmark_v2_Strings strings;
    benchmark_v2_Event event;
} ob_any_msg_t;

static ob_any_msg_t *g_prep_msgs = NULL;
static size_t g_prep_cap = 0;
static int g_prep_n = 0;
static test_data_kind_t g_prep_kind = TD_COUNT;
static const test_fixture_t *g_prep_fx = NULL;

static ob_any_msg_t g_scratch_msg;
static ob_any_msg_t g_dec_msg;

static inline void copy_cstr(char *dst, size_t cap, const char *src) {
    size_t n = src ? strlen(src) : 0u;
    if (n >= cap) n = cap - 1u;
    if (n) memcpy(dst, src, n);
    dst[n] = '\0';
}

static const ob_msgdesc_t *desc_for_kind(test_data_kind_t kind) {
    switch (kind) {
        case TD_MESSAGE:   return &benchmark_v2_Message_msg;
        case TD_DOCUMENT:  return &benchmark_v2_Document_msg;
        case TD_TELEMETRY: return &benchmark_v2_Telemetry_msg;
        case TD_STRINGS:   return &benchmark_v2_Strings_msg;
        case TD_EVENT:     return &benchmark_v2_Event_msg;
        default:           return NULL;
    }
}

static int fixture_to_ob(const test_fixture_t *fx, ob_any_msg_t *dst) {
    if (!fx || !dst) return -1;
    switch (fx->kind) {
        case TD_MESSAGE: {
            const message_t *s = &fx->message;
            benchmark_v2_Message *m = &dst->message;
            memset(m, 0, sizeof(*m));
            m->f_bool = s->f_bool;
            m->f_int32 = s->f_int32;
            m->f_int64 = s->f_int64;
            m->f_float64 = s->f_float64;
            copy_cstr(m->f_string, sizeof(m->f_string), s->f_string);
            m->f_bool_2 = s->f_bool_2;
            m->f_int32_2 = s->f_int32_2;
            copy_cstr(m->f_string_2, sizeof(m->f_string_2), s->f_string_2);
            return 0;
        }
        case TD_DOCUMENT: {
            const document_t *s = &fx->document;
            benchmark_v2_Document *d = &dst->document;
            if (s->item_count < 0 || s->item_count > V2_MAX_CHILDREN) return -1;
            memset(d, 0, sizeof(*d));
            copy_cstr(d->id, sizeof(d->id), s->id);
            d->status = s->status;
            d->has_meta = true;
            copy_cstr(d->meta.region, sizeof(d->meta.region), s->meta.region);
            d->meta.version = s->meta.version;
            d->items_count = (uint16_t)s->item_count;
            for (int i = 0; i < s->item_count; i++) {
                copy_cstr(d->items[i].sku, sizeof(d->items[i].sku), s->items[i].sku);
                d->items[i].qty = s->items[i].qty;
                d->items[i].price_minor = s->items[i].price_minor;
            }
            return 0;
        }
        case TD_TELEMETRY: {
            const telemetry_t *s = &fx->telemetry;
            benchmark_v2_Telemetry *t = &dst->telemetry;
            if (s->tag_count < 0 || s->tag_count > V2_MAX_TAGS) return -1;
            if (s->value_count < 0 || s->value_count > V2_MAX_POINTS) return -1;
            memset(t, 0, sizeof(*t));
            copy_cstr(t->source, sizeof(t->source), s->source);
            t->ts = s->ts;
            t->tags_count = (uint16_t)s->tag_count;
            for (int i = 0; i < s->tag_count; i++) {
                copy_cstr(t->tags[i], sizeof(t->tags[i]), s->tags[i]);
            }
            t->values_count = (uint16_t)s->value_count;
            if (s->value_count > 0) {
                memcpy(t->values, s->values, (size_t)s->value_count * sizeof(double));
            }
            return 0;
        }
        case TD_STRINGS: {
            const strings_t *s = &fx->strings;
            benchmark_v2_Strings *st = &dst->strings;
            if (s->count < 0 || s->count > V2_MAX_STRINGS) return -1;
            memset(st, 0, sizeof(*st));
            st->items_count = (uint16_t)s->count;
            for (int i = 0; i < s->count; i++) {
                copy_cstr(st->items[i], sizeof(st->items[i]), s->items[i]);
            }
            return 0;
        }
        case TD_EVENT: {
            const event_t *s = &fx->event;
            benchmark_v2_Event *e = &dst->event;
            if (s->attr_count < 0 || s->attr_count > V2_MAX_ATTRS) return -1;
            memset(e, 0, sizeof(*e));
            copy_cstr(e->event_id, sizeof(e->event_id), s->event_id);
            copy_cstr(e->event_type, sizeof(e->event_type), s->event_type);
            e->occurred_at = s->occurred_at;
            copy_cstr(e->producer, sizeof(e->producer), s->producer);
            e->attrs_count = (uint16_t)s->attr_count;
            for (int i = 0; i < s->attr_count; i++) {
                copy_cstr(e->attrs[i].key, sizeof(e->attrs[i].key), s->attrs[i].key);
                copy_cstr(e->attrs[i].value, sizeof(e->attrs[i].value), s->attrs[i].value);
            }
            return 0;
        }
        default:
            return -1;
    }
}

static int ob_to_fixture(test_data_kind_t kind, const ob_any_msg_t *src, test_fixture_t *out) {
    out->kind = kind;
    out->name = test_data_name(kind);
    out->batch_n = 1;
    out->batch = NULL;
    switch (kind) {
        case TD_MESSAGE: {
            const benchmark_v2_Message *m = &src->message;
            message_t *o = &out->message;
            o->f_bool = m->f_bool;
            o->f_int32 = m->f_int32;
            o->f_int64 = m->f_int64;
            o->f_float64 = m->f_float64;
            copy_cstr(o->f_string, sizeof(o->f_string), m->f_string);
            o->f_bool_2 = m->f_bool_2;
            o->f_int32_2 = m->f_int32_2;
            copy_cstr(o->f_string_2, sizeof(o->f_string_2), m->f_string_2);
            return 0;
        }
        case TD_DOCUMENT: {
            const benchmark_v2_Document *d = &src->document;
            document_t *o = &out->document;
            if (d->items_count > V2_MAX_CHILDREN) return -1;
            copy_cstr(o->id, sizeof(o->id), d->id);
            o->status = d->status;
            if (d->has_meta) {
                copy_cstr(o->meta.region, sizeof(o->meta.region), d->meta.region);
                o->meta.version = d->meta.version;
            } else {
                o->meta.region[0] = '\0';
                o->meta.version = 0;
            }
            o->item_count = (int)d->items_count;
            for (uint16_t i = 0; i < d->items_count; i++) {
                copy_cstr(o->items[i].sku, sizeof(o->items[i].sku), d->items[i].sku);
                o->items[i].qty = d->items[i].qty;
                o->items[i].price_minor = d->items[i].price_minor;
            }
            return 0;
        }
        case TD_TELEMETRY: {
            const benchmark_v2_Telemetry *t = &src->telemetry;
            telemetry_t *o = &out->telemetry;
            if (t->tags_count > V2_MAX_TAGS || t->values_count > V2_MAX_POINTS) return -1;
            copy_cstr(o->source, sizeof(o->source), t->source);
            o->ts = t->ts;
            o->tag_count = (int)t->tags_count;
            for (uint16_t i = 0; i < t->tags_count; i++) {
                copy_cstr(o->tags[i], sizeof(o->tags[i]), t->tags[i]);
            }
            o->value_count = (int)t->values_count;
            if (t->values_count > 0) {
                memcpy(o->values, t->values, (size_t)t->values_count * sizeof(double));
            }
            return 0;
        }
        case TD_STRINGS: {
            const benchmark_v2_Strings *st = &src->strings;
            strings_t *o = &out->strings;
            if (st->items_count > V2_MAX_STRINGS) return -1;
            o->count = (int)st->items_count;
            for (uint16_t i = 0; i < st->items_count; i++) {
                copy_cstr(o->items[i], sizeof(o->items[i]), st->items[i]);
            }
            return 0;
        }
        case TD_EVENT: {
            const benchmark_v2_Event *e = &src->event;
            event_t *o = &out->event;
            if (e->attrs_count > V2_MAX_ATTRS) return -1;
            copy_cstr(o->event_id, sizeof(o->event_id), e->event_id);
            copy_cstr(o->event_type, sizeof(o->event_type), e->event_type);
            o->occurred_at = e->occurred_at;
            copy_cstr(o->producer, sizeof(o->producer), e->producer);
            o->attr_count = (int)e->attrs_count;
            for (uint16_t i = 0; i < e->attrs_count; i++) {
                copy_cstr(o->attrs[i].key, sizeof(o->attrs[i].key), e->attrs[i].key);
                copy_cstr(o->attrs[i].value, sizeof(o->attrs[i].value), e->attrs[i].value);
            }
            return 0;
        }
        default:
            return -1;
    }
}

static int reserve_prep_msgs(size_t n) {
    if (g_prep_cap >= n) return 0;
    ob_any_msg_t *p = (ob_any_msg_t *)realloc(g_prep_msgs, n * sizeof(ob_any_msg_t));
    if (!p) return -1;
    g_prep_msgs = p;
    g_prep_cap = n;
    return 0;
}

static bool ob_begin_container(ob_ostream_t *os, size_t *len_off_out) {
    *len_off_out = os->bytes_written;
    const uint8_t zeros[2] = {0u, 0u};
    return ob_write(os, zeros, 2u);
}

static bool ob_end_container(ob_ostream_t *os, uint8_t *base, size_t len_off) {
    size_t body_len = os->bytes_written - (len_off + 2u);
    uint8_t *len_ptr = base + len_off;
    if (body_len <= 16383u) {
        uint16_t u = (uint16_t)(((uint16_t)body_len << 2) | 2u);
        len_ptr[0] = (uint8_t)(u & 0xFFu);
        len_ptr[1] = (uint8_t)((u >> 8) & 0xFFu);
        return true;
    }
    if (body_len <= 536870911UL) {
        if (os->bytes_written > os->max_size || (os->max_size - os->bytes_written) < 2u) {
            return false;
        }
        memmove(len_ptr + 4u, len_ptr + 2u, body_len);
        uint32_t u = (uint32_t)(((uint32_t)body_len << 3) | 4u);
        len_ptr[0] = (uint8_t)(u & 0xFFu);
        len_ptr[1] = (uint8_t)((u >> 8) & 0xFFu);
        len_ptr[2] = (uint8_t)((u >> 16) & 0xFFu);
        len_ptr[3] = (uint8_t)((u >> 24) & 0xFFu);
        os->bytes_written += 2u;
        os->state = base + os->bytes_written;
        return true;
    }
    return false;
}

static int prep(test_data_kind_t kind, const test_fixture_t *fx) {
    if (!desc_for_kind(kind) || !fx) return -1;
    int batch = (fx->batch_n > 1 && fx->batch != NULL) ? fx->batch_n : 1;
    if (reserve_prep_msgs((size_t)batch) != 0) return -1;
    if (batch > 1) {
        for (int i = 0; i < batch; i++) {
            if (fixture_to_ob(&fx->batch[i], &g_prep_msgs[i]) != 0) return -1;
        }
    } else {
        if (fixture_to_ob(fx, &g_prep_msgs[0]) != 0) return -1;
    }
    g_prep_fx = fx;
    g_prep_kind = kind;
    g_prep_n = batch;
    return 0;
}

static int ser(const test_fixture_t *fx, uint8_t *buf, size_t cap, size_t *ol) {
    if (!fx || !buf || !ol) return -1;
    const ob_msgdesc_t *desc = desc_for_kind(fx->kind);
    if (!desc) return -1;

    int batch = (fx->batch_n > 1 && fx->batch != NULL) ? fx->batch_n : 1;
    ob_ostream_t os = ob_ostream_from_buffer(buf, cap);

    if (batch <= 1) {
        const ob_any_msg_t *src = NULL;
        if (fx == g_prep_fx && fx->kind == g_prep_kind && g_prep_n == 1 && g_prep_msgs) {
            src = &g_prep_msgs[0];
        } else {
            if (fixture_to_ob(fx, &g_scratch_msg) != 0) return -1;
            src = &g_scratch_msg;
        }
        if (!ob_encode(&os, desc, src)) return -1;
        *ol = os.bytes_written;
        return 0;
    }

    /* Batch<Type> wire format: field 1 = WireArray of WireSubmsg. */
    bool use_prepared = (fx == g_prep_fx && fx->kind == g_prep_kind &&
                         g_prep_n == batch && g_prep_msgs != NULL);
    if (!ob_write_tag(&os, 1u, OB_WIRE_ARRAY)) return -1;
    size_t arr_len_off = 0;
    if (!ob_begin_container(&os, &arr_len_off)) return -1;
    uint8_t elem_wt = OB_WIRE_SUBMSG;
    if (!ob_write(&os, &elem_wt, 1u)) return -1;
    if (!ob_varint_encode(&os, (uint64_t)batch)) return -1;

    for (int i = 0; i < batch; i++) {
        const ob_any_msg_t *src = NULL;
        if (use_prepared) {
            src = &g_prep_msgs[i];
        } else {
            if (fixture_to_ob(&fx->batch[i], &g_scratch_msg) != 0) return -1;
            src = &g_scratch_msg;
        }
        size_t elem_len_off = 0;
        if (!ob_begin_container(&os, &elem_len_off)) return -1;
        if (!ob_encode(&os, desc, src)) return -1;
        if (!ob_end_container(&os, buf, elem_len_off)) return -1;
    }

    if (!ob_end_container(&os, buf, arr_len_off)) return -1;
    *ol = os.bytes_written;
    return 0;
}

static int de(const uint8_t *buf, size_t len, test_fixture_t *out, test_data_kind_t kind) {
    if (!buf || !out) return -1;
    const ob_msgdesc_t *desc = desc_for_kind(kind);
    if (!desc) return -1;

    int expect_n = out->batch_n > 0 ? out->batch_n : 1;
    ob_istream_t is = ob_istream_from_buffer(buf, len);

    if (expect_n <= 1) {
        if (!ob_decode(&is, desc, &g_dec_msg)) return -1;
        if (is.bytes_left != 0u) return -1;
        return ob_to_fixture(kind, &g_dec_msg, out);
    }

    /* Decode Batch<Type>: field 1 = WireArray of WireSubmsg. */
    uint64_t tag_raw = 0;
    if (!ob_varint_decode(&is, &tag_raw)) return -1;
    if ((tag_raw >> 4) != 1u || (tag_raw & 0x0Fu) != OB_WIRE_ARRAY) return -1;

    uint64_t body_len = 0;
    if (!ob_varint_decode(&is, &body_len)) return -1;
    if (body_len < 2u || body_len != (uint64_t)is.bytes_left) return -1;

    ob_istream_t sub = is;
    sub.bytes_left = (size_t)body_len;

    uint8_t elem_wt = 0;
    if (!ob_read(&sub, &elem_wt, 1u) || elem_wt != OB_WIRE_SUBMSG) return -1;

    uint64_t count = 0;
    if (!ob_varint_decode(&sub, &count)) return -1;
    if (count == 0u || count != (uint64_t)expect_n) return -1;

    test_fixture_t *items = (test_fixture_t *)calloc((size_t)count, sizeof(test_fixture_t));
    if (!items) return -1;

    for (uint64_t i = 0; i < count; i++) {
        uint64_t elem_len = 0;
        if (!ob_varint_decode(&sub, &elem_len) || elem_len > (uint64_t)sub.bytes_left) {
            free(items);
            return -1;
        }
        ob_istream_t elem_is = sub;
        elem_is.bytes_left = (size_t)elem_len;
        if (!ob_decode(&elem_is, desc, &g_dec_msg) || elem_is.bytes_left != 0u) {
            free(items);
            return -1;
        }
        sub.state = elem_is.state;
        sub.bytes_left -= (size_t)elem_len;
        if (ob_to_fixture(kind, &g_dec_msg, &items[i]) != 0) {
            free(items);
            return -1;
        }
    }
    if (sub.bytes_left != 0u) {
        free(items);
        return -1;
    }

    out->kind = kind;
    out->name = test_data_name(kind);
    out->batch_n = (int)count;
    out->batch = items;
    switch (kind) {
        case TD_MESSAGE:   out->message = items[0].message; break;
        case TD_DOCUMENT:  out->document = items[0].document; break;
        case TD_TELEMETRY: out->telemetry = items[0].telemetry; break;
        case TD_STRINGS:   out->strings = items[0].strings; break;
        case TD_EVENT:     out->event = items[0].event; break;
        default: break;
    }
    return 0;
}

void bench_register_omnibuf_nano(serializer_t *o, int *c) {
    BENCH_ADD(o, c, "omnibuf-nano", "0.1.3", "schema", prep, ser, de, fidelity_fx);
    o[*c - 1].owns_batch = 1;
}
