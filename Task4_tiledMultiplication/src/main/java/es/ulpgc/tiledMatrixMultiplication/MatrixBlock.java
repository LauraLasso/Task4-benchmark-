package es.ulpgc.tiledMatrixMultiplication;

public class MatrixBlock {
    private int[][] block;

    public MatrixBlock(int[][] block) {
        this.block = block;
    }

    public int[][] getBlock() {
        return block;
    }

    public void setBlock(int[][] block) {
        this.block = block;
    }
    private int[][] getSubMatrix(int[][] matrix, int startRow, int startCol, int endRow, int endCol) {
        int[][] subMatrix = new int[endRow - startRow][endCol - startCol];

        for (int i = startRow; i < endRow; i++) {
            for (int j = startCol; j < endCol; j++) {
                subMatrix[i - startRow][j - startCol] = matrix[i][j];
            }
        }

        return subMatrix;
    }
    public MatrixBlock[] divideMatrixIntoBlocks(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        int halfRows = rows / 2;
        int halfCols = cols / 2;

        if (rows % 2 != 0 || cols % 2 != 0) {
            halfRows = (int) Math.ceil(rows / 2);
            halfCols = (int) Math.ceil(cols / 2);

        }

        MatrixBlock[] blocks = new MatrixBlock[4];

        blocks[0] = new MatrixBlock(getSubMatrix(matrix, 0, 0, halfRows, halfCols));
        blocks[1] = new MatrixBlock(getSubMatrix(matrix, 0, halfCols, halfRows, cols));
        blocks[2] = new MatrixBlock(getSubMatrix(matrix, halfRows, 0, rows, halfCols));
        blocks[3] = new MatrixBlock(getSubMatrix(matrix, halfRows, halfCols, rows, cols));

        return blocks;
    }

}
