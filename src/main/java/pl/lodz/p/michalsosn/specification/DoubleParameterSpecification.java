package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoubleParameterSpecification extends ParameterSpecification {
    private double min;
    private double max;
    private Double def;

    DoubleParameterSpecification() {
    }

    public <T> DoubleParameterSpecification(
            Class<T> tClass, String name, double min, double max, Double def
    ) throws NoSuchFieldException {
        this(null, tClass, name, min, max, def);
    }

    public DoubleParameterSpecification(String description,
                                        Class tClass, String name,
                                        double min, double max, Double def
    ) throws NoSuchFieldException {
        super(description, tClass, name);
        if (getField().getType() != double.class) {
            throw new NoSuchFieldException("Wrong field type");
        }
        this.min = min;
        this.max = max;
        this.def = def;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public Double getDef() {
        return def;
    }

    @Override
    public ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    ) {
        double doubleValue = (Double) getValue(object);
        if (doubleValue < min) {
            throw new IllegalArgumentException(doubleValue + " < " + min);
        }
        if (doubleValue > max) {
            throw new IllegalArgumentException(doubleValue + " > " + max);
        }
        return new ArgumentEntity.DoubleArgumentEntity(doubleValue);
    }

    @Override
    public void applyArgumentEntity(Object object,
                                    ArgumentEntity argument) {
        ArgumentEntity.DoubleArgumentEntity doubleArgument
                = (ArgumentEntity.DoubleArgumentEntity) argument;
        setValue(object, doubleArgument.getValue());
    }
}

