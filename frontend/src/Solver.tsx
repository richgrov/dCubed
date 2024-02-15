import { MouseEvent } from "react";
import Button from "./Button";
import { useDropzone } from "react-dropzone";

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
  const { getRootProps, getInputProps, isDragActive } = useDropzone();
  const handleUpload = (e: MouseEvent) => {};
  const handleCamera = (e: MouseEvent) => {};

  return (
    <div className="flex h-full flex-col items-center">
      <div
        {...getRootProps()}
        className="flex w-full flex-1 cursor-pointer items-center justify-center border-b-4 border-dashed border-black"
      >
        <input {...getInputProps()} />
        <h1 className="text-3xl">Click to Upload or Drop Photo Here</h1>
      </div>
      <div className="p-5">
        <Button onClick={handleCamera}>Use Camera Instead</Button>
      </div>
    </div>
  );
}
