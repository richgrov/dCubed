#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "activation.h"
#include "linear.h"
#include "rand.h"

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
    linear_init(&linear, 2, 1);
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

    linear_deinit(&linear);
}
