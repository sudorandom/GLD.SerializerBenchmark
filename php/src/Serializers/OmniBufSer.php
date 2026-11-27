<?php

declare(strict_types=1);

namespace Benchmark\Serializers;

use Benchmark\ProtoBridge;
use OmniBuf\Benchmark\V2\BatchDocument;
use OmniBuf\Benchmark\V2\BatchEvent;
use OmniBuf\Benchmark\V2\BatchMessage;
use OmniBuf\Benchmark\V2\BatchStrings;
use OmniBuf\Benchmark\V2\BatchTelemetry;
use OmniBuf\Benchmark\V2\Document;
use OmniBuf\Benchmark\V2\DocumentItem;
use OmniBuf\Benchmark\V2\DocumentMeta;
use OmniBuf\Benchmark\V2\Event;
use OmniBuf\Benchmark\V2\EventAttr;
use OmniBuf\Benchmark\V2\Message;
use OmniBuf\Benchmark\V2\Strings;
use OmniBuf\Benchmark\V2\Telemetry;
use OmniBuf\Encoder;
use OmniBuf\GeneratedMessage;

final class OmniBufSer extends BytesSer
{
    private readonly bool $hasNative;
    private Encoder $encoder;
    private ?string $kind = null;
    private ?bool $batch = null;
    private ?GeneratedMessage $decodeTarget = null;
    /** @var class-string<GeneratedMessage>|null */
    private ?string $targetClass = null;

    public function __construct()
    {
        $this->hasNative = \function_exists('omnibuf_native_encode') && \function_exists('omnibuf_native_decode');
        $this->encoder = new Encoder();
        \class_exists(\OmniBuf\Decoder::class);
        \class_exists(\OmniBuf\Wire::class);
        \class_exists(\OmniBuf\WireType::class);
    }

    public function name(): string
    {
        return 'omnibuf';
    }

    public function version(): string
    {
        $pkg = \Composer\InstalledVersions::getPrettyVersion('omnibuf/omnibuf') ?? 'omnibuf/omnibuf';
        return $this->hasNative ? $pkg . '+ext' : $pkg . '+php';
    }

    public function streamMode(): string
    {
        return 'adapted';
    }

    public function prepare(mixed $value): void
    {
        [$this->kind, $this->batch] = ProtoBridge::detect($value);
        $this->decodeTarget = self::emptyMessage($this->kind, $this->batch);
        $this->targetClass = $this->decodeTarget::class;
    }

    public function serializeBytes(mixed $value): string
    {
        $msg = self::toOmniBuf($value, $this->kind, $this->batch);

        if ($this->hasNative) {
            return $msg->marshalOmnibuf();
        }

        $this->encoder->reset();
        $msg->marshalTo($this->encoder);
        return $this->encoder->finish();
    }

    public function deserializeBytes(string $data): mixed
    {
        if ($this->hasNative && $this->targetClass !== null) {
            $cls = $this->targetClass;
            return self::fromOmniBuf($cls::unmarshalOmnibuf($data));
        }

        $target = $this->decodeTarget;
        if ($target === null) {
            throw new \LogicException('omnibuf: prepare() must be called before deserializeBytes()');
        }
        $target->unmarshalInto($data);
        return self::fromOmniBuf($target);
    }

    private static function toOmniBuf(mixed $value, ?string $kind = null, ?bool $batch = null): GeneratedMessage
    {
        if ($kind === null || $batch === null) {
            [$kind, $batch] = ProtoBridge::detect($value);
        }
        if ($batch) {
            return match ($kind) {
                'message' => new BatchMessage(array_map(self::toMessage(...), $value)),
                'document' => new BatchDocument(array_map(self::toDocument(...), $value)),
                'telemetry' => new BatchTelemetry(array_map(self::toTelemetry(...), $value)),
                'strings' => new BatchStrings(array_map(self::toStrings(...), $value)),
                'event' => new BatchEvent(array_map(self::toEvent(...), $value)),
                default => throw new \InvalidArgumentException($kind),
            };
        }
        return match ($kind) {
            'message' => self::toMessage($value),
            'document' => self::toDocument($value),
            'telemetry' => self::toTelemetry($value),
            'strings' => self::toStrings($value),
            'event' => self::toEvent($value),
            default => throw new \InvalidArgumentException($kind),
        };
    }

    private static function emptyMessage(string $kind, bool $batch): GeneratedMessage
    {
        if ($batch) {
            return match ($kind) {
                'message' => new BatchMessage(),
                'document' => new BatchDocument(),
                'telemetry' => new BatchTelemetry(),
                'strings' => new BatchStrings(),
                'event' => new BatchEvent(),
                default => throw new \InvalidArgumentException($kind),
            };
        }
        return match ($kind) {
            'message' => new Message(),
            'document' => new Document(),
            'telemetry' => new Telemetry(),
            'strings' => new Strings(),
            'event' => new Event(),
            default => throw new \InvalidArgumentException($kind),
        };
    }

