package pl.lodz.p.michalsosn.entities.specification;

import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

/**
 * @author Michał Sośnicki
 */
public class DoubleParameterSpecification extends ParameterSpecification {
    private double min;
    private double max;

    DoubleParameterSpecification() {
    }

    public <T> DoubleParameterSpecification(
            Class<T> tClass, String name, double min, double max
    ) throws NoSuchFieldException {
        super(tClass, name);
        if (getField().getType() != double.class) {
            throw new NoSuchFieldException("Wrong field type");
        }
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
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

