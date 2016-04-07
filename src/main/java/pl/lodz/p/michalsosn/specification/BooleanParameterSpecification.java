package pl.lodz.p.michalsosn.specification;

import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

/**
 * @author Michał Sośnicki
 */
public class BooleanParameterSpecification extends ParameterSpecification {

    BooleanParameterSpecification() {
    }

    public <T> BooleanParameterSpecification(
            Class<T> tClass, String name
    ) throws NoSuchFieldException {
        this(null, tClass, name);
    }

    public BooleanParameterSpecification(String description,
                                         Class tClass, String name
    ) throws NoSuchFieldException {
        super(description, tClass, name);
        if (getField().getType() != boolean.class) {
            throw new NoSuchFieldException("Wrong field type");
        }
    }

    @Override
    public ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    ) {
        boolean booleanValue = (Boolean) getValue(object);
        return new ArgumentEntity.BooleanArgumentEntity(booleanValue);
    }

    @Override
    public void applyArgumentEntity(Object object,
                                    ArgumentEntity argument) {
        ArgumentEntity.BooleanArgumentEntity booleanArgument
                = (ArgumentEntity.BooleanArgumentEntity) argument;
        setValue(object, booleanArgument.getValue());
    }
}

