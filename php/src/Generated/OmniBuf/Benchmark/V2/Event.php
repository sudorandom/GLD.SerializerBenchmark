<?php

declare(strict_types=1);

namespace OmniBuf\Benchmark\V2;

use OmniBuf\Decoder;
use OmniBuf\Encoder;
use OmniBuf\GeneratedMessage;
use OmniBuf\SchemaMeta;
use OmniBuf\UInt64;
use OmniBuf\Wire;
use OmniBuf\WireType;

final class Event implements GeneratedMessage
{
    private static ?int $nativeSchemaId = null;
    private static bool $nativeSchemaChecked = false;

    public string $eventId = '';

    public string $eventType = '';

    public int $occurredAt = 0;

    public string $producer = '';

    /** @var list<\OmniBuf\Benchmark\V2\EventAttr> */
    public array $attrs = [];

    public function __construct(
        ?string $eventId = null,
        ?string $eventType = null,
        ?int $occurredAt = null,
        ?string $producer = null,
        ?array $attrs = null,
    ) {
        if ($eventId !== null) { $this->eventId = $eventId; }
        if ($eventType !== null) { $this->eventType = $eventType; }
        if ($occurredAt !== null) { $this->occurredAt = $occurredAt; }
        if ($producer !== null) { $this->producer = $producer; }
        if ($attrs !== null) { $this->attrs = $attrs; }
    }

    public function marshalOmnibuf(bool $deterministic = false, bool $selfDescribing = false): string
    {
        if (!$selfDescribing && \function_exists('omnibuf_native_encode')) {
            $schemaId = self::nativeSchemaId();
            if ($schemaId !== null) {
                return \omnibuf_native_encode($schemaId, $this, $deterministic);
            }
        }
        $enc = new Encoder();
        if ($selfDescribing) {
            self::schemaMeta()->writeTo($enc, true);
        }
        $this->marshalTo($enc, $deterministic);
        return $enc->finish();
    }

    public function marshalTo(Encoder $enc, bool $deterministic = false): void
    {
        if ($this->eventId !== '') {
            $enc->buf .= "\x35";
            $enc->writeVarint(\strlen($this->eventId));
            $enc->buf .= $this->eventId;
        }
        if ($this->eventType !== '') {
            $enc->buf .= "\x55";
            $enc->writeVarint(\strlen($this->eventType));
            $enc->buf .= $this->eventType;
        }
        if ($this->occurredAt !== 0) {
            $enc->buf .= "\x63";
            $enc->writeVarint(($this->occurredAt << 1) ^ ($this->occurredAt >> 63));
        }
        if ($this->producer !== '') {
            $enc->buf .= "\x95";
            $enc->writeVarint(\strlen($this->producer));
            $enc->buf .= $this->producer;
        }
        if ($this->attrs !== []) {
            $enc->buf .= "\xbb";
            $enc->beginContainer();
            $enc->buf .= "\x0c";
            $enc->writeVarint(\count($this->attrs));
            foreach ($this->attrs as $item) {
                $enc->beginContainer(); $item->marshalTo($enc, $deterministic ?? true); $enc->endContainer();
            }
            $enc->endContainer();
        }
    }

    public static function unmarshalOmnibuf(
        string $data,
        int $maxDepth = Wire::MAX_NESTING_DEPTH,
        int $maxElements = Wire::DEFAULT_MAX_ELEMENTS,
    ): static {
        if (\function_exists('omnibuf_native_decode')) {
            $schemaId = self::nativeSchemaId();
            if ($schemaId !== null) {
                /** @var static $msg */
                $msg = \omnibuf_native_decode($schemaId, $data, $maxDepth, $maxElements);
                return $msg;
            }
        }
        $msg = new self();
        $msg->decodeFrom(new Decoder($data, 0, \strlen($data), 0, $maxDepth, $maxElements), false);
        return $msg;
    }

