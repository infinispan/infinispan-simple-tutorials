package org.infinispan.tutorial.simple.remote.marshalling;

import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MagazineMarshaller implements MessageMarshaller<Magazine> {
    @Override
    public Magazine readFrom(ProtoStreamReader reader) throws IOException {
        String name = reader.readString("name");
        YearMonth yearMonth = YearMonth.of(reader.readInt("publicationYear"), reader.readInt("publicationMonth"));
        List<String> stories = reader.readCollection("stories", new ArrayList<>(), String.class);
        return new Magazine(name, yearMonth, stories);
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, Magazine magazine) throws IOException {
        writer.writeString("name", magazine.name());
        YearMonth yearMonth = magazine.publicationDate();
        writer.writeInt("publicationYear", yearMonth.getYear());
        writer.writeInt("publicationMonth", yearMonth.getMonthValue());
        writer.writeCollection("stories", magazine.stories(), String.class);
    }

    @Override
    public Class<? extends Magazine> getJavaClass() {
        return Magazine.class;
    }

    @Override
    public String getTypeName() {
        return "magazine_sample.Magazine";
    }
}
