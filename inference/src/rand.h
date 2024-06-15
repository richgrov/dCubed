#ifndef RAND_H_
#define RAND_H_

#include <stdlib.h>

static inline double rand_double(double min, double max) {
    double range = max - min;
    double r = (double)rand() / (double)RAND_MAX;
    return min + r * range;
}

#endif // !RAND_H_
