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

    function loop() {
      controls.update();
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
  private rotationCollision = new THREE.Mesh(
    new THREE.BoxGeometry(1, 4, 4),
    new THREE.MeshBasicMaterial({ color: 0xffffff })
  );

  constructor(private sides: Record<string, string[]>) {
    super();
    this.rotationCollision.matrixAutoUpdate = false;
    this.add(this.rotationGroup);
    this.add(this.rotationCollision);

    this.addSide("GREEN");
    this.addSide("ORANGE", Math.PI / 2);
    this.addSide("BLUE", Math.PI);
    this.addSide("RED", Math.PI * 1.5);
    this.addSide("WHITE", 0, Math.PI / 2);
    this.addSide("YELLOW", 0, Math.PI / -2);
  }

  addSide(color: string, yRot: number = 0, zRot: number = 0) {
    const group = new THREE.Group();

    const centerFace = this.generateFace(COLORS[color]);
    centerFace.position.setX(-1.5);
    group.add(centerFace);

    const faces = this.sides[color];
    for (let i = 0; i < SIDE_OFFSETS.length; i++) {
      const color = COLORS[faces[i]];
      const face = this.generateFace(color);
      face.position.x = -1.5;
      face.position.y = SIDE_OFFSETS[i][1];
      face.position.z = SIDE_OFFSETS[i][0];
      group.add(face);
    }

    group.rotateY(yRot);
    group.rotateZ(zRot);

    const pos = new THREE.Vector3();
    const childrenCopy = group.children.slice();
    for (const child of childrenCopy) {
      child.getWorldPosition(pos);
      child.position.copy(pos);
      child.rotation.copy(group.rotation);
      this.add(child);
    }
  }

  rotateSide(sideColor: string) {
    const rotationInfo = ROTATIONS[sideColor];
    this.currentRotation = sideColor;

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
    this.rotationGroup.rotateOnAxis(rotationInfo.rotationAxis, Math.PI / 4);
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

    return new THREE.Mesh(geometry, new THREE.MeshBasicMaterial({ color }));
  }
}
