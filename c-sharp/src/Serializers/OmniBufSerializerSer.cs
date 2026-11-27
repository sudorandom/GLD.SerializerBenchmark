using System;
using System.Collections.Generic;
using System.IO;
using System.Linq.Expressions;
using System.Reflection;
using OmniBuf;

namespace GLD.SerializerBenchmark.Serializers
{
    /// <summary>OmniBuf static codecs on Benchmark.V2 messages (https://github.com/omnibuf/omnibuf-cs).</summary>
    internal sealed class OmniBufSerializerSer : MappedSerDeser
    {
        private delegate object ParseSpanDelegate(ReadOnlySpan<byte> data);

        private readonly OmniBufEncoder _encoder = new OmniBufEncoder();
        private Action<object, OmniBufEncoder> _marshal;
        private ParseSpanDelegate _parse;
        private Func<byte[], List<double>> _project;

        public OmniBufSerializerSer(IDomainNativeMap map) : base(map) { }

        public override string Name => "omnibuf";

        public override bool Supports(string testDataName) => testDataName is
            "message" or "document" or "telemetry" or "strings" or "event"
            or "table" or "table_project" or "nested_table" or "signal";

        protected override void OnNativeTypeReady(Type nativeRoot, List<Type> nativeSecondary)
        {
            var marshalMi = nativeRoot.GetMethod(
                "MarshalOmniBuf",
                BindingFlags.Public | BindingFlags.Instance,
                null,
                new[] { typeof(OmniBufEncoder), typeof(MarshalOptions) },
                null);
            if (marshalMi == null)
                throw new InvalidOperationException($"No MarshalOmniBuf(OmniBufEncoder, MarshalOptions) on {nativeRoot}");

            var pObj = Expression.Parameter(typeof(object), "o");
            var pEnc = Expression.Parameter(typeof(OmniBufEncoder), "enc");
            var marshalBody = Expression.Call(
                Expression.Convert(pObj, nativeRoot),
                marshalMi,
                pEnc,
                Expression.Constant(default(MarshalOptions)));
            _marshal = Expression.Lambda<Action<object, OmniBufEncoder>>(marshalBody, pObj, pEnc).Compile();

            var parseMi = nativeRoot.GetMethod(
                "ParseOmniBuf",
                BindingFlags.Public | BindingFlags.Static,
                null,
                new[] { typeof(ReadOnlySpan<byte>) },
                null);
            if (parseMi == null)
                throw new InvalidOperationException($"No static ParseOmniBuf(ReadOnlySpan<byte>) on {nativeRoot}");

            _parse = (ParseSpanDelegate)Delegate.CreateDelegate(typeof(ParseSpanDelegate), parseMi);
            _project = TryBindOpaqueProject(nativeRoot);
        }

        private static Func<byte[], List<double>> TryBindOpaqueProject(Type nativeRoot)
        {
            var opaqueType = nativeRoot.Assembly.GetType($"{nativeRoot.Namespace}.Opaque{nativeRoot.Name}");
            var ctor = opaqueType?.GetConstructor(new[] { typeof(byte[]) });
            if (ctor == null) return null;

            var pBytes = Expression.Parameter(typeof(byte[]), "b");
            var newOpaque = Expression.New(ctor, pBytes);

            var f0 = opaqueType.GetProperty("FFloat0", BindingFlags.Public | BindingFlags.Instance);
            if (f0 != null && f0.PropertyType == typeof(double))
            {
                var helper = typeof(OmniBufSerializerSer).GetMethod(nameof(Singleton), BindingFlags.NonPublic | BindingFlags.Static);
                var body = Expression.Call(helper, Expression.Property(newOpaque, f0));
                return Expression.Lambda<Func<byte[], List<double>>>(body, pBytes).Compile();
            }

            var itemsProp = opaqueType.GetProperty("Items", BindingFlags.Public | BindingFlags.Instance);
            if (itemsProp != null && itemsProp.PropertyType.IsGenericType
                && itemsProp.PropertyType.GetGenericTypeDefinition() == typeof(IReadOnlyList<>))
            {
                var rowType = itemsProp.PropertyType.GetGenericArguments()[0];
                var rowF0 = rowType.GetProperty("FFloat0", BindingFlags.Public | BindingFlags.Instance);
                if (rowF0 != null && rowF0.PropertyType == typeof(double))
                {
                    var pRow = Expression.Parameter(rowType, "r");
                    var getter = Expression.Lambda(
                        typeof(Func<,>).MakeGenericType(rowType, typeof(double)),
                        Expression.Property(pRow, rowF0),
                        pRow).Compile();
                    var helper = typeof(OmniBufSerializerSer)
                        .GetMethod(nameof(ProjectBatch), BindingFlags.NonPublic | BindingFlags.Static)
                        .MakeGenericMethod(rowType);
                    var body = Expression.Call(helper, Expression.Property(newOpaque, itemsProp), Expression.Constant(getter));
                    return Expression.Lambda<Func<byte[], List<double>>>(body, pBytes).Compile();
                }
            }

            return null;
        }

        private static List<double> Singleton(double v) => new List<double>(1) { v };

        private static List<double> ProjectBatch<TRow>(IReadOnlyList<TRow> rows, Func<TRow, double> getFloat0)
        {
            var outList = new List<double>(rows.Count);
            for (int i = 0; i < rows.Count; i++)
                outList.Add(getFloat0(rows[i]));
            return outList;
        }

        public override byte[] SerializeBytes(object serializable)
        {
            _encoder.Reset();
            _marshal(NativeOf(serializable), _encoder);
            return _encoder.ToArray();
        }

        public override object DeserializeBytes(byte[] serialized)
        {
            if (IsTableProject && _project != null)
                return _project(serialized);
            var msg = _parse(serialized);
            return IsTableProject ? ColumnarProject.FFloat0(msg) : msg;
        }

        public override void Serialize(object serializable, Stream outputStream)
        {
            _encoder.Reset();
            _marshal(NativeOf(serializable), _encoder);
            outputStream.Write(_encoder.WrittenSpan);
        }

        public override object Deserialize(Stream inputStream)
            => DeserializeBytes(ReadAll(inputStream, rewind: true));
    }
}
