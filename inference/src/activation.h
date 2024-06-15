#ifndef ACTIVATION_H_
#define ACTIVATION_H_

static inline double mse(double activation, double expected) {
    double diff = activation - expected;
    return diff * diff;
}

static inline double mse_derivative(double activation, double expected) {
    return 2 * (activation - expected);
}

#endif // !ACTIVATION_H_
