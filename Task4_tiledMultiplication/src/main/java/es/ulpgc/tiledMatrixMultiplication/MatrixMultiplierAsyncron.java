package es.ulpgc.tiledMatrixMultiplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixMultiplierAsyncron implements MatrixMultiplier {
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
    public List<int[][]> multiplyMatrix(MatrixBlock[] matrixA, MatrixBlock[] matrixB) throws ExecutionException, InterruptedException {
        List<int[][]> resultMatrix = new ArrayList<>();
        List<CompletableFuture<int[][]>> futures = new ArrayList<>();

        for (List<Integer> blocks : blocks_to_multiply) {
            futures.add(multiplyMatricesWithCompletableFuture(matrixA[blocks.get(0)], matrixB[blocks.get(1)]));
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<int[][]> pairsOfBlocks = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        for (int i = 0; i < pairsOfBlocks.size(); i += 2) {
            int[][] result = sumPairsOfBlocks(Arrays.asList(pairsOfBlocks.get(i), pairsOfBlocks.get(i + 1)));
            resultMatrix.add(result);
        }

        allOf.get();

        return resultMatrix;
    }

    public int[][] sumPairsOfBlocks(List<int[][]> listOfMatrix) {
        int filas = listOfMatrix.get(0).length;
        int columnas = listOfMatrix.get(0)[0].length;

        int[][] resultado = new int[filas][columnas];

        for (int[][] matriz : listOfMatrix) {
            sumMatrix(resultado, matriz);
        }

        return resultado;
    }

    private void sumMatrix(int[][] resultado, int[][] matriz) {
        int filas = resultado.length;
        int columnas = resultado[0].length;

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                resultado[i][j] += matriz[i][j];
            }
        }
    }

    public CompletableFuture<int[][]> multiplyMatricesWithCompletableFuture(MatrixBlock blockA, MatrixBlock blockB) {
        int[][] matrixA = blockA.getBlock();
        int[][] matrixB = blockB.getBlock();

        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] result = new int[rowsA][colsB];

        CompletableFuture<int[][]>[] futures = new CompletableFuture[rowsA * colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                int finalI = i;
                int finalJ = j;
                futures[i * colsB + j] = CompletableFuture.supplyAsync(() -> {
                    int[][] partialResult = new int[1][1];
                    partialResult[0][0] = IntStream.range(0, colsA)
                            .map(k -> matrixA[finalI][k] * matrixB[k][finalJ])
                            .sum();
                    return partialResult;
                });
            }
        }

        CompletableFuture<int[][]> combinedFuture = CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    int[][] combinedResult = new int[rowsA][colsB];
                    for (int i = 0; i < rowsA; i++) {
                        for (int j = 0; j < colsB; j++) {
                            combinedResult[i][j] = futures[i * colsB + j].join()[0][0];
                        }
                    }
                    return combinedResult;
                });

        return combinedFuture;
    }
}
