"""OmniBuf static codecs for Data Model v2 suite and columnar messages."""

from __future__ import annotations

import os
import sys
from typing import Any, Callable

from .base import Serializer
from ..data_v2 import models as m

_PY_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
if _PY_ROOT not in sys.path:
    sys.path.insert(0, _PY_ROOT)

try:
    from generated.omnibuf import benchmark_v2_omnibuf_pb as _ob  # type: ignore
except ImportError:
    _ob = None  # type: ignore

_SUPPORTED_TYPES = frozenset(
    {
        "message",
        "document",
        "telemetry",
        "strings",
        "event",
        "table",
        "table_project",
        "nested_table",
        "signal",
    }
)


def _to_message(o: m.Message) -> Any:
    return _ob.Message(
        f_bool=o.f_bool,
        f_int32=o.f_int32,
        f_int64=o.f_int64,
        f_float64=o.f_float64,
        f_string=o.f_string,
        f_bool_2=o.f_bool_2,
        f_int32_2=o.f_int32_2,
        f_string_2=o.f_string_2,
    )


def _to_document(o: m.Document) -> Any:
    return _ob.Document(
        id=o.id,
        status=o.status,
        meta=_ob.DocumentMeta(region=o.meta.region, version=o.meta.version),
        items=[
            _ob.DocumentItem(sku=it.sku, qty=it.qty, price_minor=it.price_minor)
            for it in o.items
        ],
    )


def _to_telemetry(o: m.Telemetry) -> Any:
    return _ob.Telemetry(
        source=o.source,
        ts=o.ts,
        tags=list(o.tags),
        values=list(o.values),
    )


def _to_strings(o: m.Strings) -> Any:
    return _ob.Strings(items=list(o.items))


def _to_event(o: m.Event) -> Any:
    return _ob.Event(
        event_id=o.event_id,
        event_type=o.event_type,
        occurred_at=o.occurred_at,
        producer=o.producer,
        attrs=[_ob.EventAttr(key=a.key, value=a.value) for a in o.attrs],
    )


def _to_table(o: m.TableRow) -> Any:
    return _ob.Table(
        f_float_0=o.f_float_0,
        f_float_1=o.f_float_1,
        f_float_2=o.f_float_2,
        f_float_3=o.f_float_3,
        f_float_4=o.f_float_4,
        f_float_5=o.f_float_5,
        f_float_6=o.f_float_6,
        f_float_7=o.f_float_7,
        f_float_8=o.f_float_8,
        f_float_9=o.f_float_9,
        f_float_10=o.f_float_10,
        f_float_11=o.f_float_11,
        f_float_12=o.f_float_12,
        f_float_13=o.f_float_13,
        f_float_14=o.f_float_14,
        f_float_15=o.f_float_15,
        f_int_0=o.f_int_0,
        f_int_1=o.f_int_1,
        f_int_2=o.f_int_2,
        f_int_3=o.f_int_3,
        f_str_0=o.f_str_0,
        f_str_1=o.f_str_1,
    )


def _to_nested_row(o: m.NestedRow) -> Any:
    return _ob.NestedRow(
        id=o.id,
        status=o.status,
        meta=_ob.NestedMeta(region=o.meta.region, version=o.meta.version),
        items=[
            _ob.NestedItem(sku=it.sku, qty=it.qty, price_minor=it.price_minor)
            for it in o.items
        ],
    )


def _to_signal(o: m.Signal) -> Any:
    return _ob.Signal(
        seq=o.seq,
        ts=o.ts,
        price_mantissa=o.price_mantissa,
        qty=o.qty,
        flags=o.flags,
        symbol=o.symbol,
        venue=o.venue,
        legs=[
            _ob.SignalLeg(leg_id=leg.leg_id, leg_qty=leg.leg_qty, leg_pad=leg.leg_pad)
            for leg in o.legs
        ],
    )


_TYPE_SPECS: dict[
    str,
    tuple[
        Callable[[Any], Any],
        type,
        tuple[Callable[[Any], bytes], Callable[[bytes], Any]],
        tuple[Callable[[Any], bytes], Callable[[bytes], Any]],
    ],
] = {}

