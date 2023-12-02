package es.ulpgc.tiledMatrixMultiplication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixMultiplierWithStreams implements MatrixMultiplier{

    static List<List<Integer>> blocks_to_multiply = new ArrayList<>(Arrays.asList(
            Arrays.asList(0, 0),
            Arrays.asList(1, 2),
            Arrays.asList(0, 1),
            Arrays.asList(1, 3),
            Arrays.asList(2, 0),
            Arrays.asList(3, 2),
            Arrays.asList(2, 1),
            Arrays.asList(3, 3)
    ));

    @Override
    public List<int[][]> multiplyMatrix(MatrixBlock[] matrixA, MatrixBlock[] matrixB) {
        List<int[][]> resultMatrix = new ArrayList<>();
        List<int[][]> pairsOfBlocks = new ArrayList<>();

        IntStream.range(0, blocks_to_multiply.size())
                .mapToObj(i -> blocks_to_multiply.get(i))
                .forEach(pair -> {
                    pairsOfBlocks.add(multiplyMatricesWithStreams(matrixA[pair.get(0)], matrixB[pair.get(1)]));

                    if (pairsOfBlocks.size() % 2 == 0) {
                        int[][] result = sumMatrix(pairsOfBlocks);
                        resultMatrix.add(result);
                        pairsOfBlocks.clear();
                    }
                });

        return resultMatrix;
    }

    public int[][] sumMatrix(List<int[][]> listOfMatrix) {
        int filas = listOfMatrix.get(0).length;
        int columnas = listOfMatrix.get(0)[0].length;

        int[][] resultado = new int[filas][columnas];

        listOfMatrix.stream()
                .flatMapToInt(matriz -> IntStream.range(0, filas)
                        .flatMap(i -> IntStream.range(0, columnas-1)
                                .map(j -> matriz[i][j])
                        )
                )
                .forEachOrdered(valor -> resultado[valor / columnas][valor % columnas] += 1);

        return resultado;
    }

    public int[][] multiplyMatricesWithStreams(MatrixBlock blockA, MatrixBlock blockB) {
        int[][] matrixA = blockA.getBlock();
        int[][] matrixB = blockB.getBlock();

        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] result = new int[rowsA][colsB];

        IntStream.range(0, rowsA).parallel().forEachOrdered(i -> {
            for (int j = 0; j < colsB; j++) {
                final int colB = j;
                result[i][colB] = IntStream.range(0, colsA)
                        .map(k -> matrixA[i][k] * matrixB[k][colB])
                        .sum();
            }
        });

        return result;
    }
}
