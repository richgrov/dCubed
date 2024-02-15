export default function Solver() {
  return (
    <div className="flex flex-col items-center">
      <h1 className="py-20 text-5xl">Step 1: Scan Cube</h1>
      <div className="aspect-video w-3/5 rounded-2xl border-4 border-black">
        <InputSetup />
      </div>
    </div>
  );
}

function InputSetup() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-5">
      <h1 className="text-3xl">Upload or Drop Photo Here</h1>
      <div className="flex gap-10">
        <button className="rounded-lg border-4 border-black p-4 shadow-[black_10px_10px]">
          Upload Photo
        </button>
        <button className="rounded-lg border-4 border-black p-4 shadow-[black_10px_10px]">
          Enable camera
        </button>
      </div>
    </div>
  );
}
