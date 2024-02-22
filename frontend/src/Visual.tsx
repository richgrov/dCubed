import { useEffect, useRef, useState } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/Addons.js";
import {
  PlayIcon,
  ForwardIcon,
  BackwardIcon,
  PauseIcon,
} from "@heroicons/react/16/solid";
import { IconButton } from "./Button";

export type CubeInfo = {
  sides: Record<string, string[]>;
  session: string;
};

type MoveState = {
  currentMove: number;
  moves: {
    side: string;
    clockwise: boolean;
  }[];
};

export default function Visual(props: { cubeInfo: CubeInfo }) {
  const wrapperEl = useRef<HTMLDivElement>(null);
  const canvasEl = useRef<HTMLCanvasElement>(null);
  const scene = useRef(new CubeScene(props.cubeInfo.sides));
  const moveState = useRef<MoveState>({ currentMove: -1, moves: [] });
  const [paused, setPaused] = useState(true);

  function tryPlayNextAnimation() {
    if (!scene.current.isAnimationDone()) {
      return;
    }

    const moves = moveState.current;
    if (moves.currentMove + 1 < moves.moves.length) {
      const { side, clockwise } = moves.moves[++moves.currentMove];
      scene.current.rotateSide(side, clockwise).then(() => {
        setPaused((updatedPause) => {
          if (!updatedPause) {
            tryPlayNextAnimation();
          }
          return updatedPause;
        });
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
      new URLSearchParams({ session: props.cubeInfo.session });

    fetch(url, { method: "POST" })
      .then((r) => r.json())
      .then((json) => (moveState.current = { currentMove: -1, moves: json }));
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
      <div ref={wrapperEl} className="min-w-0 flex-[3]">
        <canvas ref={canvasEl}></canvas>
      </div>
      <div className="flex-[1]">
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
      </div>
    </div>
  );
}

const COLORS = {
  WHITE: 0xffffff,
  RED: 0xff0000,
  ORANGE: 0xff5500,
  YELLOW: 0xffff00,
  GREEN: 0x00ff00,
  BLUE: 0x0000ff,
};

type RotationInfo = {
  boundaryAxis: THREE.Vector3;
  boundaryRotation: number;
  rotationAxis: THREE.Vector3;
};

const axisX = new THREE.Vector3(1, 0, 0);
const axisNegX = new THREE.Vector3(-1, 0, 0);
const axisY = new THREE.Vector3(0, 1, 0);
const axisNegY = new THREE.Vector3(0, -1, 0);
const axisZ = new THREE.Vector3(0, 0, 1);
const axisNegZ = new THREE.Vector3(0, 0, -1);

const ROTATIONS: Record<string, RotationInfo> = {
  WHITE: {
    boundaryAxis: axisZ,
    boundaryRotation: Math.PI / -2,
    rotationAxis: axisY,
  },
  RED: {
    boundaryAxis: axisY,
    boundaryRotation: Math.PI / 2,
    rotationAxis: axisZ,
  },
  ORANGE: {
    boundaryAxis: axisY,
    boundaryRotation: Math.PI * 1.5,
    rotationAxis: axisNegZ,
  },
  YELLOW: {
    boundaryAxis: axisZ,
    boundaryRotation: Math.PI / 2,
    rotationAxis: axisNegY,
  },
  GREEN: {
    boundaryAxis: axisY,
    boundaryRotation: Math.PI,
    rotationAxis: axisX,
  },
  BLUE: {
    boundaryAxis: axisY,
    boundaryRotation: 0,
    rotationAxis: axisNegX,
  },
};

const SIDE_OFFSETS = [
  [-1, 1],
  [0, 1],
  [1, 1],
  [1, 0],
  [1, -1],
  [0, -1],
  [-1, -1],
  [-1, 0],
];

class CubeScene {
  private scene = new THREE.Scene();
  private camera = new THREE.PerspectiveCamera(70, 16 / 9, 0.01, 10);
  private renderer: THREE.Renderer | undefined;
  private controls: OrbitControls | undefined;

  private rotatingFaces = new Array<THREE.Object3D>();
  private rotatingFaceMatrices = new Array<THREE.Matrix4>();
  private rotationProgress = 1;
  private rotationAxis = new THREE.Vector3();
  private rotationDirection = 0;
  private completionHandler: (() => void) | undefined;

  private rotationCollision = new THREE.Mesh(
    new THREE.BoxGeometry(1, 4, 4),
    new THREE.MeshBasicMaterial({ color: 0xffffff })
  );

  constructor(private sides: Record<string, string[]>) {
    this.camera.position.set(0, 5, 0);
    this.rotationCollision.matrixAutoUpdate = false;

    this.addSide("GREEN", axisY, 0);
    this.addSide("ORANGE", axisY, Math.PI / 2);
    this.addSide("BLUE", axisY, Math.PI);
    this.addSide("RED", axisY, Math.PI * 1.5);
    this.addSide("WHITE", axisZ, Math.PI / 2);
    this.addSide("YELLOW", axisZ, Math.PI / -2);
  }

  setCanvas(canvas: HTMLCanvasElement) {
    if (typeof this.renderer === "undefined") {
      this.renderer = new THREE.WebGLRenderer({ canvas });
    } else {
      this.renderer.domElement = canvas;
    }

    this.controls?.dispose();
    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.update();
  }

  update(delta: number) {
    this.controls!.update();

    this.rotationProgress = Math.min(this.rotationProgress + delta / 500, 1);

    const rotation =
      THREE.MathUtils.lerp(0, Math.PI / 2, this.rotationProgress) *
      this.rotationDirection;

    const matrix = new THREE.Matrix4();
    const rotate = new THREE.Matrix4().makeRotationAxis(
      this.rotationAxis,
      rotation
    );

    for (let iFace = 0; iFace < this.rotatingFaces.length; iFace++) {
      const face = this.rotatingFaces[iFace];
      matrix.copy(this.rotatingFaceMatrices[iFace]);
      matrix.multiplyMatrices(rotate, matrix);
      face.matrix.copy(matrix);
    }

    if (
      this.isAnimationDone() &&
      typeof this.completionHandler !== "undefined"
    ) {
      // store in temporary variable andclear immediately to reduce odds of infinite recursion
      const completionHandler = this.completionHandler;
      this.completionHandler = undefined;
      completionHandler();
    }
  }

  render() {
    this.renderer!.render(this.scene, this.camera);
  }

  resize(width: number, height: number) {
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer!.setSize(width, height);
  }

  isAnimationDone() {
    return this.rotationProgress === 1;
  }

  addSide(color: string, rotAxis: THREE.Vector3, rot: number) {
    const rotate = new THREE.Matrix4().makeRotationAxis(rotAxis, rot);
    const matrix = new THREE.Matrix4();

    const centerFace = this.generateFace(COLORS[color as keyof typeof COLORS]);
    centerFace.matrixAutoUpdate = false;

    matrix.makeTranslation(-1.5, 0, 0);
    matrix.multiplyMatrices(rotate, matrix);
    centerFace.matrix.copy(matrix);
    this.scene.add(centerFace);

    const faceColors = this.sides[color];
    for (let i = 0; i < SIDE_OFFSETS.length; i++) {
      const color = COLORS[faceColors[i] as keyof typeof COLORS];
      const face = this.generateFace(color);

      matrix.makeTranslation(-1.5, SIDE_OFFSETS[i][1], SIDE_OFFSETS[i][0]);
      matrix.multiplyMatrices(rotate, matrix);
      face.matrix.copy(matrix);
      this.scene.add(face);
    }
  }

  rotateSide(sideColor: string, clockwise: boolean): Promise<void> {
    if (this.rotationProgress !== 1) {
      throw new Error("rotation not done");
    }

    this.rotationProgress = 0;
    const rotationInfo = ROTATIONS[sideColor];
    this.rotationAxis = rotationInfo.rotationAxis;
    this.rotationDirection = clockwise ? 1 : -1;

    const translate = new THREE.Matrix4().makeTranslation(
      new THREE.Vector3(1.5, 0, 0)
    );
    const rotate = new THREE.Matrix4().makeRotationAxis(
      rotationInfo.boundaryAxis,
      rotationInfo.boundaryRotation
    );
    this.rotationCollision.matrix.copy(rotate.multiply(translate));

    const collision = new THREE.Box3().setFromObject(this.rotationCollision);

    this.rotatingFaces = new Array();
    this.rotatingFaceMatrices = new Array();
    for (const child of this.scene.children) {
      const bounds = new THREE.Box3().setFromObject(child);
      if (collision.intersectsBox(bounds)) {
        this.rotatingFaces.push(child);
        this.rotatingFaceMatrices.push(child.matrix.clone());
      }
    }

    return new Promise((resolve) => {
      this.completionHandler = resolve;
    });
  }

  generateFace(color: number) {
    const geometry = new THREE.BufferGeometry();

    // prettier-ignore
    geometry.setAttribute("position", new THREE.Float32BufferAttribute([
      0, -0.4, -0.4,
      0,  0.4,  0.4,
      0,  0.4, -0.4,
      0, -0.4,  0.4,
    ], 3));
    geometry.setIndex(new THREE.Uint16BufferAttribute([0, 1, 2, 3, 1, 0], 1));
    geometry.computeVertexNormals();

    const mesh = new THREE.Mesh(
      geometry,
      new THREE.MeshBasicMaterial({ color })
    );
    mesh.matrixAutoUpdate = false;
    return mesh;
  }
}
