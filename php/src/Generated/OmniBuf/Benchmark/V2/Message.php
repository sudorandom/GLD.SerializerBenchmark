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

final class Message implements GeneratedMessage
{
    private static ?int $nativeSchemaId = null;
    private static bool $nativeSchemaChecked = false;

    public bool $fBool = false;

    public int $fInt32 = 0;

    public int $fInt64 = 0;

    public float $fFloat64 = 0.0;

    public string $fString = '';

    public bool $fBool2 = false;

    public int $fInt322 = 0;

    public string $fString2 = '';

    public function __construct(
        ?bool $fBool = null,
        ?int $fInt32 = null,
        ?int $fInt64 = null,
        ?float $fFloat64 = null,
        ?string $fString = null,
        ?bool $fBool2 = null,
        ?int $fInt322 = null,
        ?string $fString2 = null,
    ) {
        if ($fBool !== null) { $this->fBool = $fBool; }
        if ($fInt32 !== null) { $this->fInt32 = $fInt32; }
        if ($fInt64 !== null) { $this->fInt64 = $fInt64; }
        if ($fFloat64 !== null) { $this->fFloat64 = $fFloat64; }
        if ($fString !== null) { $this->fString = $fString; }
        if ($fBool2 !== null) { $this->fBool2 = $fBool2; }
        if ($fInt322 !== null) { $this->fInt322 = $fInt322; }
        if ($fString2 !== null) { $this->fString2 = $fString2; }
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
        if ($this->fBool) {
            $enc->buf .= "\x27";
        }
        if ($this->fInt32 !== 0) {
            $enc->buf .= "\x43";
            $n = ($this->fInt32 << 32) >> 32;
            $enc->writeVarint(($n << 1) ^ ($n >> 63));
        }
        if ($this->fInt64 !== 0) {
            $enc->buf .= "\x63";
            $enc->writeVarint(($this->fInt64 << 1) ^ ($this->fInt64 >> 63));
        }
        if ($this->fFloat64 !== 0.0 || pack('e', $this->fFloat64) !== "\x00\x00\x00\x00\x00\x00\x00\x00") {
            $enc->buf .= "\x93" . pack('e', $this->fFloat64);
        }
        if ($this->fString !== '') {
            $enc->buf .= "\xb5";
            $enc->writeVarint(\strlen($this->fString));
            $enc->buf .= $this->fString;
        }
        if ($this->fBool2) {
            $enc->buf .= "\xc7";
        }
        if ($this->fInt322 !== 0) {
            $enc->buf .= "\xe3";
            $n = ($this->fInt322 << 32) >> 32;
            $enc->writeVarint(($n << 1) ^ ($n >> 63));
        }
        if ($this->fString2 !== '') {
            $enc->buf .= "\x2a\x02";
            $enc->writeVarint(\strlen($this->fString2));
            $enc->buf .= $this->fString2;
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
        $specs[] = ['number' => 1, 'prop' => 'fBool', 'kind' => 'bool', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 2, 'prop' => 'fInt32', 'kind' => 'sint32', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 3, 'prop' => 'fInt64', 'kind' => 'sint64', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 4, 'prop' => 'fFloat64', 'kind' => 'float64', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 5, 'prop' => 'fString', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 6, 'prop' => 'fBool2', 'kind' => 'bool', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 7, 'prop' => 'fInt322', 'kind' => 'sint32', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 8, 'prop' => 'fString2', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
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
            $this->fBool = false;
            $this->fInt32 = 0;
            $this->fInt64 = 0;
            $this->fFloat64 = 0.0;
            $this->fString = '';
            $this->fBool2 = false;
            $this->fInt322 = 0;
            $this->fString2 = '';
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
                    if ($wireType === WireType::BOOL_FALSE) {
                        $this->fBool = false;
                    } elseif ($wireType === WireType::BOOL_TRUE) {
                        $this->fBool = true;
                    } else {
                        $dec->skipUnknown($wireType);
                    }
                    break;
                case 2:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->fInt32 = $dec->readSint32();
                    break;
                case 3:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->fInt64 = $dec->readSint64();
                    break;
                case 4:
                    if ($wireType !== 9) { $dec->skipUnknown($wireType); break; }
                    $this->fFloat64 = $dec->readFloat64();
                    break;
                case 5:
                    if ($wireType !== 10) { $dec->skipUnknown($wireType); break; }
                    $this->fString = $dec->readString();
                    break;
                case 6:
                    if ($wireType === WireType::BOOL_FALSE) {
                        $this->fBool2 = false;
                    } elseif ($wireType === WireType::BOOL_TRUE) {
                        $this->fBool2 = true;
                    } else {
                        $dec->skipUnknown($wireType);
                    }
                    break;
                case 7:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->fInt322 = $dec->readSint32();
                    break;
                case 8:
                    if ($wireType !== 10) { $dec->skipUnknown($wireType); break; }
                    $this->fString2 = $dec->readString();
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
            'benchmark.v2.Message',
            [
                1 => 'f_bool',
                2 => 'f_int32',
                3 => 'f_int64',
                4 => 'f_float64',
                5 => 'f_string',
                6 => 'f_bool_2',
                7 => 'f_int32_2',
                8 => 'f_string_2',
            ],
            $children,
        );
    }
}
