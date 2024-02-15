import { MouseEvent } from "react";
import Button from "./Button";

export default function Solver() {
  return (
    <div className="flex flex-col items-center gap-20 py-20">
      <h1 className="text-5xl">Step 1: Scan Cube</h1>
      <div className="aspect-video w-3/5 rounded-2xl border-4 border-black">
        <InputSetup />
      </div>
    </div>
  );
}

function InputSetup() {
  const handleUpload = (e: MouseEvent) => {};
  const handleCamera = (e: MouseEvent) => {};

  return (
    <div className="flex h-full flex-col items-center justify-center gap-5">
      <h1 className="text-3xl">Upload or Drop Photo Here</h1>
      <div className="flex gap-10">
        <Button onClick={handleUpload}>Upload Photo</Button>
        <Button onClick={handleCamera}>Use Camera</Button>
      </div>
    </div>
  );
}
