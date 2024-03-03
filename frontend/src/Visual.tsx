import { useEffect, useRef, useState } from "react";
import {
  PlayIcon,
  ForwardIcon,
  BackwardIcon,
  PauseIcon,
} from "@heroicons/react/16/solid";
import { IconButton } from "./Button";
import CubeScene from "./CubeScene";
import { AppState, Move, SolveStageIndices } from "./model";

type MoveState = {
  currentMove: number;
  moves: Move[];
  stages: SolveStageIndices;
};

export default function Visual(props: { appState: AppState }) {
  const wrapperEl = useRef<HTMLDivElement>(null);
  const canvasEl = useRef<HTMLCanvasElement>(null);
  const scene = useRef(new CubeScene(props.appState.cube));
  const moveState = useRef<MoveState>({
    currentMove: -1,
    moves: [],
    stages: new SolveStageIndices(),
  });
  const [paused, setPaused] = useState(true);
  const [moveList, setMoveList] = useState(<MoveList moves={[]} />);

  async function tryPlayNextAnimation() {
    if (!scene.current.isAnimationDone()) {
      return;
    }

    const moves = moveState.current;
    if (moves.currentMove + 1 < moves.moves.length) {
      const { side, clockwise } = moves.moves[++moves.currentMove];
      await scene.current.rotateSide(side, clockwise);
      setPaused((updatedPause) => {
        if (!updatedPause) {
          tryPlayNextAnimation();
        }
        return updatedPause;
      });
    }
  }

  useEffect(() => {
    const wrapper = wrapperEl.current!;
    scene.current.setCanvas(canvasEl.current!);

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
          stages: json.stages,
        };
        setMoveList(<MoveList moves={json.moves} />);
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
      <div ref={wrapperEl} className="min-w-0 flex-[2]">
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

function MoveList(props: { moves: Move[] }) {
  return props.moves.map((move, i) => (
    <div key={i} className="px-5 pb-4">
      Rotate the {move.side.toLowerCase()} side{" "}
      {move.clockwise ? "clockwise" : "counter-clockwise"}
    </div>
  ));
}
