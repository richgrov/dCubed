import { useEffect, useRef } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/Addons.js";

export type VisualProps = {
  sides: Record<string, string[]>;
};

export default function Visual(props: VisualProps) {
  const wrapperEl = useRef<HTMLDivElement>(null);
  const canvasEl = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const scene = new CubeScene(props.sides);
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

    return () => {
      running = false;
    };
  }, []);

  return (
    <div ref={wrapperEl} className="h-full w-full">
      <canvas ref={canvasEl}></canvas>
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

class CubeScene extends THREE.Scene {
  private rotationGroup = new THREE.Group();
  private currentRotation: string | undefined = undefined;
  private rotationProgress = 0;
  private rotationDirection = 0;
  private rotationCollision = new THREE.Mesh(
    new THREE.BoxGeometry(1, 4, 4),
    new THREE.MeshBasicMaterial({ color: 0xffffff })
  );

  constructor(private sides: Record<string, string[]>) {
    super();
    this.rotationCollision.matrixAutoUpdate = false;
    this.add(this.rotationGroup);

    this.addSide("GREEN", axisY, 0);
    this.addSide("ORANGE", axisY, Math.PI / 2);
    this.addSide("BLUE", axisY, Math.PI);
    this.addSide("RED", axisY, Math.PI * 1.5);
    this.addSide("WHITE", axisZ, Math.PI / 2);
    this.addSide("YELLOW", axisZ, Math.PI / -2);
  }

  update(delta: number) {
    if (typeof this.currentRotation === "undefined") {
      return;
    }

    const rotationAxis = ROTATIONS[this.currentRotation].rotationAxis;
    const rotationStep = delta / 500;
    const targetRotation = Math.PI / 2;
    const distanceFromTarget = Math.max(
      targetRotation - this.rotationProgress,
      0
    ); // max 0 to avoid possible microscopic floating point errors

    if (distanceFromTarget === 0) {
      this.currentRotation = undefined;
      this.rotationProgress = 0;

      const pos = new THREE.Vector3();
      const childrenCopy = this.rotationGroup.children.slice();
      for (const child of childrenCopy) {
        child.getWorldPosition(pos);
        child.position.copy(pos);
        const quat = new THREE.Quaternion();
        child.getWorldQuaternion(quat);
        child.setRotationFromQuaternion(quat);
        this.add(child);
      }

      this.rotationGroup.clear();
      this.rotationGroup.rotation.set(0, 0, 0);
      return;
    }

    const rotation = Math.min(rotationStep, distanceFromTarget);

    this.rotationGroup.rotateOnAxis(
      rotationAxis,
      rotation * this.rotationDirection
    );

    this.rotationProgress += rotation;
  }

  addSide(color: string, rotAxis: THREE.Vector3, rot: number) {
    const rotate = new THREE.Matrix4().makeRotationAxis(rotAxis, rot);
    const matrix = new THREE.Matrix4();

    const centerFace = this.generateFace(COLORS[color]);
    centerFace.matrixAutoUpdate = false;

    matrix.makeTranslation(-1.5, 0, 0);
    matrix.multiplyMatrices(rotate, matrix);
    centerFace.matrix.copy(matrix);
    this.add(centerFace);

    const faceColors = this.sides[color];
    for (let i = 0; i < SIDE_OFFSETS.length; i++) {
      const color = COLORS[faceColors[i]];
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

    const facesToRotate = new Array<THREE.Object3D>();
    for (const child of this.children) {
      if (child === this.rotationGroup) {
        continue;
      }

      const bounds = new THREE.Box3().setFromObject(child);
      if (collision.intersectsBox(bounds)) {
        facesToRotate.push(child);
      }
    }

    this.rotationGroup.add(...facesToRotate);
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