    private static function fromOmniBuf(GeneratedMessage $msg): mixed
    {
        return match (true) {
            $msg instanceof BatchMessage => array_map(self::fromMessage(...), $msg->items),
            $msg instanceof BatchDocument => array_map(self::fromDocument(...), $msg->items),
            $msg instanceof BatchTelemetry => array_map(self::fromTelemetry(...), $msg->items),
            $msg instanceof BatchStrings => array_map(self::fromStrings(...), $msg->items),
            $msg instanceof BatchEvent => array_map(self::fromEvent(...), $msg->items),
            $msg instanceof Message => self::fromMessage($msg),
            $msg instanceof Document => self::fromDocument($msg),
            $msg instanceof Telemetry => self::fromTelemetry($msg),
            $msg instanceof Strings => self::fromStrings($msg),
            $msg instanceof Event => self::fromEvent($msg),
            default => throw new \InvalidArgumentException($msg::class),
        };
    }

    private static function toMessage(array $v): Message
    {
        return new Message(
            fBool: (bool) $v['f_bool'],
            fInt32: (int) $v['f_int32'],
            fInt64: (int) $v['f_int64'],
            fFloat64: (float) $v['f_float64'],
            fString: (string) $v['f_string'],
            fBool2: (bool) $v['f_bool_2'],
            fInt322: (int) $v['f_int32_2'],
            fString2: (string) $v['f_string_2'],
        );
    }

    private static function fromMessage(Message $m): array
    {
        return [
            'f_bool' => $m->fBool,
            'f_int32' => $m->fInt32,
            'f_int64' => $m->fInt64,
            'f_float64' => $m->fFloat64,
            'f_string' => $m->fString,
            'f_bool_2' => $m->fBool2,
            'f_int32_2' => $m->fInt322,
            'f_string_2' => $m->fString2,
        ];
    }

    private static function toDocument(array $v): Document
    {
        $meta = new DocumentMeta(
            region: (string) $v['meta']['region'],
            version: (int) $v['meta']['version'],
        );
        $items = [];
        foreach ($v['items'] as $it) {
            $items[] = new DocumentItem(
                sku: (string) $it['sku'],
                qty: (int) $it['qty'],
                priceMinor: (int) $it['price_minor'],
            );
        }
        return new Document(
            id: (string) $v['id'],
            status: (int) $v['status'],
            meta: $meta,
            items: $items,
        );
    }

    private static function fromDocument(Document $d): array
    {
        $meta = $d->meta;
        $items = [];
        foreach ($d->items as $it) {
            $items[] = [
                'sku' => $it->sku,
                'qty' => $it->qty,
                'price_minor' => $it->priceMinor,
            ];
        }
        return [
            'id' => $d->id,
            'status' => $d->status,
            'meta' => [
                'region' => $meta !== null ? $meta->region : '',
                'version' => $meta !== null ? $meta->version : 0,
            ],
            'items' => $items,
        ];
    }

    private static function toTelemetry(array $v): Telemetry
    {
        $tags = [];
        foreach ($v['tags'] as $t) {
            $tags[] = (string) $t;
        }
        $values = [];
        foreach ($v['values'] as $x) {
            $values[] = (float) $x;
        }
        return new Telemetry(
            source: (string) $v['source'],
            ts: (int) $v['ts'],
            tags: $tags,
            values: $values,
        );
    }

    private static function fromTelemetry(Telemetry $t): array
    {
        return [
            'source' => $t->source,
            'ts' => $t->ts,
            'tags' => $t->tags,
            'values' => $t->values,
        ];
    }

    private static function toStrings(array $v): Strings
    {
        $items = [];
        foreach ($v['items'] as $s) {
            $items[] = (string) $s;
        }
        return new Strings(items: $items);
    }

    private static function fromStrings(Strings $s): array
    {
        return ['items' => $s->items];
    }

    private static function toEvent(array $v): Event
    {
        $attrs = [];
        foreach ($v['attrs'] as $a) {
            $attrs[] = new EventAttr(
                key: (string) $a['key'],
                value: (string) $a['value'],
            );
        }
        return new Event(
            eventId: (string) $v['event_id'],
            eventType: (string) $v['event_type'],
            occurredAt: (int) $v['occurred_at'],
            producer: (string) $v['producer'],
            attrs: $attrs,
        );
    }

    private static function fromEvent(Event $e): array
    {
        $attrs = [];
        foreach ($e->attrs as $a) {
            $attrs[] = ['key' => $a->key, 'value' => $a->value];
        }
        return [
            'event_id' => $e->eventId,
            'event_type' => $e->eventType,
            'occurred_at' => $e->occurredAt,
            'producer' => $e->producer,
            'attrs' => $attrs,
        ];
    }
}
