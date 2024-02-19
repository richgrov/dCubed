import { useEffect, useRef } from "react";
import * as THREE from "three";

export default function Visual() {
  const wrapperEl = useRef<HTMLDivElement>(null);
  const canvasEl = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(70, 16 / 9, 0.01, 10);
    const renderer = new THREE.WebGLRenderer({
      canvas: canvasEl.current!,
    });
    let running = true;

    function loop() {
      renderer.render(scene, camera);

      if (running) {
        window.requestAnimationFrame(loop);
      }
    }

    new ResizeObserver(() => {
      const width = wrapperEl.current!.clientWidth;
      const height = wrapperEl.current!.clientHeight;

      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.setSize(width, height);
    }).observe(wrapperEl.current!);

    window.requestAnimationFrame(loop);

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
