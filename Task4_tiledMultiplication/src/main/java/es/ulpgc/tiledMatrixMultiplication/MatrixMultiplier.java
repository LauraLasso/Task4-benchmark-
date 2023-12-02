package es.ulpgc.tiledMatrixMultiplication;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MatrixMultiplier {
    public List<int[][]> multiplyMatrix(MatrixBlock[] a, MatrixBlock[] b) throws ExecutionException, InterruptedException;
}
