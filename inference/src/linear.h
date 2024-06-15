#ifndef LINEAR_H_
#define LINEAR_H_

#include <stdbool.h>

typedef struct {
    int num_in;
    int num_out;
    // Represented in input-major order
    double *weights;
    double *biases;
    double *outputs;
} Linear;

void linear_init(Linear *linear, int num_in, int num_out);

void linear_deinit(Linear *linear);

void linear_randinit(Linear *linear, double min, double max);

void linear_forward(Linear *linear, double *inputs);

void linear_backward(Linear *linear, double *inputs, double *output_gradients, double lr);

#endif // !LINEAR_H_
