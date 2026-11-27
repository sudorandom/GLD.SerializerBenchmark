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

final class Document implements GeneratedMessage
{
    private static ?int $nativeSchemaId = null;
    private static bool $nativeSchemaChecked = false;

    public string $id = '';

    public int $status = 0;

    public ?\OmniBuf\Benchmark\V2\DocumentMeta $meta = null;

    /** @var list<\OmniBuf\Benchmark\V2\DocumentItem> */
    public array $items = [];

    public function __construct(
        ?string $id = null,
        ?int $status = null,
        ?\OmniBuf\Benchmark\V2\DocumentMeta $meta = null,
        ?array $items = null,
    ) {
        if ($id !== null) { $this->id = $id; }
        if ($status !== null) { $this->status = $status; }
        if ($meta !== null) { $this->meta = $meta; }
        if ($items !== null) { $this->items = $items; }
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
        if ($this->id !== '') {
            $enc->buf .= "\x35";
            $enc->writeVarint(\strlen($this->id));
            $enc->buf .= $this->id;
        }
        if ($this->status !== 0) {
            $enc->buf .= "\x43";
            $n = ($this->status << 32) >> 32;
            $enc->writeVarint(($n << 1) ^ ($n >> 63));
        }
        if ($this->meta !== null) {
            $enc->buf .= "\x79";
            $enc->beginContainer();
            $this->meta->marshalTo($enc, $deterministic);
            $enc->endContainer();
        }
        if ($this->items !== []) {
            $enc->buf .= "\x9b";
            $enc->beginContainer();
            $enc->buf .= "\x0c";
            $enc->writeVarint(\count($this->items));
            foreach ($this->items as $item) {
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
        $specs[] = ['number' => 1, 'prop' => 'id', 'kind' => 'string', 'omitEmpty' => true, 'nullable' => false];
        $specs[] = ['number' => 2, 'prop' => 'status', 'kind' => 'sint32', 'omitEmpty' => true, 'nullable' => false];
        $sub_3 = \OmniBuf\Benchmark\V2\DocumentMeta::nativeSchemaId();
        if ($sub_3 === null) { return null; }
        $specs[] = ['number' => 3, 'prop' => 'meta', 'kind' => 'message', 'subSchemaId' => $sub_3, 'omitEmpty' => true, 'nullable' => true];
        $sub_4 = \OmniBuf\Benchmark\V2\DocumentItem::nativeSchemaId();
        if ($sub_4 === null) { return null; }
        $specs[] = ['number' => 4, 'prop' => 'items', 'kind' => 'array', 'elemKind' => 'message', 'subSchemaId' => $sub_4, 'omitEmpty' => true, 'nullable' => false];
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
            $this->id = '';
            $this->status = 0;
            $this->meta = null;
            $this->items = [];
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
                    $this->id = $dec->readString();
                    break;
                case 2:
                    if ($wireType !== 1) { $dec->skipUnknown($wireType); break; }
                    $this->status = $dec->readSint32();
                    break;
                case 3:
                    if ($wireType !== 12) { $dec->skipUnknown($wireType); break; }
                    $subDec = $dec->readSubMessageDecoder();
                    if ($this->meta === null) {
                        $this->meta = new \OmniBuf\Benchmark\V2\DocumentMeta();
                        $this->meta->decodeFrom($subDec, false);
                    } else {
                        $this->meta->decodeFrom($subDec, true);
                    }
                    break;
                case 4:
                    if ($wireType !== WireType::ARRAY) { $dec->skipUnknown($wireType); break; }
                    [$arrDec, $_et, $count] = $dec->readArrayHeader(12);
                    for ($i = 0; $i < $count; $i++) {
                        $this->items[] = (static function () use ($arrDec): \OmniBuf\Benchmark\V2\DocumentItem { $c = new \OmniBuf\Benchmark\V2\DocumentItem(); $c->decodeFrom($arrDec->readSubMessageDecoder(), false); return $c; })();
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
        if (!isset($visited[\OmniBuf\Benchmark\V2\DocumentMeta::class])) {
            $children[3] = \OmniBuf\Benchmark\V2\DocumentMeta::schemaMeta($visited);
        }
        if (!isset($visited[\OmniBuf\Benchmark\V2\DocumentItem::class])) {
            $children[4] = \OmniBuf\Benchmark\V2\DocumentItem::schemaMeta($visited);
        }
        return new SchemaMeta(
            'benchmark.v2.Document',
            [
                1 => 'id',
                2 => 'status',
                3 => 'meta',
                4 => 'items',
            ],
            $children,
        );
    }
}
