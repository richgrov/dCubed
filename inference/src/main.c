#include <stdio.h>
#include <stdlib.h>
#include <time.h>

double rand_double(double min, double max) {
    double range = max - min;
    double r = (double)rand() / (double)RAND_MAX;
    return min + r * range;
}

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

double mse(double activation, double expected) {
    double diff = activation - expected;
    return diff * diff;
}

double mse_derivative(double activation, double expected) {
    return 2 * (activation - expected);
}

#define ARRAY_LEN(x) (sizeof(x) / sizeof(x[0]))

typedef struct {
    double input[2];
    double output;
} Datapoint;

int main(int argc, char **argv) {
    srand(time(NULL));

    Datapoint dataset[100];
    for (int i = 0; i < ARRAY_LEN(dataset); ++i) {
        Datapoint e;
        e.input[0] = rand_double(0.0, 1.0);
        e.input[1] = rand_double(0.0, 1.0);
        e.output = (e.input[0] + e.input[1]) / 2;
        dataset[i] = e;
    }

    Linear linear;
    init_linear(&linear, 2, 1);
    linear_randinit(&linear, 0.0, 1.0);
    linear.biases[0] = 0.0;

    for (int i = 0; i < ARRAY_LEN(dataset); ++i) {
        Datapoint *entry = &dataset[i];
        linear_forward(&linear, entry->input);
        double out = linear.outputs[0];
        double cost = mse(out, entry->output);

        double activation_gradient = mse_derivative(out, entry->output);
        linear_backward(&linear, entry->input, &activation_gradient, 0.1);
        printf("avg(%f, %f) is about %f (cost %f)\n", entry->input[0], entry->input[1], out, cost);
    }

    deinit_linear(&linear);
}