    /** @internal */
    public static function nativeSchemaId(): ?int
    {
        if (self::$nativeSchemaChecked) {
            return self::$nativeSchemaId;
        }
        self::$nativeSchemaChecked = true;
        if (!\function_exists('omnibuf_native_register_schema')) {
            return null;
        }
        $specs = [];
        $specs[] = ['number' => 1, 'prop' => 'eventId', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 2, 'prop' => 'eventType', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 3, 'prop' => 'occurredAt', 'kind' => 'sint64', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 4, 'prop' => 'producer', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $sub_5 = \OmniBuf\Benchmark\V2\EventAttr::nativeSchemaId();
        if ($sub_5 === null) { return null; }
        $specs[] = ['number' => 5, 'prop' => 'attrs', 'kind' => 'array', 'elemKind' => 'message', 'subSchemaId' => $sub_5, 'omitEmpty' => true, 'nullable' => false];
        self::$nativeSchemaId = \omnibuf_native_register_schema(self::class, $specs);
        return self::$nativeSchemaId;
    }

    public function unmarshalInto(
        string $data,
        bool $merge = false,
        int $maxDepth = Wire::MAX_NESTING_DEPTH,
        int $maxElements = Wire::DEFAULT_MAX_ELEMENTS,
    ): void {
        $this->decodeFrom(new Decoder($data, 0, \strlen($data), 0, $maxDepth, $maxElements), $merge);
    }

    public function decodeFrom(Decoder $dec, bool $merge = false): void
    {
        if (!$merge) {
            $this->eventId = '';
            $this->eventType = '';
            $this->occurredAt = 0;
            $this->producer = '';
            $this->attrs = [];
        }
        $buf = $dec->buf;
        $end = $dec->end;
        while ($dec->pos < $end) {
            $b0 = \ord($buf[$dec->pos]);
            if (($b0 & 1) !== 0) {
                $tag = $b0 >> 1;
                $dec->pos++;
                $fieldNum = $tag >> 4;
                $wireType = $tag & 0x0F;
                if ($fieldNum === 0 && $wireType !== WireType::SUB_MSG) {
                    throw new \OmniBuf\Exception\DecodeException("omnibuf: field #0 must use WireSubMsg");
                }
            } else {
                [$fieldNum, $wireType] = $dec->readTag();
            }
            if ($fieldNum === 0) {
                $dec->readField0();
                continue;
            }
            switch ($fieldNum) {
                case 1:
                    if ($wireType !== 10) { $dec->skipUnknown($wireType); break; }
                    $this->eventId = $dec->readString();
                    break;
                case 2:
                    if ($wireType !== 10) { $dec->skipUnknown($wireType); break; }
                    $this->eventType = $dec->readString();
                    break;
                case 3:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->occurredAt = $dec->readSint64();
                    break;
                case 4:
                    if ($wireType !== 10) { $dec->skipUnknown($wireType); break; }
                    $this->producer = $dec->readString();
                    break;
                case 5:
                    if ($wireType !== WireType::ARRAY) { $dec->skipUnknown($wireType); break; }
                    [$arrDec, $_et, $count] = $dec->readArrayHeader(12);
                    for ($i = 0; $i < $count; $i++) {
                        $this->attrs[] = (static function () use ($arrDec): \OmniBuf\Benchmark\V2\EventAttr { $c = new \OmniBuf\Benchmark\V2\EventAttr(); $c->decodeFrom($arrDec->readSubMessageDecoder(), false); return $c; })();
                    }
                    $arrDec->assertAtEnd('WireArray');
                    break;
                default:
                    $dec->skipUnknown($wireType);
                    break;
            }
        }
    }

    /** @param array<class-string, true> $visited */
    public static function schemaMeta(array $visited = []): SchemaMeta
    {
        $visited[self::class] = true;
        $children = [];
        if (!isset($visited[\OmniBuf\Benchmark\V2\EventAttr::class])) {
            $children[5] = \OmniBuf\Benchmark\V2\EventAttr::schemaMeta($visited);
        }
        return new SchemaMeta(
            'benchmark.v2.Event',
            [
                1 => 'event_id',
                2 => 'event_type',
                3 => 'occurred_at',
                4 => 'producer',
                5 => 'attrs',
            ],
            $children,
        );
    }
}
