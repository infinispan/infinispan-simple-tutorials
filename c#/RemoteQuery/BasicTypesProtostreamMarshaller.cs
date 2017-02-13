using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SampleBankAccount;
using Infinispan.HotRod;
using Google.Protobuf;
using Infinispan.Hotrod.Protobuf;
using Org.Infinispan.Protostream;
using Quickstart;

namespace Infinispan.HotRod
{
    class BasicTypesProtoStreamMarshaller : IMarshaller
    {
        public bool IsMarshallable(object o)
        {
            throw new NotImplementedException();
        }

        public object ObjectFromByteBuffer(byte[] buf)
        {
            WrappedMessage wm = WrappedMessage.Parser.ParseFrom(buf);
            switch (wm.WrappedDescriptorId)
            {
                case 42:
                    return Person.Parser.ParseFrom(wm.WrappedMessageBytes);
            }
            base_types bt = base_types.Parser.ParseFrom(buf);
            return bt;
            throw new NotImplementedException();
        }

        public object ObjectFromByteBuffer(byte[] buf, int offset, int length)
        {
            throw new NotImplementedException();
        }

        public byte[] ObjectToByteBuffer(object obj)
        {
            if (obj is String)
            {
                return StringToByteBuffer((String)obj);
            }
            if (obj is int)
            {
                return IntToByteBuffer((int)obj);
            }
            if (obj is Person)
            {
                return ObjectToByteBuffer(42, obj);
            }
            throw new NotImplementedException();
        }

        private byte[] ObjectToByteBuffer(int descriptorId, object obj)
        {
            IMessage u = (IMessage)obj;

            int size = u.CalculateSize();
            byte[] bytes = new byte[size];
            CodedOutputStream cos = new CodedOutputStream(bytes);
            u.WriteTo(cos);
            
            cos.Flush();
            WrappedMessage wm = new WrappedMessage();
            wm.WrappedMessageBytes = ByteString.CopyFrom(bytes);
            wm.WrappedDescriptorId = descriptorId;

            byte[] msgBytes = new byte[wm.CalculateSize()];
            CodedOutputStream msgCos = new CodedOutputStream(msgBytes);
            wm.WriteTo(msgCos);
            msgCos.Flush();
            return msgBytes;
        }

        private byte[] StringToByteBuffer(string str)
        {
            int t = CodedOutputStream.ComputeTagSize(9);
            int s = CodedOutputStream.ComputeStringSize(str);

            s += t;
            byte[] bytes = new byte[s];
            CodedOutputStream cos = new CodedOutputStream(bytes);
            cos.WriteTag((9 << 3) + 2);
            cos.WriteString(str);
            cos.Flush();
            return bytes;
        }

        private byte[] IntToByteBuffer(int i)
        {
            int t = CodedOutputStream.ComputeTagSize(5);
            int s = CodedOutputStream.ComputeInt32Size(i);

            s += t;
            byte[] bytes = new byte[s];
            CodedOutputStream cos = new CodedOutputStream(bytes);
            cos.WriteTag((5 << 3) + 0);
            cos.WriteInt32(i);
            cos.Flush();
            return bytes;
        }

        public byte[] ObjectToByteBuffer(object obj, int estimatedSize)
        {
            throw new NotImplementedException();
        }
    }
}
