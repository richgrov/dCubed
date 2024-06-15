add_executable(inference
	"linear.c"
	"linear.h"
	"main.c"
	"rand.h"
)

set_property(TARGET inference PROPERTY C_STANDARD 11)