if _ob is not None:
    _TABLE_SPEC = (
        _to_table,
        _ob.BatchTable,
        (_ob.marshal_table, _ob.unmarshal_table),
        (_ob.marshal_batch_table, _ob.unmarshal_batch_table),
    )
    _TYPE_SPECS = {
        "message": (
            _to_message,
            _ob.BatchMessage,
            (_ob.marshal_message, _ob.unmarshal_message),
            (_ob.marshal_batch_message, _ob.unmarshal_batch_message),
        ),
        "document": (
            _to_document,
            _ob.BatchDocument,
            (_ob.marshal_document, _ob.unmarshal_document),
            (_ob.marshal_batch_document, _ob.unmarshal_batch_document),
        ),
        "telemetry": (
            _to_telemetry,
            _ob.BatchTelemetry,
            (_ob.marshal_telemetry, _ob.unmarshal_telemetry),
            (_ob.marshal_batch_telemetry, _ob.unmarshal_batch_telemetry),
        ),
        "strings": (
            _to_strings,
            _ob.BatchStrings,
            (_ob.marshal_strings, _ob.unmarshal_strings),
            (_ob.marshal_batch_strings, _ob.unmarshal_batch_strings),
        ),
        "event": (
            _to_event,
            _ob.BatchEvent,
            (_ob.marshal_event, _ob.unmarshal_event),
            (_ob.marshal_batch_event, _ob.unmarshal_batch_event),
        ),
        "table": _TABLE_SPEC,
        "table_project": _TABLE_SPEC,
        "nested_table": (
            _to_nested_row,
            _ob.BatchNestedRow,
            (_ob.marshal_nested_row, _ob.unmarshal_nested_row),
            (_ob.marshal_batch_nested_row, _ob.unmarshal_batch_nested_row),
        ),
        "signal": (
            _to_signal,
            _ob.BatchSignal,
            (_ob.marshal_signal, _ob.unmarshal_signal),
            (_ob.marshal_batch_signal, _ob.unmarshal_batch_signal),
        ),
    }


def _spec_for(
    type_id: str,
) -> tuple[
    Callable[[Any], Any],
    type,
    tuple[Callable[[Any], bytes], Callable[[bytes], Any]],
    tuple[Callable[[Any], bytes], Callable[[bytes], Any]],
]:
    if _ob is None:
        raise RuntimeError("omnibuf generated codecs are unavailable")
    spec = _TYPE_SPECS.get(type_id)
    if spec is None:
        raise TypeError(f"no omnibuf mapping for {type_id!r}")
    return spec


def _bind_deser(
    unmarshal_fn: Callable[[bytes], Any],
    *,
    batch: bool,
    project: bool,
) -> Callable[[bytes], Any]:
    if project:
        if batch:
            def _deser_project_batch(data: bytes) -> list[float]:
                return [row.f_float_0 for row in unmarshal_fn(data).items]

            return _deser_project_batch

        def _deser_project_one(data: bytes) -> list[float]:
            return [unmarshal_fn(data).f_float_0]

        return _deser_project_one

    if batch:
        def _deser_batch(data: bytes) -> list[Any]:
            return unmarshal_fn(data).items

        return _deser_batch

    return unmarshal_fn


class OmniBufSerializer(Serializer):
    """OmniBuf generated-codec wrapper for suite and columnar types."""

    package_name = "omnibuf"
    native_kind = "message"
    stream_mode = "adapted"

    def __init__(self) -> None:
        super().__init__()
        self._marshal: Callable[[Any], bytes] | None = None
        self._deser: Callable[[bytes], Any] | None = None

    @property
    def name(self) -> str:
        return "omnibuf"

    def supports(self, test_data_name: str) -> bool:
        return test_data_name in _SUPPORTED_TYPES and _ob is not None

    def _bind(self, test_data_name: str, batch: bool) -> None:
        _, _, single_pair, batch_pair = _spec_for(test_data_name)
        marshal_fn, unmarshal_fn = batch_pair if batch else single_pair
        self._marshal = marshal_fn
        self._deser = _bind_deser(
            unmarshal_fn,
            batch=batch,
            project=test_data_name == "table_project",
        )

    def prepare(self, test_data_name: str, test_data_type: type) -> None:
        super().prepare(test_data_name, test_data_type)
        origin = getattr(test_data_type, "__origin__", None)
        self._bind(test_data_name, batch=origin is list)

    def prepare_data(self, obj: Any, test_data_name: str, test_data_type: type) -> Any:
        conv, batch_ctor, single_pair, batch_pair = _spec_for(test_data_name)
        batch = isinstance(obj, list)
        marshal_fn, unmarshal_fn = batch_pair if batch else single_pair
        self._marshal = marshal_fn
        self._deser = _bind_deser(
            unmarshal_fn,
            batch=batch,
            project=test_data_name == "table_project",
        )
        if batch:
            return batch_ctor(items=[conv(item) for item in obj])
        return conv(obj)

    def serialize_bytes(self, obj: Any) -> bytes:
        marshal_fn = self._marshal
        if marshal_fn is None:
            raise RuntimeError("omnibuf prepare required")
        return marshal_fn(obj)

    def deserialize_bytes(self, data: bytes) -> Any:
        deser_fn = self._deser
        if deser_fn is None:
            raise RuntimeError("omnibuf prepare required")
        return deser_fn(data)
