#include "linear.h"

#include <stdlib.h>

#include "rand.h"

void linear_init(Linear *linear, int num_in, int num_out) {
    linear->num_in = num_in;
    linear->num_out = num_out;
    linear->weights = malloc(sizeof(double) * num_in * num_out);
    linear->biases = malloc(sizeof(double) * num_out);
    linear->outputs = malloc(sizeof(double) * num_out);
}

void linear_deinit(Linear *linear) {
    free(linear->weights);
    linear->weights = NULL;
    free(linear->outputs);
    linear->outputs = NULL;
}

void linear_randinit(Linear *linear, double min, double max) {
    for (int i = 0; i < linear->num_in * linear->num_out; ++i) {
        linear->weights[i] = rand_double(min, max);
    }

    for (int i = 0; i < linear->num_out; ++i) {
        linear->biases[i] = rand_double(min, max);
    }
}

void linear_forward(Linear *linear, double *inputs) {
    for (int out = 0; out < linear->num_out; ++out) {
        double sum = linear->biases[out];
        for (int in = 0; in < linear->num_in; ++in) {
            sum += inputs[in] * linear->weights[out * linear->num_in + in];
        }
        linear->outputs[out] = sum;
    }
}

void linear_backward(Linear *linear, double *inputs, double *output_gradients, double lr) {
    for (int out = 0; out < linear->num_out; ++out) {
        for (int in = 0; in < linear->num_in; ++in) {
            int this_weight = out * linear->num_in + in;
            double gradient = inputs[in] * output_gradients[out];
            linear->weights[this_weight] -= gradient * lr;
        }
    }
}
