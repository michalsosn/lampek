package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegerParameterSpecification extends ParameterSpecification {
    private int min;
    private int max;
    private Integer def;

    IntegerParameterSpecification() {
    }

    public <T> IntegerParameterSpecification(
            Class<T> tClass, String name, int min, int max, Integer def
    ) throws NoSuchFieldException {
        this(null, tClass, name, min, max, def);
    }

    public IntegerParameterSpecification(String description, Class tClass,
                                         String name, int min, int max, Integer def
    ) throws NoSuchFieldException {
        super(description, tClass, name);
        if (getField().getType() != int.class) {
            throw new NoSuchFieldException("Wrong field type");
        }
        this.min = min;
        this.max = max;
        this.def = def;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public Integer getDef() {
        return def;
    }

    @Override
    public ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    ) {
        int intValue = (Integer) getValue(object);
        if (intValue < min) {
            throw new IllegalArgumentException(intValue + " < " + min);
        }
        if (intValue > max) {
            throw new IllegalArgumentException(intValue + " > " + max);
        }
        return new ArgumentEntity.IntegerArgumentEntity(intValue);
    }

    @Override
    public void applyArgumentEntity(Object object,
                                    ArgumentEntity argument) {
        ArgumentEntity.IntegerArgumentEntity integerArgument
                = (ArgumentEntity.IntegerArgumentEntity) argument;
        setValue(object, integerArgument.getValue());
    }
}

