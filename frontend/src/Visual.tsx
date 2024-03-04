import { useEffect, useRef, useState } from "react";
import {
  PlayIcon,
  ForwardIcon,
  BackwardIcon,
  PauseIcon,
  InformationCircleIcon,
} from "@heroicons/react/16/solid";
import { IconButton } from "./Button";
import CubeScene from "./CubeScene";
import { AppState, Move, MoveMarker, SolveStageIndices } from "./model";

type MoveState = {
  currentMove: number;
  moves: Move[];
  stages: SolveStageIndices;
  markers: Record<number, MoveMarker>;
};

export default function Visual(props: { appState: AppState }) {
  const wrapperEl = useRef<HTMLDivElement>(null);
  const canvasEl = useRef<HTMLCanvasElement>(null);
  const scene = useRef(new CubeScene(props.appState.cube));
  const moveState = useRef<MoveState>({
    currentMove: -1,
    moves: [],
    stages: new SolveStageIndices(),
    markers: [],
  });
  const [paused, setPaused] = useState(true);
  const [moveList, setMoveList] = useState(
    <MoveList state={moveState.current} />
  );

  async function tryPlayNextAnimation() {
    if (!scene.current.isAnimationDone()) {
      return;
    }

    const moves = moveState.current;
    if (moves.currentMove + 1 < moves.moves.length) {
      const { side, clockwise } = moves.moves[++moves.currentMove];
      setMoveList(<MoveList state={moveState.current} />);
      await scene.current.rotateSide(side, clockwise);
      setTimeout(() => {
        setPaused((updatedPause) => {
          if (!updatedPause) {
            tryPlayNextAnimation();
          }
          return updatedPause;
        });
      }, 1000);
    }
  }

  useEffect(() => {
    const wrapper = wrapperEl.current!;

    let running = true;
    let time = Date.now();

    function loop() {
      const now = Date.now();
      const delta = now - time;
      time = now;

      scene.current!.update(delta);
      scene.current!.render();

      if (running) {
        window.requestAnimationFrame(loop);
      }
    }
    window.requestAnimationFrame(loop);

    const observer = new ResizeObserver(() => {
      const { clientWidth, clientHeight } = wrapper;
      scene.current!.resize(clientWidth, clientHeight);
    });
    observer.observe(wrapper);

    return () => {
      running = false;
      observer.unobserve(wrapper);
    };
  });

  useEffect(() => {
    scene.current.setCanvas(canvasEl.current!);

    let url =
      import.meta.env.VITE_BACKEND_URL +
      "/solve?" +
      new URLSearchParams({ session: props.appState.sessionId });

    fetch(url, { method: "POST" })
      .then((r) => r.json())
      .then((json) => {
        moveState.current = {
          currentMove: -1,
          moves: json.moves,
          stages: json.stageIndices,
          markers: json.markers,
        };
        setMoveList(<MoveList state={moveState.current} />);
      });
  }, []);

  function onNext() {
    if (!paused) {
      setPaused(true);
      return;
    }

    tryPlayNextAnimation();
  }

  function onPrev() {
    if (!paused) {
      setPaused(true);
      return;
    }

    if (!scene.current.isAnimationDone()) {
      return;
    }

    const moves = moveState.current;
    if (moves.currentMove < 0) {
      return;
    }

    const { side, clockwise } = moves.moves[moves.currentMove];
    setMoveList(
      <MoveList
        state={{
          ...moveState.current,
          currentMove: moveState.current.currentMove - 1,
        }}
      />
    );
    // Invert clockwise to "undo" move.
    // Forward animations are done by incrementing and then animating. Because we are going
    // backwards, the animation must be done before decrementing to maintain order.
    scene.current.rotateSide(side, !clockwise).then(() => moves.currentMove--);
  }

  function onPause() {
    setPaused((wasPaused) => {
      if (wasPaused) {
        tryPlayNextAnimation();
      }

      return !paused;
    });
  }

  return (
    <div className="flex h-full">
      <div key="__canvas__" ref={wrapperEl} className="min-w-0 flex-[2]">
        <canvas ref={canvasEl}></canvas>
      </div>
      <div className="flex flex-[1] flex-col">
        <div className="flex justify-center gap-5 py-10">
          <IconButton onClick={onPrev}>
            <BackwardIcon className="w-10" />
          </IconButton>
          <IconButton onClick={onPause}>
            {paused ? (
              <PlayIcon className="w-10" />
            ) : (
              <PauseIcon className="w-10" />
            )}
          </IconButton>
          <IconButton onClick={onNext}>
            <ForwardIcon className="w-10" />
          </IconButton>
        </div>
        <div className="overflow-y-scroll">{moveList}</div>
      </div>
    </div>
  );
}

