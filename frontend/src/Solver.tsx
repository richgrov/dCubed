import { useCallback, useState } from "react";
import Button from "./Button";
import { useDropzone } from "react-dropzone";

export default function Solver() {
  const [sessionId, setSessionId] = useState<string | undefined>(undefined);

  const onPhoto = useCallback(async (file: File) => {
    const formData = new FormData();
    formData.append("photo", file);

    let url = import.meta.env.VITE_BACKEND_URL + "/scan-photo";
    if (typeof sessionId !== "undefined") {
      url += new URLSearchParams({ session: sessionId });
    }

    const reponse = await fetch(url, { method: "POST", body: formData });
  }, []);

  return (
    <div className="flex flex-col items-center gap-20 py-20">
      <h1 className="text-5xl">Step 1: Scan Cube</h1>
      <div className="aspect-video w-3/5 rounded-2xl border-4 border-black">
        <InputSetup onPhoto={onPhoto} />
      </div>
    </div>
  );
}

function InputSetup(props: { onPhoto: (file: File) => void }) {
  const onDrop = useCallback((accepted: File[]) => {
    props.onPhoto(accepted[0]);
  }, []);

  const handleCamera = () => {};
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: {
      "image/png": [".png"],
      "image/jpeg": [".jpeg"],
    },
    onDrop,
    maxFiles: 1,
  });

  return (
    <div className="flex h-full flex-col items-center">
      <div
        {...getRootProps()}
        className="flex w-full flex-1 cursor-pointer items-center justify-center border-b-4 border-dashed border-black"
      >
        <input {...getInputProps()} />
        <h1 className="text-3xl">
          {isDragActive
            ? "Drop Files Here"
            : "Click to Upload or Drop Photo Here"}
        </h1>
      </div>
      <div className="p-5">
        <Button onClick={handleCamera}>Use Camera Instead</Button>
      </div>
    </div>
  );
}
