package com.github.dstendardi.avroreflection;

import com.viadeo.avro.reflection.NullableDateTimeEncoding;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.MyReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomAvroReflectionTest {

    MyReflectData MY_REFLECT_DATA = new MyReflectData().addEncorder(DateTime.class, "inst", new NullableDateTimeEncoding());

    private BloatedEvent event = new BloatedEvent(new DateTime("2012-10-12"), "toto");


    public static class BloatedEvent {

        private DateTime dateTime;
        private String toto;

        public BloatedEvent() {
        }

        public BloatedEvent(DateTime dateTime, String toto) {
            this.dateTime = dateTime;
            this.toto = toto;
        }

        public DateTime getDateTime() {
            return dateTime;
        }

        public String getToto() {
            return toto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BloatedEvent that = (BloatedEvent) o;

            if (dateTime != null ? !dateTime.equals(that.dateTime) : that.dateTime != null) return false;
            if (toto != null ? !toto.equals(that.toto) : that.toto != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = dateTime != null ? dateTime.hashCode() : 0;
            result = 31 * result + (toto != null ? toto.hashCode() : 0);
            return result;
        }
    }

    Schema SCHEMA = MY_REFLECT_DATA.getSchema(BloatedEvent.class);

    @Test
    public void schema_with_custom_encoder() throws Exception {

        assertNotNull(SCHEMA.getField("dateTime"));
        Schema dateTimeSchema = SCHEMA.getField("dateTime").schema().getTypes().get(1);
        Schema.Type dateTime = dateTimeSchema.getType();
        assertEquals(dateTime, Schema.Type.STRING);
        assertEquals("inst", dateTimeSchema.getProp("tag"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void serialization_with_custom_encoder() throws Exception {
        ReflectDatumWriter<BloatedEvent> writer = (ReflectDatumWriter<BloatedEvent>) MY_REFLECT_DATA.createDatumWriter(SCHEMA);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().jsonEncoder(SCHEMA, os);
        writer.write(event, encoder);
        encoder.flush();
        os.flush();

        assertEquals("{\"dateTime\":{\"string\":\"2012-10-12T00:00:00.000+02:00\"},\"toto\":{\"string\":\"toto\"}}", os.toString("UTF-8"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deserialization_with_custom_encoder() throws IOException {

        ReflectDatumReader<BloatedEvent> reader = (ReflectDatumReader<BloatedEvent>) MY_REFLECT_DATA.createDatumReader(SCHEMA);
        BloatedEvent actual = reader.read(null, DecoderFactory.get().jsonDecoder(SCHEMA, "{\"dateTime\":{\"string\":\"2012-10-12T00:00:00.000+02:00\"},\"toto\":{\"string\":\"toto\"}}"));

        assertEquals(event, actual);
    }

}
