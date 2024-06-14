#include <stdio.h>
#include <stdlib.h>

typedef struct {
    int num_in;
    int num_out;
    // Represented in input-major order
    double *weights;
    double *biases;
    double *outputs;
} Linear;

void init_linear(Linear *linear, int num_in, int num_out) {
    linear->num_in = num_in;
    linear->num_out = num_out;
    linear->weights = malloc(sizeof(double) * num_in * num_out);
    linear->biases = malloc(sizeof(double) * num_out);
    linear->outputs = malloc(sizeof(double) * num_out);
}

void deinit_linear(Linear *linear) {
    free(linear->weights);
    linear->weights = NULL;
    free(linear->outputs);
    linear->outputs = NULL;
}

void linear_forward(Linear *linear, double *input) {
    for (int out = 0; out < linear->num_out; ++out) {
        double sum = linear->biases[out];
        for (int in = 0; in < linear->num_in; ++in) {
            sum += input[in] * linear->weights[out * linear->num_in + in];
        }
        linear->outputs[out] = sum;
    }
}

int main(int argc, char **argv) {
    Linear linear;
    init_linear(&linear, 2, 1);
    linear.weights[0] = 0.5;
    linear.weights[1] = 0.5;
    linear.biases[0] = 1.5;

    double inputs[] = {1, 2};
    linear_forward(&linear, inputs);

    printf("Result: %f", linear.outputs[0]);

    deinit_linear(&linear);
}
