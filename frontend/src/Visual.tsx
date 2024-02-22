import { useEffect, useRef } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/Addons.js";
import { PlayIcon, ForwardIcon, BackwardIcon } from "@heroicons/react/16/solid";
import { IconButton } from "./Button";

export type CubeInfo = {
  sides: Record<string, string[]>;
  session: string;
};

export default function Visual(props: { cubeInfo: CubeInfo }) {
  const wrapperEl = useRef<HTMLDivElement>(null);
  const canvasEl = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const scene = new CubeScene(props.cubeInfo.sides);
    const camera = new THREE.PerspectiveCamera(70, 16 / 9, 0.01, 10);
    const renderer = new THREE.WebGLRenderer({
      canvas: canvasEl.current!,
    });

    const controls = new OrbitControls(camera, renderer.domElement);
    camera.position.set(0, 5, 0);
    controls.update();

    let running = true;
    let time = Date.now();

    function loop() {
      const now = Date.now();
      const delta = now - time;
      time = now;

      controls.update();
      scene.update(delta);
      renderer.render(scene, camera);

      if (running) {
        window.requestAnimationFrame(loop);
      }
    }
    window.requestAnimationFrame(loop);

    new ResizeObserver(() => {
      const width = wrapperEl.current!.clientWidth;
      const height = wrapperEl.current!.clientHeight;

      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.setSize(width, height);
    }).observe(wrapperEl.current!);

    let url =
      import.meta.env.VITE_BACKEND_URL +
      "/solve?" +
      new URLSearchParams({ session: props.cubeInfo.session });

    fetch(url, { method: "POST" })
      .then((r) => r.json())
      .then((json) => (scene.moves = json));

    return () => {
      running = false;
    };
  }, []);

  return (
    <div className="flex h-full">
      <div ref={wrapperEl} className="min-w-0 flex-[3]">
        <canvas ref={canvasEl}></canvas>
      </div>
      <div className="flex-[1]">
        <div className="flex justify-center gap-5 py-10">
          <IconButton onClick={() => {}}>
            <BackwardIcon className="w-10" />
          </IconButton>
          <IconButton onClick={() => {}}>
            <PlayIcon className="w-10" />
          </IconButton>
          <IconButton onClick={() => {}}>
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

type Move = {
  side: string;
  clockwise: boolean;
};

class CubeScene extends THREE.Scene {
  private rotatingFaces = new Array<THREE.Object3D>();
  private rotatingFaceMatrices = new Array<THREE.Matrix4>();
  private currentRotation: string | undefined = undefined;
  private rotationProgress = 0;
  private rotationDirection = 0;
  private rotationCollision = new THREE.Mesh(
    new THREE.BoxGeometry(1, 4, 4),
    new THREE.MeshBasicMaterial({ color: 0xffffff })
  );

  public moves = new Array<Move>();
  private currentMove = 0;

  constructor(private sides: Record<string, string[]>) {
    super();
    this.rotationCollision.matrixAutoUpdate = false;

    this.addSide("GREEN", axisY, 0);
    this.addSide("ORANGE", axisY, Math.PI / 2);
    this.addSide("BLUE", axisY, Math.PI);
    this.addSide("RED", axisY, Math.PI * 1.5);
    this.addSide("WHITE", axisZ, Math.PI / 2);
    this.addSide("YELLOW", axisZ, Math.PI / -2);
  }

  update(delta: number) {
    if (typeof this.currentRotation === "undefined") {
      if (this.currentMove >= this.moves.length) {
        return;
      }

      const move = this.moves[this.currentMove];
      this.rotateSide(move.side, move.clockwise);
      return;
    }

    const rotationAxis = ROTATIONS[this.currentRotation].rotationAxis;
    this.rotationProgress = Math.min(this.rotationProgress + delta / 500, 1);

    const rotation =
      THREE.MathUtils.lerp(0, Math.PI / 2, this.rotationProgress) *
      this.rotationDirection;

    const matrix = new THREE.Matrix4();
    const rotate = new THREE.Matrix4().makeRotationAxis(rotationAxis, rotation);

    for (let iFace = 0; iFace < this.rotatingFaces.length; iFace++) {
      const face = this.rotatingFaces[iFace];
      matrix.copy(this.rotatingFaceMatrices[iFace]);
      matrix.multiplyMatrices(rotate, matrix);
      face.matrix.copy(matrix);
    }

    if (this.rotationProgress === 1) {
      this.currentRotation = undefined;
      this.rotationProgress = 0;
      this.currentMove++;
    }
  }

  addSide(color: string, rotAxis: THREE.Vector3, rot: number) {
    const rotate = new THREE.Matrix4().makeRotationAxis(rotAxis, rot);
    const matrix = new THREE.Matrix4();

    const centerFace = this.generateFace(COLORS[color as keyof typeof COLORS]);
    centerFace.matrixAutoUpdate = false;

    matrix.makeTranslation(-1.5, 0, 0);
    matrix.multiplyMatrices(rotate, matrix);
    centerFace.matrix.copy(matrix);
    this.add(centerFace);

    const faceColors = this.sides[color];
    for (let i = 0; i < SIDE_OFFSETS.length; i++) {
      const color = COLORS[faceColors[i] as keyof typeof COLORS];
      const face = this.generateFace(color);

      matrix.makeTranslation(-1.5, SIDE_OFFSETS[i][1], SIDE_OFFSETS[i][0]);
      matrix.multiplyMatrices(rotate, matrix);
      face.matrix.copy(matrix);
      this.add(face);
    }
  }

  rotateSide(sideColor: string, clockwise: boolean) {
    const rotationInfo = ROTATIONS[sideColor];
    this.currentRotation = sideColor;
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
    for (const child of this.children) {
      const bounds = new THREE.Box3().setFromObject(child);
      if (collision.intersectsBox(bounds)) {
        this.rotatingFaces.push(child);
        this.rotatingFaceMatrices.push(child.matrix.clone());
      }
    }
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
