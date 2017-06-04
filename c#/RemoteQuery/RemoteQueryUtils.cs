using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Google.Protobuf;
using Org.Infinispan.Protostream;
using Org.Infinispan.Query.Remote.Client;

namespace Infinispan.HotRod
{
    class RemoteQueryUtils
    {
        public static List<T> unwrapResults<T>(QueryResponse resp) where T : IMessage<T>
        {
            List<T> result = new List<T>();
            if (resp.ProjectionSize > 0)
            {  // Query has select
                return result;
            }
            for (int i = 0; i < resp.NumResults; i++)
            {
                WrappedMessage wm = resp.Results.ElementAt(i);

                if (wm.WrappedBytes != null)
                {
                    WrappedMessage wmr = WrappedMessage.Parser.ParseFrom(wm.WrappedBytes);
                    if (wmr.WrappedMessageBytes != null)
                    {
                        System.Reflection.PropertyInfo pi = typeof(T).GetProperty("Parser");

                        MessageParser<T> p = (MessageParser<T>)pi.GetValue(null);
                        T u = p.ParseFrom(wmr.WrappedMessageBytes);
                        result.Add(u);
                    }
                }
            }
            return result;
        }

        public static List<Object[]> unwrapWithProjection(QueryResponse resp)
        {
            List<Object[]> result = new List<Object[]>();
            if (resp.ProjectionSize == 0)
            {
                return result;
            }
            for (int i = 0; i < resp.NumResults; i++)
            {
                Object[] projection = new Object[resp.ProjectionSize];
                for (int j = 0; j < resp.ProjectionSize; j++)
                {
                    WrappedMessage wm = resp.Results.ElementAt(i * resp.ProjectionSize + j);
                    switch (wm.ScalarOrMessageCase)
                    {
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedDouble:
                            projection[j] = wm.WrappedDouble;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedFloat:
                            projection[j] = wm.WrappedFloat;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedInt64:
                            projection[j] = wm.WrappedInt64;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedUInt64:
                            projection[j] = wm.WrappedUInt64;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedInt32:
                            projection[j] = wm.WrappedInt32;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedFixed64:
                            projection[j] = wm.WrappedFixed64;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedFixed32:
                            projection[j] = wm.WrappedFixed32;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedBool:
                            projection[j] = wm.WrappedBool;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedString:
                            projection[j] = wm.WrappedString;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedBytes:
                            projection[j] = wm.WrappedBytes;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedUInt32:
                            projection[j] = wm.WrappedUInt32;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedSFixed32:
                            projection[j] = wm.WrappedSFixed32;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedSFixed64:
                            projection[j] = wm.WrappedSFixed64;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedSInt32:
                            projection[j] = wm.WrappedSInt32;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedSInt64:
                            projection[j] = wm.WrappedSInt64;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedDescriptorFullName:
                            projection[j] = wm.WrappedDescriptorFullName;
                            break;
                        case WrappedMessage.ScalarOrMessageOneofCase.WrappedMessageBytes:
                            projection[j] = wm.WrappedMessageBytes;
                            break;
                    }
                }
                result.Add(projection);
            }
            return result;
        }
    }
}
