package edu.coursera.parallel;

import java.util.concurrent.*;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {
    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
                                              final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
                                            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {

        final static int TS = 50000;

        /**
         * Starting index for traversal done by this task.
         */
        private final int start;
        /**
         * Ending index for traversal done by this task.
         */
        private final int end;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        /**
         * Intermediate value produced by this task.
         */
        private double value;

        /**
         * Constructor.
         * @param start Set the starting index to begin
         *        parallel traversal at.
         * @param end Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int start, final int end, final double[] setInput) {
            this.start = start;
            this.end = end;
            this.input = setInput;
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            // TODO

            if(this.end-this.start <= TS) {
                for (int i = this.start; i < this.end; i++) {
                    this.value += (1 / input[i]);
                }
            }
            else if(this.start > this.end) {
                this.value = 0;
            }
            else {
                ReciprocalArraySumTask right = new ReciprocalArraySumTask(this.start + (this.end-this.start)/2, this.end, input);
                ReciprocalArraySumTask left = new ReciprocalArraySumTask(this.start, this.start + (this.end-this.start)/2, input);

                left.fork();
                right.compute();
                left.join();

                this.value = left.value + right.value;
            }
        }
    }

    /**
     * TODO: Modify this method to compute the same reciprocal sum as
     * seqArraySum, but use two tasks running in parallel under the Java Fork
     * Join framework. You may assume that the length of the input array is
     * evenly divisible by 2.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;

        ReciprocalArraySumTask task = new ReciprocalArraySumTask(0, input.length, input);
        ForkJoinPool.commonPool().invoke(task);

        return task.getValue();
    }

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
        ReciprocalArraySumTask[] taskArray = new ReciprocalArraySumTask[numTasks];
        for(int i = 0; i < numTasks; i++) {
            taskArray[i] = new ReciprocalArraySumTask(getChunkStartInclusive(i, numTasks, input.length),
                    getChunkEndExclusive(i, numTasks, input.length), input);
        }

        for(int i = 1; i < numTasks; i++) {
            ForkJoinPool.commonPool().invoke(taskArray[i]);
        }
        taskArray[0].compute();

        double sum = 0;
        for(int i = 0; i < numTasks; i++) {
            sum += taskArray[i].getValue();
        }

        return sum;
    }
}
