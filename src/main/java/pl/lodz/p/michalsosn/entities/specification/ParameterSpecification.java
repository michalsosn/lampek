package pl.lodz.p.michalsosn.entities.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

import java.lang.reflect.Field;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * @author Michał Sośnicki
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes(value = {
        @Type(name = "IMAGE", value = ImageParameterSpecification.class),
        @Type(name = "INTEGER", value = IntegerParameterSpecification.class),
        @Type(name = "DOUBLE", value = DoubleParameterSpecification.class),
        @Type(name = "MATRIX", value = MatrixParameterSpecification.class),
})
public abstract class ParameterSpecification {

    @JsonIgnore
    private Field field;

    ParameterSpecification() {
    }

    public ParameterSpecification(Class tClass, String name)
            throws NoSuchFieldException {
        this.field = tClass.getDeclaredField(name);
    }

    public abstract ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    );

    public abstract void applyArgumentEntity(Object object,
                                             ArgumentEntity argument);

    public Object getValue(Object object) {
        return getValue(object, field);
    }

    public Object getValue(Object object, Field field) {
        boolean isAccessible = field.isAccessible();
        if (!isAccessible) {
            field.setAccessible(true);
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } finally {
            if (!isAccessible) {
                field.setAccessible(false);
            }
        }
    }

    public void setValue(Object object, Object value) {
        setValue(object, value, field);
    }

    public void setValue(Object object, Object value, Field field) {
        boolean isAccessible = field.isAccessible();
        if (!isAccessible) {
            field.setAccessible(true);
        }
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } finally {
            if (!isAccessible) {
                field.setAccessible(false);
            }
        }
    }

    public Field getField() {
        return field;
    }

}
