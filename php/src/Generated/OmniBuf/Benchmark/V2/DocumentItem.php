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

final class DocumentItem implements GeneratedMessage
{
    private static ?int $nativeSchemaId = null;
    private static bool $nativeSchemaChecked = false;

    public string $sku = '';

    public int $qty = 0;

    public int $priceMinor = 0;

    public function __construct(
        ?string $sku = null,
        ?int $qty = null,
        ?int $priceMinor = null,
    ) {
        if ($sku !== null) { $this->sku = $sku; }
        if ($qty !== null) { $this->qty = $qty; }
        if ($priceMinor !== null) { $this->priceMinor = $priceMinor; }
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
        if ($this->sku !== '') {
            $enc->buf .= "\x35";
            $enc->writeVarint(\strlen($this->sku));
            $enc->buf .= $this->sku;
        }
        if ($this->qty !== 0) {
            $enc->buf .= "\x43";
            $n = ($this->qty << 32) >> 32;
            $enc->writeVarint(($n << 1) ^ ($n >> 63));
        }
        if ($this->priceMinor !== 0) {
            $enc->buf .= "\x63";
            $enc->writeVarint(($this->priceMinor << 1) ^ ($this->priceMinor >> 63));
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
        $specs[] = ['number' => 1, 'prop' => 'sku', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 2, 'prop' => 'qty', 'kind' => 'sint32', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 3, 'prop' => 'priceMinor', 'kind' => 'sint64', 'omitEmpty' => true, 'nullable' => false];
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
            $this->sku = '';
            $this->qty = 0;
            $this->priceMinor = 0;
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
                    $this->sku = $dec->readString();
                    break;
                case 2:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->qty = $dec->readSint32();
                    break;
                case 3:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->priceMinor = $dec->readSint64();
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
            'benchmark.v2.DocumentItem',
            [
                1 => 'sku',
                2 => 'qty',
                3 => 'price_minor',
            ],
            $children,
        );
    }
}
