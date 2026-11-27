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

final class Telemetry implements GeneratedMessage
{
    private static ?int $nativeSchemaId = null;
    private static bool $nativeSchemaChecked = false;

    public string $source = '';

    public int $ts = 0;

    /** @var list<string> */
    public array $tags = [];

    /** @var list<float> */
    public array $values = [];

    public function __construct(
        ?string $source = null,
        ?int $ts = null,
        ?array $tags = null,
        ?array $values = null,
    ) {
        if ($source !== null) { $this->source = $source; }
        if ($ts !== null) { $this->ts = $ts; }
        if ($tags !== null) { $this->tags = $tags; }
        if ($values !== null) { $this->values = $values; }
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
        if ($this->source !== '') {
            $enc->buf .= "\x35";
            $enc->writeVarint(\strlen($this->source));
            $enc->buf .= $this->source;
        }
        if ($this->ts !== 0) {
            $enc->buf .= "\x43";
            $enc->writeVarint(($this->ts << 1) ^ ($this->ts >> 63));
        }
        if ($this->tags !== []) {
            $enc->buf .= "\x7b";
            $enc->beginContainer();
            $enc->buf .= "\x0a";
            $enc->writeVarint(\count($this->tags));
            foreach ($this->tags as $item) {
                $enc->writeVarint(\strlen($item)); $enc->buf .= $item;
            }
            $enc->endContainer();
        }
        if ($this->values !== []) {
            $enc->buf .= "\x9b";
            $enc->beginContainer();
            $enc->buf .= "\x09";
            $enc->writeVarint(\count($this->values));
            foreach ($this->values as $item) {
                $enc->buf .= pack('e', $item);
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
        $specs[] = ['number' => 1, 'prop' => 'source', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 2, 'prop' => 'ts', 'kind' => 'sint64', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 3, 'prop' => 'tags', 'kind' => 'array', 'elemKind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 4, 'prop' => 'values', 'kind' => 'array', 'elemKind' => 'float64', 'omitEmpty' => true, 'nullable' => false];
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
            $this->source = '';
            $this->ts = 0;
            $this->tags = [];
            $this->values = [];
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
                    $this->source = $dec->readString();
                    break;
                case 2:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->ts = $dec->readSint64();
                    break;
                case 3:
                    if ($wireType !== WireType::ARRAY) { $dec->skipUnknown($wireType); break; }
                    [$arrDec, $_et, $count] = $dec->readArrayHeader(10);
                    for ($i = 0; $i < $count; $i++) {
                        $this->tags[] = $arrDec->readString();
                    }
                    $arrDec->assertAtEnd('WireArray');
                    break;
                case 4:
                    if ($wireType !== WireType::ARRAY) { $dec->skipUnknown($wireType); break; }
                    [$arrDec, $_et, $count] = $dec->readArrayHeader(9);
                    for ($i = 0; $i < $count; $i++) {
                        $this->values[] = $arrDec->readFloat64();
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
        return new SchemaMeta(
            'benchmark.v2.Telemetry',
            [
                1 => 'source',
                2 => 'ts',
                3 => 'tags',
                4 => 'values',
            ],
            $children,
        );
    }
}
