package pl.lodz.p.michalsosn.entities.specification;

import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

/**
 * @author Michał Sośnicki
 */
public class MatrixParameterSpecification extends ParameterSpecification {
    MatrixParameterSpecification() {
    }

    public <T> MatrixParameterSpecification(
            Class<T> tClass, String name
    ) throws NoSuchFieldException {
        super(tClass, name);
        if (getField().getType() != double[][].class) {
            throw new NoSuchFieldException("Wrong field type");
        }
    }

    @Override
    public ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    ) {
        double[][] matrix = (double[][]) getValue(object);
        return new ArgumentEntity.MatrixArgumentEntity(matrix);
    }

    @Override
    public void applyArgumentEntity(Object object,
                                    ArgumentEntity argument) {
        ArgumentEntity.MatrixArgumentEntity matrixArgument
                = (ArgumentEntity.MatrixArgumentEntity) argument;
        setValue(object, matrixArgument.getMatrix());
    }
}