function MoveList(props: { state: MoveState }) {
  let focused = useRef<HTMLDivElement>(null);

  useEffect(() => {
    focused.current?.scrollIntoView({ behavior: "smooth", block: "center" });
  });

  return props.state.moves.map((move, i) => {
    const isCurrent = props.state.currentMove === i;

    return (
      <div
        key={i}
        className={"px-5 py-2" + (isCurrent ? " bg-red-200" : "")}
        ref={isCurrent ? focused : undefined}
      >
        {GetMoveHeader(props.state.stages, i)}
        {GetMoveMarker(props.state.markers, i)}
        {`Rotate the ${move.side.toLowerCase()} side ${move.clockwise ? "clockwise" : "counter-clockwise"}`}
      </div>
    );
  });
}

type StepDescription = {
  header: string;
  description: string;
};

const STEP_DESCRIPTIONS: Record<keyof SolveStageIndices, StepDescription> = {
  whiteCross: {
    header: "Step 1: White Cross",
    description:
      "Solving a Rubik's cube typically starts on the white face. First, let's move " +
      "each of the white edges to their correct position on the bottom. After that, we'll cover " +
      "how to solve the remaining layers without destroying the progress we've already made. " +
      "The moves to make depend on where each piece is, so let's jump into an example:",
  },
  whiteCorners: {
    header: "Step 2: White Corners",
    description:
      "The white cross is now complete. Next, we need to ensure all four white corners are in " +
      "the correct position and rotation. Once again, we'll solve these without messing up the " +
      "white cross!",
  },
  secondLayer: {
    header: "Step 3: Second Layer Edges",
    description:
      "The white face is now complete. Now, we'll go over how to solve the middle layer.",
  },
  yellowCross: {
    header: "Step 4: Yellow Cross",
    description:
      "The last stage is to solve the yellow side. We'll being by moving each yellow edge to " +
      "the top. Unlike the white cross, we don't care if the yellow edges are aligned with " +
      "their neighboring color!",
  },
  yellowEdges: {
    header: "Step 5: Position Yellow Edges",
    description:
      "Now that the yellow cross is formed, let's move each edge to be touching the correct" +
      "neighboring color.",
  },
  positionYellowCorners: {
    header: "Step 6: Position Yellow Corners",
    description:
      "Only four corners left to go! First, let's move them to the correct position. They don't " +
      "need to be rotated properly, we'll cover that in the last step.",
  },
  orientYellowCorners: {
    header: "Step 7: Orient Yellow Corners",
    description:
      "All that's left is to correctly rotate the yellow corners! This is a simple algorithm " +
      "that's easy to memorize.",
  },
};

type MarkerFormatter = (args: string[]) => string;

const MARKER_FORMATTERS: Record<string, MarkerFormatter> = {
  whiteCrossBest: () =>
    "Rotate the white side to line up as many of the edges as possible",
  whiteEdge: (sides) => `Solve the white-${sides[0]} edge`,
  whiteCorner: (sides) => `Solve the white-${sides[0]}-${sides[1]} corner`,
  secondEdgeMove: (sides) =>
    `Move the ${sides[0]}-${sides[1]} edge next to it's target`,
  secondEdgeInsert: (sides) => `Insert the ${sides[0]}-${sides[1]} edge`,
  yellowL: () => `Arrange yellow edges into L shape`,
  yellowLine: () => `Arrange yellow edges into line`,
  yellowCross: () => `Arrange yellow edges into cross`,
  yellowCrossBest: () =>
    `Rotate the yellow side to line up as many of the edges as possible`,
  yellowSwap: () => `Swap yellow edges`,
  yellowCycle: () => `Cycle yellow corners`,
  yellowCorner: () => `Rotate yellow corner`,
};

function GetMoveHeader(steps: SolveStageIndices, index: number) {
  let step: StepDescription | undefined;
  for (const k of Object.keys(steps)) {
    const key = k as keyof SolveStageIndices;

    const stepIndex = steps[key];
    if (stepIndex === index) {
      step = STEP_DESCRIPTIONS[key];
      break;
    }
  }

  if (typeof step === "undefined") {
    return;
  }

  return (
    <div className="py-5">
      <h1 className="mb-5 rounded-full border-4 border-black bg-purple-400 p-2 text-center text-lg shadow-neo5">
        {step.header}
      </h1>
      <p className="text-lg">{step.description}</p>
    </div>
  );
}

function GetMoveMarker(markers: Record<string, MoveMarker>, index: number) {
  const marker = markers[index];
  if (typeof marker === "undefined") {
    return;
  }

  const formatter = MARKER_FORMATTERS[marker.id];
  if (typeof formatter === "undefined") {
    console.warn("Unknown marker formatter " + marker.id);
    return;
  }

  return (
    <div className="flex gap-2">
      <InformationCircleIcon className="w-5" color="#9ca3af" />
      <p className="r text-gray-400">{formatter(marker.arguments)}</p>
    </div>
  );
}
