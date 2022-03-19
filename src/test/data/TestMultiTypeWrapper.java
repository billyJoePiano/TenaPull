package data;

import nessusTools.data.entity.*;
import nessusTools.data.persistence.*;

import java.math.*;
import java.util.*;

import org.junit.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestMultiTypeWrapper {
    Map<String, Object> TEST_BOTH_DIRECTIONS = Collections.unmodifiableMap(
            new LinkedHashMap() { {
                    put(null, null);
                    put("STesting string conversion", "Testing string conversion");
                    put("i123",                 Integer.valueOf(123));
                    put("i-123",                Integer.valueOf(-123));
                    put("i0",                   Integer.valueOf(0));
                    put("i-2147483648",         Integer.valueOf(-2147483648));
                    put("i-2147483647",         Integer.valueOf(-2147483647));
                    put("Btrue",                Boolean.valueOf(true));
                    put("Bfalse",               Boolean.valueOf(false));
                    put("d1.0",                 Double.valueOf(1.0));
                    put("d-123.0",              Double.valueOf(-123.0));
                    put("d0.0",                 Double.valueOf(0.0));
                    put("d-0.0",                Double.valueOf(-0.0));
                    put("d4.9E-324",            Double.valueOf(4.9e-324d));
                    put("d1.7976931348623157E308", Double.valueOf(1.79769313486231570e+308d));
                    put("d-4.9E-324",           Double.valueOf(-4.9e-324d));
                    put("d-1.7976931348623157E308", Double.valueOf(-1.79769313486231570e+308d));
                    put("l0",                   Long.valueOf(0));
                    put("l-9223372036854775808", Long.valueOf(-9223372036854775808l));
                    put("l9223372036854775807", Long.valueOf(9223372036854775807l));
                    put("b0",                   Byte.valueOf((byte) 0));
                    put("b-128",                Byte.valueOf((byte) -128));
                    put("b127",                 Byte.valueOf((byte) 127));
                    put("s0",                   Short.valueOf((short) 0));
                    put("s-32768",              Short.valueOf((short) -32768));
                    put("s32767",               Short.valueOf((short) 32767));
                    put("f1.4E-45",             Float.valueOf(1.4e-45f));
                    put("f-1.4E-45",            Float.valueOf(-1.4e-45f));
                    put("f3.4028235E38",        Float.valueOf(3.4028235e+38f));
                    put("f-3.4028235E38",       Float.valueOf(-3.4028235e+38f));
                    put("I1234567890987654321234567890987654321", new BigInteger("1234567890987654321234567890987654321"));
                    put("I-1234567890987654321234567890987654321", new BigInteger("-1234567890987654321234567890987654321"));
                    put("D1234567890987654321234567890987654321.1234567890987654321234567890987654321",
                        new BigDecimal("1234567890987654321234567890987654321.1234567890987654321234567890987654321"));
                    put("D-1234567890987654321234567890987654321.1234567890987654321234567890987654321",
                        new BigDecimal("-1234567890987654321234567890987654321.1234567890987654321234567890987654321"));
                    put("Uclass nessusTools.data.entity.ScanType\nthis is a test",
                            MultiTypeWrapper.wrap(createUnknownType()));

        }});

    Map<String, Object> TEST_FROM_DB = Map.of(
            "b-0", Byte.valueOf((byte)-0),
            "s-0", Short.valueOf((short)-0),
            "i-0", Integer.valueOf(-0),
            "l-0", Long.valueOf(-0),

            "BtestingRandoString", Boolean.valueOf(false),
            "not a valid type init", "not a valid type init"
        );

    Map<Object, String> TEST_TO_DB = Map.of(
            createUnknownType(), "Uclass nessusTools.data.entity.ScanType\nthis is a test"
        );

    private static Object createUnknownType() {
        ScanType o = new ScanType();
        o.setValue("this is a test");
        return o;
    }


    private MultiTypeWrapper.Converter converter
                                = new MultiTypeWrapper.Converter();

    @Test
    public void testBothDirections() {

        boolean firstPolarity = Math.random() < 0.5;
        boolean secondPolarity = Math.random() < 0.5;

        System.out.println();
        System.out.println("Test firstPolarity: "
                + (firstPolarity ? "toDb first" : "fromDb first"));

        System.out.println("Test secondPolarity: "
                + (secondPolarity ? "backToDb first" : "backFromDb first"));

        for (Map.Entry<String, Object> test : TEST_BOTH_DIRECTIONS.entrySet()) {
            String str = test.getKey();
            Object obj =  test.getValue();

            String toDb;
            MultiTypeWrapper fromDb;

            if (firstPolarity) {
                toDb = converter.convertToDatabaseColumn(MultiTypeWrapper.wrap(obj));
                fromDb = converter.convertToEntityAttribute(str);
            } else {
                fromDb = converter.convertToEntityAttribute(str);
                toDb = converter.convertToDatabaseColumn(MultiTypeWrapper.wrap(obj));
            }

            System.out.println();
            System.out.println(toDb);
            if (fromDb != null) {
                Object tmp = fromDb.getObject();
                Class type = tmp != null ? tmp.getClass() : void.class;
                System.out.println(type + " : \t " + fromDb.toString());

            } else {
                System.out.println((Object) null);
            }

            assertEquals(str, toDb);

            if (fromDb != null) {
                assertEquals(obj, fromDb.getObject());
                assertEquals(str, fromDb.toDb());
            } else {
                assertNull(obj);
                assertNull(str);
                assertNull(toDb);
            }

            String backToDb;
            MultiTypeWrapper backFromDb;

            if (secondPolarity) {
                backToDb = converter.convertToDatabaseColumn(fromDb);
                backFromDb = converter.convertToEntityAttribute(toDb);

            } else {
                backFromDb = converter.convertToEntityAttribute(toDb);
                backToDb = converter.convertToDatabaseColumn(fromDb);
            }

            assertEquals(str, backToDb);
            assertEquals(toDb, backToDb);

            if (backFromDb != null) {
                assertEquals(obj, backFromDb.getObject());
                assertEquals(fromDb, backFromDb.getObject());
                assertEquals(str, backFromDb.toDb());
                assertEquals(toDb, backFromDb.toDb());
                assertEquals(backToDb, backFromDb.toDb());

            } else {
                assertNull(obj);
                assertNull(fromDb);
                assertNull(str);
                assertNull(toDb);
                assertNull(backToDb);
            }
        }
    }

    @Test
    public void testToDb() {
        for (Map.Entry<Object, String> test : TEST_TO_DB.entrySet()) {
            String str = test.getValue();
            Object obj =  test.getKey();

            String toDb = converter.convertToDatabaseColumn(MultiTypeWrapper.wrap(obj));

            System.out.println();
            System.out.println(toDb);
            if (obj != null) {
                System.out.println(obj.getClass() + " : \t " + obj.toString());

            } else {
                System.out.println((Object) null);
            }

            assertEquals(str, toDb);
        }
    }

    @Test
    public void testFromDb() {
        for (Map.Entry<String, Object> test : TEST_FROM_DB.entrySet()) {
            String str = test.getKey();
            Object obj =  test.getValue();

            MultiTypeWrapper fromDb = converter.convertToEntityAttribute(str);
            Object objFromDb = fromDb.getObject();

            System.out.println();
            System.out.println(str);
            if (fromDb != null) {
                System.out.println(objFromDb.getClass() + " : \t " + objFromDb.toString());

            } else {
                System.out.println((Object) null);
            }

            assertEquals(obj, fromDb.getObject());
        }
    }

}
