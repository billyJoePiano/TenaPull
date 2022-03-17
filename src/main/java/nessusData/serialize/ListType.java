package nessusData.serialize;

import nessusData.entity.template.Pojo;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ListType {
    Class<? extends Pojo> type();
    Class<? extends AbstractContextualDeserializer> using();
}
